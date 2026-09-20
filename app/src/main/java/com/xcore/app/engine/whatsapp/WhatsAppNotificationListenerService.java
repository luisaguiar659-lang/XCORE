package com.xcore.app.engine.whatsapp;

import android.app.Notification;
import android.app.Person;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class WhatsAppNotificationListenerService extends NotificationListenerService {
    private static final String WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b";
    private static final String WHATSAPP_PACKAGE = "com.whatsapp";

    private WhatsAppTriggerEngine engine;
    private WhatsAppNotificationTransport transport;

    // Guarda a última mensagem efetivamente processada, e não apenas o key da notificação.
    // O WhatsApp reutiliza/atualiza a mesma notificação durante uma conversa.
    private final Map<String, Long> processedMessages = new HashMap<>();
    private final Map<String, Long> sentMessages = new HashMap<>();

    @Override public void onCreate() {
        super.onCreate();
        AndroidWhatsAppTriggerStore store =
                new AndroidWhatsAppTriggerStore(getApplicationContext());
        transport = new WhatsAppNotificationTransport(this);
        engine = new WhatsAppTriggerEngine(
                store,
                new LocalWhatsAppFlowRouter(),
                new AndroidWhatsAppConversationStore(getApplicationContext()),
                transport
        );
        engine.start();
    }

    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        if (!isWhatsAppNotification(sbn) || sbn.isOngoing()) return;

        Notification notification = sbn.getNotification();
        if (notification == null || notification.extras == null) return;

        IncomingMessage incoming = extractLatestIncomingMessage(notification, sbn.getPostTime());
        if (incoming == null || incoming.text.trim().isEmpty()) return;

        long now = System.currentTimeMillis();

        // A mesma conversa pode republicar a notificação várias vezes.
        // O timestamp da mensagem faz mensagens iguais, mas realmente novas,
        // serem tratadas separadamente.
        String sender = incoming.sender;
        if (sender.isEmpty()) sender = extractSender(notification);
        if (sender.isEmpty()) sender = sbn.getKey();

        String messageKey = normalizeMessageKey(sender, incoming.text, incoming.timestamp);
        synchronized (processedMessages) {
            Long previous = processedMessages.get(messageKey);
            if (previous != null && now - previous < 60000L) return;

            processedMessages.put(messageKey, now);
            if (processedMessages.size() > 300) {
                processedMessages.entrySet().removeIf(
                        entry -> now - entry.getValue() > 120000L
                );
            }
        }

        // Segunda barreira contra resposta do próprio XCORE voltando como entrada.
        String outgoingKey = normalizeLoopKey(sender, incoming.text);
        synchronized (sentMessages) {
            Long sentAt = sentMessages.get(outgoingKey);
            if (sentAt != null) {
                if (now - sentAt < 15000L) {
                    sentMessages.remove(outgoingKey);
                    return;
                }
                sentMessages.remove(outgoingKey);
            }
        }

        transport.setReplyTarget(sbn);

        WhatsAppMessage message = new WhatsAppMessage(
                sbn.getKey() + ":" + incoming.timestamp,
                sender,
                incoming.text.trim(),
                incoming.timestamp,
                buildMetadata(sbn, sender)
        );

        WhatsAppResult result = engine.receive(message);
        if (result.isSuccess()) {
            transport.clearReplyTarget();
        }
    }

    @Override public void onDestroy() {
        if (engine != null) engine.stop();
        super.onDestroy();
    }

    private boolean isWhatsAppNotification(StatusBarNotification sbn) {
        if (sbn == null) return false;
        String pkg = sbn.getPackageName();
        return WHATSAPP_BUSINESS_PACKAGE.equals(pkg) || WHATSAPP_PACKAGE.equals(pkg);
    }

    /**
     * Extrai a mensagem recebida mais recente da conversa.
     *
     * Prioridade:
     * 1) Notification.MessagingStyle.EXTRA_MESSAGES;
     * 2) fallback para EXTRA_BIG_TEXT/EXTRA_TEXT/EXTRA_TEXT_LINES.
     *
     * No MessagingStyle, mensagens enviadas pelo próprio usuário são ignoradas.
     * Isso evita que a resposta do XCORE volte para o motor como um novo comando.
     */
    private IncomingMessage extractLatestIncomingMessage(
            Notification notification, long fallbackTimestamp) {

        Bundle extras = notification.extras;
        if (extras == null) return null;

        String selfName = extractMessagingUserName(extras);

        Parcelable[] parcelables = extras.getParcelableArray(Notification.EXTRA_MESSAGES);
        if (parcelables != null && parcelables.length > 0) {
            if (Build.VERSION.SDK_INT >= 30) {
                List<Notification.MessagingStyle.Message> messages =
                        Notification.MessagingStyle.Message
                                .getMessagesFromBundleArray(parcelables);

                for (int i = messages.size() - 1; i >= 0; i--) {
                    Notification.MessagingStyle.Message item = messages.get(i);
                    String text = item.getText() == null ? "" : item.getText().toString().trim();
                    if (text.isEmpty()) continue;

                    Person person = item.getSenderPerson();
                    String sender = person == null || person.getName() == null
                            ? ""
                            : person.getName().toString().trim();

                    boolean self = person == null
                            || (!selfName.isEmpty() && selfName.equalsIgnoreCase(sender));

                    if (!self) {
                        long timestamp = item.getTimestamp() > 0
                                ? item.getTimestamp() : fallbackTimestamp;
                        return new IncomingMessage(text, sender, timestamp);
                    }
                }
            } else {
                // Android 10 (API 29) não possui getMessagesFromBundleArray().
                // Os bundles ainda seguem o formato público do MessagingStyle.
                for (int i = parcelables.length - 1; i >= 0; i--) {
                    if (!(parcelables[i] instanceof Bundle)) continue;

                    Bundle item = (Bundle) parcelables[i];
                    CharSequence rawText = item.getCharSequence("text");
                    if (rawText == null) continue;

                    String text = rawText.toString().trim();
                    if (text.isEmpty()) continue;

                    CharSequence rawSender = item.getCharSequence("sender");
                    String sender = rawSender == null ? "" : rawSender.toString().trim();
                    boolean self = sender.isEmpty()
                            || (!selfName.isEmpty() && selfName.equalsIgnoreCase(sender));

                    if (!self) {
                        long timestamp = item.getLong("time", fallbackTimestamp);
                        if (timestamp <= 0) timestamp = fallbackTimestamp;
                        return new IncomingMessage(text, sender, timestamp);
                    }
                }
            }
        }

        // Fallback para notificações que não usam MessagingStyle.
        CharSequence bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        if (bigText != null && bigText.length() > 0) {
            return new IncomingMessage(bigText.toString().trim(), extractSender(notification), fallbackTimestamp);
        }

        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        if (text != null && text.length() > 0) {
            return new IncomingMessage(text.toString().trim(), extractSender(notification), fallbackTimestamp);
        }

        CharSequence[] lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
        if (lines != null && lines.length > 0) {
            CharSequence last = lines[lines.length - 1];
            if (last != null && last.length() > 0) {
                return new IncomingMessage(last.toString().trim(), extractSender(notification), fallbackTimestamp);
            }
        }

        return null;
    }

    private String extractMessagingUserName(Bundle extras) {
        if (Build.VERSION.SDK_INT < 28) return "";

        try {
            Person person = extras.getParcelable(Notification.EXTRA_MESSAGING_PERSON);
            if (person != null && person.getName() != null) {
                return person.getName().toString().trim();
            }
        } catch (Exception ignored) {
        }

        CharSequence legacy = extras.getCharSequence("android.selfDisplayName");
        return legacy == null ? "" : legacy.toString().trim();
    }

    private String extractSender(Notification notification) {
        Bundle extras = notification.extras;
        if (extras == null) return "";
        CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
        return title == null ? "" : title.toString().trim();
    }

    private Map<String, String> buildMetadata(StatusBarNotification sbn, String sender) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("notificationKey", sbn.getKey());
        metadata.put("packageName", sbn.getPackageName());
        metadata.put("sender", sender);
        return metadata;
    }

    void markOutgoing(String phone, String text) {
        if (text == null || text.trim().isEmpty()) return;

        String key = normalizeLoopKey(phone, text);
        synchronized (sentMessages) {
            sentMessages.put(key, System.currentTimeMillis());
            if (sentMessages.size() > 100) {
                long now = System.currentTimeMillis();
                sentMessages.entrySet().removeIf(e -> now - e.getValue() > 30000L);
            }
        }
    }

    private static String normalizeLoopKey(String sender, String text) {
        String a = sender == null ? "" : sender.trim().toLowerCase(Locale.ROOT);
        String b = text == null ? "" : text.trim().toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
        return a + "|" + b;
    }

    private static String normalizeMessageKey(String sender, String text, long timestamp) {
        String a = sender == null ? "" : sender.trim().toLowerCase(Locale.ROOT);
        String b = text == null ? "" : text.trim().toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
        return a + "|" + b + "|" + timestamp;
    }

    private static final class IncomingMessage {
        final String text;
        final String sender;
        final long timestamp;

        IncomingMessage(String text, String sender, long timestamp) {
            this.text = text == null ? "" : text;
            this.sender = sender == null ? "" : sender;
            this.timestamp = timestamp;
        }
    }

    private static final class LocalWhatsAppFlowRouter implements WhatsAppFlowRouter {
        @Override public WhatsAppResult execute(String flowId, WhatsAppMessage message) {
            return WhatsAppResult.success("Fluxo " + flowId + " recebido pela notificação", null);
        }
    }

}
