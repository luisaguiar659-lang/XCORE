package com.xcore.app.engine.whatsapp;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.xcore.app.engine.masterflix.MasterflixAutomationAccessibilityService;
import java.util.HashMap;
import java.util.Map;

public final class WhatsAppNotificationListenerService extends NotificationListenerService {
    private static final String WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b";
    private static final String WHATSAPP_PACKAGE = "com.whatsapp";

    private WhatsAppTriggerEngine engine;
    private WhatsAppNotificationTransport transport;
    private final Map<String, Long> processedNotifications = new HashMap<>();

    @Override public void onCreate() {
        super.onCreate();
        AndroidWhatsAppTriggerStore store =
                new AndroidWhatsAppTriggerStore(getApplicationContext());
        transport = new WhatsAppNotificationTransport(this);
        engine = new WhatsAppTriggerEngine(
                store,
                new LocalWhatsAppFlowRouter(transport),
                new AndroidWhatsAppConversationStore(getApplicationContext()),
                transport
        );
        engine.start();
    }

    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        if (!isWhatsAppNotification(sbn) || sbn.isOngoing()) return;

        Notification notification = sbn.getNotification();
        if (notification == null) return;

        CharSequence text = extractMessage(notification);
        if (text == null || text.toString().trim().isEmpty()) return;

        long now = System.currentTimeMillis();
        String key = sbn.getKey();

        synchronized (processedNotifications) {
            Long previous = processedNotifications.get(key);
            if (previous != null && now - previous < 5000L) return;
            processedNotifications.put(key, now);
            if (processedNotifications.size() > 200) {
                processedNotifications.entrySet().removeIf(
                        entry -> now - entry.getValue() > 60000L
                );
            }
        }

        String sender = extractSender(notification);
        if (sender.isEmpty()) sender = key;

        transport.setReplyTarget(sender, sbn);

        WhatsAppMessage message = new WhatsAppMessage(
                key + ":" + now,
                sender,
                text.toString().trim(),
                now,
                buildMetadata(sbn, sender)
        );

        WhatsAppResult result = engine.receive(message);
        // O alvo fica armazenado por telefone para permitir que o Masterflix
        // responda de forma assíncrona depois de gerar o teste no navegador.
        if (!result.isSuccess() && result.getMessage() != null) {
            transport.sendText(sender, "⚠️ " + result.getMessage());
            transport.clearReplyTarget(sender);
        }
    }

    @Override public void onDestroy() {
        if (engine != null) engine.stop();
        if (transport != null) transport.stop();
        super.onDestroy();
    }

    private boolean isWhatsAppNotification(StatusBarNotification sbn) {
        if (sbn == null) return false;
        String pkg = sbn.getPackageName();
        return WHATSAPP_BUSINESS_PACKAGE.equals(pkg) || WHATSAPP_PACKAGE.equals(pkg);
    }

    private CharSequence extractMessage(Notification notification) {
        Bundle extras = notification.extras;
        if (extras == null) return null;
        CharSequence bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        if (bigText != null && bigText.length() > 0) return bigText;
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        if (text != null && text.length() > 0) return text;
        CharSequence[] lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
        if (lines != null && lines.length > 0) return lines[lines.length - 1];
        return null;
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

    private static final class LocalWhatsAppFlowRouter implements WhatsAppFlowRouter {
        private final WhatsAppNotificationTransport transport;

        LocalWhatsAppFlowRouter(WhatsAppNotificationTransport transport) {
            this.transport = transport;
        }

        @Override public WhatsAppResult execute(String flowId, WhatsAppMessage message) {
            if (WhatsAppFlowIds.TESTE_CLIENTE.equals(flowId)) {
                MasterflixAutomationAccessibilityService.startTest(
                        "MASTERFLIX TESTE COMPLETO 1H",
                        new MasterflixAutomationAccessibilityService.Callback() {
                            @Override public void onSuccess(String text) {
                                transport.sendText(message.getPhone(), "🎬 TESTE GERADO\n\n" + text);
                                transport.clearReplyTarget(message.getPhone());
                            }

                            @Override public void onError(String error) {
                                transport.sendText(message.getPhone(), "⚠️ Não foi possível gerar o teste: " + error);
                                transport.clearReplyTarget(message.getPhone());
                            }
                        }
                );
                return WhatsAppResult.success("Geração do teste Masterflix iniciada", null);
            }

            return WhatsAppResult.success("Fluxo " + flowId + " recebido pela notificação", null);
        }
    }
}
