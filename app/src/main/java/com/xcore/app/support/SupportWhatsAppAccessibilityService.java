package com.xcore.app.support;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

public class SupportWhatsAppAccessibilityService extends AccessibilityService {
    private static SupportWhatsAppAccessibilityService instance;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SupportGroup pending;
    private int step = 0;
    private boolean sendMessage = true;

    public static boolean isRunning() { return instance != null; }

    public static void openGroup(android.content.Context context, SupportGroup group) {
        if (instance != null) instance.begin(group, false);
        else Toast.makeText(context, "Ative o Acesso de acessibilidade do XCORE.", Toast.LENGTH_LONG).show();
    }

    public static void send(android.content.Context context, SupportGroup group) {
        if (instance != null) instance.begin(group, true);
        else Toast.makeText(context, "Ative o Acesso de acessibilidade do XCORE.", Toast.LENGTH_LONG).show();
    }

    @Override protected void onServiceConnected() { super.onServiceConnected(); instance = this; }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (pending == null) return;
        handler.postDelayed(() -> advance(), 250);
    }
    @Override public void onInterrupt() { pending = null; step = 0; }
    @Override public void onDestroy() { if (instance == this) instance = null; handler.removeCallbacksAndMessages(null); super.onDestroy(); }

    private void begin(SupportGroup group, boolean shouldSend) {
        pending = group;
        sendMessage = shouldSend;
        step = 0;
        Intent launch = getPackageManager().getLaunchIntentForPackage("com.whatsapp.w4b");
        if (launch == null) launch = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
        if (launch == null) {
            Toast.makeText(this, "WhatsApp não está instalado.", Toast.LENGTH_LONG).show();
            pending = null;
            return;
        }
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(launch);
        handler.postDelayed(() -> advance(), 900);
    }

    private void advance() {
        if (pending == null) return;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) { retry(); return; }

        if (step == 0) {
            AccessibilityNodeInfo search = findByTextOrDescription(root, "Pesquisar", "Search");
            if (search == null) search = findByIdSuffix(root, "search");
            if (search != null && click(search)) { step = 1; retryLater(); return; }
        } else if (step == 1) {
            if (setText(root, pending.getGroupName())) { step = 2; retryLater(); return; }
        } else if (step == 2) {
            AccessibilityNodeInfo group = findByText(root, pending.getGroupName());
            if (group != null && click(group)) { step = 3; retryLater(); return; }
        } else if (step == 3) {
            if (!sendMessage) { pending = null; step = 0; return; }
            AccessibilityNodeInfo input = findByHint(root, "Digite uma mensagem", "Type a message");
            if (input == null) input = findByIdSuffix(root, "entry");
            if (input != null && setText(input, pending.getMessage())) { step = 4; retryLater(); return; }
        } else if (step == 4) {
            AccessibilityNodeInfo send = findByDescription(root, "Enviar", "Send");
            if (send != null && click(send)) {
                Toast.makeText(this, "Aviso enviado para " + pending.getGroupName(), Toast.LENGTH_SHORT).show();
                pending = null; step = 0; return;
            }
        }
        retry();
    }

    private void retry() { retryLater(); }
    private void retryLater() { handler.postDelayed(() -> advance(), 700); }

    private AccessibilityNodeInfo findByTextOrDescription(AccessibilityNodeInfo r,String... v){ AccessibilityNodeInfo n=findByText(r,v); return n!=null?n:findByDescription(r,v); }
    private AccessibilityNodeInfo findByText(AccessibilityNodeInfo r,String... values){ if(r==null)return null; CharSequence t=r.getText(); if(t!=null)for(String v:values)if(t.toString().equalsIgnoreCase(v))return r; for(int i=0;i<r.getChildCount();i++){AccessibilityNodeInfo n=findByText(r.getChild(i),values);if(n!=null)return n;} return null; }
    private AccessibilityNodeInfo findByDescription(AccessibilityNodeInfo r,String... values){ if(r==null)return null; CharSequence t=r.getContentDescription(); if(t!=null)for(String v:values)if(t.toString().equalsIgnoreCase(v))return r; for(int i=0;i<r.getChildCount();i++){AccessibilityNodeInfo n=findByDescription(r.getChild(i),values);if(n!=null)return n;} return null; }
    private AccessibilityNodeInfo findByHint(AccessibilityNodeInfo r,String... values){ if(r==null)return null; CharSequence t=r.getText(); if(t!=null)for(String v:values)if(t.toString().equalsIgnoreCase(v))return r; for(int i=0;i<r.getChildCount();i++){AccessibilityNodeInfo n=findByHint(r.getChild(i),values);if(n!=null)return n;} return null; }
    private AccessibilityNodeInfo findByIdSuffix(AccessibilityNodeInfo r,String suffix){ if(r==null)return null; String id=r.getViewIdResourceName(); if(id!=null&&id.endsWith(suffix))return r; for(int i=0;i<r.getChildCount();i++){AccessibilityNodeInfo n=findByIdSuffix(r.getChild(i),suffix);if(n!=null)return n;} return null; }
    private boolean setText(AccessibilityNodeInfo n,String value){ if(n==null)return false; if(n.isEditable()){ android.os.Bundle b=new android.os.Bundle(); b.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,value); return n.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,b); } return false; }
    private AccessibilityNodeInfo findEditable(AccessibilityNodeInfo r){ if(r==null)return null; if(r.isEditable())return r; for(int i=0;i<r.getChildCount();i++){AccessibilityNodeInfo n=findEditable(r.getChild(i));if(n!=null)return n;} return null; }
    private boolean click(AccessibilityNodeInfo n){ return n!=null&&(n.isClickable()?n.performAction(AccessibilityNodeInfo.ACTION_CLICK):n.getParent()!=null&&click(n.getParent())); }

}
