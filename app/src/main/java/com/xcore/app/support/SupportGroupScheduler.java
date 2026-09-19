package com.xcore.app.support;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public final class SupportGroupScheduler {
    private SupportGroupScheduler(){}
    public static void schedule(Context c, SupportGroup g){
        cancel(c,g.getId()); if(!g.isActive())return;
        long minutes=g.getUnit()==SupportGroup.Unit.HOURS?g.getInterval()*60L:g.getInterval();
        long ms=Math.max(60L*1000L,minutes*60L*1000L);
        Intent i=new Intent(c,SupportGroupAlarmReceiver.class).setAction("com.xcore.app.SUPPORT_NOTICE").putExtra("group_id",g.getId());
        PendingIntent p=PendingIntent.getBroadcast(c,stable(g.getId()),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        if(a!=null)a.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime()+ms,ms,p);
    }
    public static void cancel(Context c,String id){
        Intent i=new Intent(c,SupportGroupAlarmReceiver.class).setAction("com.xcore.app.SUPPORT_NOTICE");
        PendingIntent p=PendingIntent.getBroadcast(c,stable(id),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE); if(a!=null)a.cancel(p); p.cancel();
    }
    private static int stable(String id){return id==null?0:id.hashCode();}
}
