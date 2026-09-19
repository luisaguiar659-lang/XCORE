package com.xcore.app.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class SupportMotorActionReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        if (SupportMotorNotification.ACTION_ACTIVATE.equals(intent.getAction())) {
            Intent service = new Intent(context, SupportMotorService.class).setAction(SupportMotorService.ACTION_START);
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service);
            else context.startService(service);
        } else if (SupportMotorNotification.ACTION_PAUSE.equals(intent.getAction())) {
            context.stopService(new Intent(context, SupportMotorService.class));
            SupportMotorNotification.showStopped(context);
        }
    }
}
