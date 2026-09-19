package com.xcore.app.engine.whatsapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import java.util.HashMap;
import java.util.Map;

public final class WhatsAppNotificationTransport implements WhatsAppTransport {
    private final WhatsAppNotificationListenerService service;
    private final Map<String, Notification.Action> replyActions = new HashMap<>();

    public WhatsAppNotificationTransport(WhatsAppNotificationListenerService service) {
        this.service = service;
    }

    public synchronized void setReplyTarget(String phone, StatusBarNotification notification) {
        if (phone == null || phone.trim().isEmpty()) return;
        Notification.Action action = findReplyAction(notification);
        if (action != null) replyActions.put(phone.trim(), action);
    }

    public synchronized void clearReplyTarget(String phone) {
        if (phone == null) return;
        replyActions.remove(phone.trim());
    }

    @Override public synchronized WhatsAppResult start() {
        return WhatsAppResult.success("Transporte por notificações iniciado", null);
    }

    @Override public synchronized WhatsAppResult stop() {
        replyActions.clear();
        return WhatsAppResult.success("Transporte por notificações parado", null);
    }

    @Override public synchronized boolean isRunning() { return true; }

    @Override public synchronized WhatsAppResult sendText(String phone, String text) {
        if (phone == null || phone.trim().isEmpty()) {
            return WhatsAppResult.error("Número do cliente não informado");
        }

        Notification.Action currentReplyAction = replyActions.get(phone.trim());
        if (currentReplyAction == null) {
            return WhatsAppResult.error("A ação de resposta do WhatsApp não está mais disponível");
        }

        RemoteInput[] inputs = currentReplyAction.getRemoteInputs();
        if (inputs == null || inputs.length == 0) {
            return WhatsAppResult.error("A ação de resposta não possui RemoteInput");
        }

        Bundle results = new Bundle();
        for (RemoteInput input : inputs) {
            results.putCharSequence(input.getResultKey(), text == null ? "" : text);
        }

        Intent intent = new Intent();
        RemoteInput.addResultsToIntent(inputs, intent, results);

        try {
            PendingIntent pendingIntent = currentReplyAction.actionIntent;
            pendingIntent.send(service, 0, intent);
            return WhatsAppResult.success("Resposta enviada pela notificação", null);
        } catch (PendingIntent.CanceledException e) {
            replyActions.remove(phone.trim());
            return WhatsAppResult.error("A ação de resposta do WhatsApp expirou");
        }
    }

    private Notification.Action findReplyAction(StatusBarNotification notification) {
        if (notification == null || notification.getNotification() == null) return null;

        Notification.Action[] actions = notification.getNotification().actions;
        if (actions == null) return null;

        for (Notification.Action action : actions) {
            if (action == null || action.getRemoteInputs() == null) continue;
            CharSequence title = action.title;
            String label = title == null ? "" : title.toString().toLowerCase();
            if (label.contains("responder") || label.contains("reply") || label.contains("resposta")) {
                return action;
            }
        }

        Notification.Action candidate = null;
        for (Notification.Action action : actions) {
            if (action != null && action.getRemoteInputs() != null
                    && action.getRemoteInputs().length > 0) {
                if (candidate != null) return candidate;
                candidate = action;
            }
        }
        return candidate;
    }
}
