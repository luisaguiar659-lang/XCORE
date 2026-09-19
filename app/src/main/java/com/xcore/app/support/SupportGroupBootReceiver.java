package com.xcore.app.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SupportGroupBootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        for (SupportGroup group : new SupportGroupStore(context).list()) {
            if (group.isActive()) SupportGroupScheduler.schedule(context, group);
        }
    }
}
