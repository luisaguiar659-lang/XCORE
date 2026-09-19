package com.xcore.app.engine.whatsapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

public final class WhatsAppNotificationTransport implements WhatsAppTransport {
    private final WhatsAppNotificationListenerService service;
    private StatusBarNotification currentNotification;
    private Notification.Action currentReplyAction;

    public WhatsAppNotificationTransport(WhatsAppNotificationListenerService service) {
        this.service = service;
    }

    public synchronized void setReplyTarget(StatusBarNotification notification) {
        currentNotification = notification;
        currentReplyAction = findReplyAction(notification);
    }

    public synchronized void clearReplyTarget() {
        currentNotification = null;
        currentReplyAction = null;
    }

    @Override public WhatsAppResult start() {
        return WhatsAppResult.success("Transporte por notificações iniciado", null);
    }

    @Override public WhatsAppResult stop() {
        clearReplyTarget();
        return WhatsAppResult.success("Transporte por notificações parado", null);
    }

    @Override public boolean isRunning() { return true; }

    @Override public synchronized WhatsAppResult sendText(String phone, String text) {
        if (currentNotification == null || currentReplyAction == null) {
            return WhatsAppResult.error("A notificação não possui ação de resposta");
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
            return WhatsAppResult.error("A ação de resposta da notificação expirou");
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
