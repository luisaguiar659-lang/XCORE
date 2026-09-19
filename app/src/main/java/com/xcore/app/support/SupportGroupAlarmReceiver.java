package com.xcore.app.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SupportGroupAlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i){
        String id=i==null?null:i.getStringExtra("group_id"); if(id==null)return;
        for(SupportGroup g:new SupportGroupStore(c).list()){
            if(id.equals(g.getId())&&g.isActive()){SupportNoticeLauncher.launch(c,g);return;}
        }
    }
}
