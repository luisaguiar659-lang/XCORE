package com.xcore.app.engine.whatsapp;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.content.Intent;
import com.xcore.app.engine.masterflix.MasterflixActivity;
import com.xcore.app.engine.masterflix.MasterflixWebAutomation;
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
                new LocalWhatsAppFlowRouter(this),
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

        transport.setReplyTarget(sbn);

        WhatsAppMessage message = new WhatsAppMessage(
                key + ":" + now,
                sender,
                text.toString().trim(),
                now,
                buildMetadata(sbn, sender)
        );

        WhatsAppResult result = engine.receive(message);
        if (result.isSuccess() && !MasterflixWebAutomation.hasRequest()) transport.clearReplyTarget();
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
        private final WhatsAppNotificationListenerService service;

        LocalWhatsAppFlowRouter(WhatsAppNotificationListenerService service) {
            this.service = service;
        }

        @Override public WhatsAppResult execute(String flowId, WhatsAppMessage message) {
            if (WhatsAppFlowIds.TESTE_CLIENTE.equals(flowId)) {
                try {
                    MasterflixWebAutomation.request(
                            message.getPhone(),
                            "MASTERFLIX TESTE COMPLETO 1H",
                            service.transport
                    );
                    Intent intent = new Intent(service, MasterflixActivity.class);
                    intent.putExtra(MasterflixActivity.EXTRA_AUTO_TEST, true);
                    intent.putExtra(MasterflixActivity.EXTRA_TEST_LABEL, "MASTERFLIX TESTE COMPLETO 1H");
                    intent.putExtra(MasterflixActivity.EXTRA_PHONE, message.getPhone());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    service.startActivity(intent);
                    return WhatsAppResult.success("Fluxo de teste Masterflix iniciado", null);
                } catch (Exception e) {
                    return WhatsAppResult.error("Não foi possível abrir o Masterflix: " + e.getMessage());
                }
            }
            return WhatsAppResult.success("Fluxo " + flowId + " recebido pela notificação", null);
        }
    }
}
