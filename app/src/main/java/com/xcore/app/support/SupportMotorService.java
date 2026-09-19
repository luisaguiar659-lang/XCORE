package com.xcore.app.support;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupportMotorService extends Service {
    private static SupportMotorService instance;

    public static boolean isRunning() { return instance != null; }
    public static final String ACTION_START = "com.xcore.app.support.START_MOTOR";
    private static final long LOOP_MS = 1000L;
    private final Handler handler = new Handler();
    private final Map<String, Long> nextRun = new HashMap<>();
    private final Runnable loop = new Runnable() {
        @Override public void run() {
            tick();
            handler.postDelayed(this, LOOP_MS);
        }
    };

    @Override public void onCreate() {
        super.onCreate();
        instance = this;
        SupportMotorNotification.ensureChannel(this);
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(SupportMotorNotification.NOTIFICATION_ID,
                    SupportMotorNotification.build(this, true, "Motor ativo • preparando os grupos",
                            SupportMotorNotification.ACTION_PAUSE, "PAUSAR MOTOR"),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(SupportMotorNotification.NOTIFICATION_ID,
                    SupportMotorNotification.build(this, true, "Motor ativo • preparando os grupos",
                            SupportMotorNotification.ACTION_PAUSE, "PAUSAR MOTOR"));
        }
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (nextRun.isEmpty()) {
            long now = SystemClock.elapsedRealtime();
            for (SupportGroup g : new SupportGroupStore(this).list()) {
                if (g.isActive()) nextRun.put(g.getId(), now + intervalMs(g));
            }
        }
        handler.removeCallbacks(loop);
        handler.post(loop);
        return START_STICKY;
    }

    private void tick() {
        long now = SystemClock.elapsedRealtime();
        List<SupportGroup> groups = new SupportGroupStore(this).list();
        for (SupportGroup g : groups) {
            if (!g.isActive()) {
                nextRun.remove(g.getId());
                continue;
            }
            Long due = nextRun.get(g.getId());
            if (due == null) {
                nextRun.put(g.getId(), now + intervalMs(g));
                continue;
            }
            if (now >= due) {
                SupportNoticeLauncher.launch(this, g);
                nextRun.put(g.getId(), now + intervalMs(g));
            }
        }
        updateNotification(groups, now);
    }

    private void updateNotification(List<SupportGroup> groups, long now) {
        long nearest = Long.MAX_VALUE;
        int active = 0;
        for (SupportGroup g : groups) {
            if (!g.isActive()) continue;
            active++;
            Long d = nextRun.get(g.getId());
            if (d != null && d < nearest) nearest = d;
        }
        String status;
        if (active == 0) status = "Motor ativo • nenhum grupo ativo";
        else if (nearest == Long.MAX_VALUE) status = "Motor ativo • " + active + " grupo(s)";
        else {
            long seconds = Math.max(0L, (nearest - now) / 1000L);
            status = "Motor ativo • " + active + " grupo(s) • próximo em " + seconds + "s";
        }
        android.app.NotificationManager nm = (android.app.NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(SupportMotorNotification.NOTIFICATION_ID,
                SupportMotorNotification.build(this, true, status,
                        SupportMotorNotification.ACTION_PAUSE, "PAUSAR MOTOR"));
    }

    private long intervalMs(SupportGroup g) {
        long minutes = g.getUnit() == SupportGroup.Unit.HOURS ? g.getInterval() * 60L : g.getInterval();
        return Math.max(60_000L, minutes * 60_000L);
    }

    @Override public void onDestroy() {
        if (instance == this) instance = null;
        handler.removeCallbacksAndMessages(null);
        nextRun.clear();
        SupportMotorNotification.showStopped(this);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
