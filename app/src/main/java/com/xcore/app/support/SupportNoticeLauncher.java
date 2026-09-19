package com.xcore.app.support;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public final class SupportNoticeLauncher {
    private SupportNoticeLauncher(){}
    public static void launch(Context c,SupportGroup g){
        try{
            Intent i=new Intent(Intent.ACTION_SEND);
            i.setType("text/plain"); i.putExtra(Intent.EXTRA_TEXT,g.getMessage());
            i.setPackage("com.whatsapp.w4b"); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(i);
        }catch(Exception e){
            try{
                Intent i=new Intent(Intent.ACTION_SEND);
                i.setType("text/plain"); i.putExtra(Intent.EXTRA_TEXT,g.getMessage());
                i.setPackage("com.whatsapp"); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(i);
            }catch(Exception ignored){
                Toast.makeText(c,"WhatsApp não encontrado para o aviso.",Toast.LENGTH_LONG).show();
            }
        }
    }
}
