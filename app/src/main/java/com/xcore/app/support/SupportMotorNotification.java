package com.xcore.app.support;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.xcore.app.R;

public final class SupportMotorNotification {
    public static final String CHANNEL_ID = "xcore_support_motor";
    public static final int NOTIFICATION_ID = 4201;
    public static final String ACTION_ACTIVATE = "com.xcore.app.support.ACTIVATE_MOTOR";
    public static final String ACTION_PAUSE = "com.xcore.app.support.PAUSE_MOTOR";
    private SupportMotorNotification() {}

    public static void ensureChannel(Context c) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Motor de Suporte", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Controle do motor de avisos automáticos do Grupo de Suporte.");
            ((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(ch);
        }
    }

    public static Notification build(Context c, boolean running, String status, String action, String actionText) {
        ensureChannel(c);
        Intent i = new Intent(c, SupportMotorActionReceiver.class).setAction(action);
        PendingIntent p = PendingIntent.getBroadcast(c, action.hashCode(), i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(c, CHANNEL_ID)
                : new Notification.Builder(c);
        b.setSmallIcon(R.drawable.xcore_logo)
                .setContentTitle("XCORE • Motor de Suporte")
                .setContentText(status)
                .setOngoing(running)
                .setContentIntent(mainActivityPendingIntent(c))
                .setOnlyAlertOnce(true)
                .setAutoCancel(!running)
                .setPriority(Notification.PRIORITY_LOW)
                .addAction(new Notification.Action.Builder(null, actionText, p).build());
        if (Build.VERSION.SDK_INT >= 31) b.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        return b.build();
    }

    private static PendingIntent mainActivityPendingIntent(Context c) {
        Intent i = new Intent(c, com.xcore.app.MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(c, 4202, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static void showStopped(Context c) {
        ensureChannel(c);
        NotificationManager nm = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIFICATION_ID,
                build(c, false, "Motor parado • toque para ativar", ACTION_ACTIVATE, "ATIVAR MOTOR"));
    }

    public static void cancel(Context c) {
        NotificationManager nm = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(NOTIFICATION_ID);
    }
}
