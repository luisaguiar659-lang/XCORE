package com.xcore.app.engine.masterflix;

import android.content.Intent;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.xcore.app.engine.whatsapp.WhatsAppNotificationTransport;
import com.xcore.app.engine.whatsapp.WhatsAppNotificationListenerService;
import java.util.Locale;

public final class MasterflixWebAutomation {
    private static String pendingPhone;
    private static String pendingLabel;
    private static boolean running;
    private static WhatsAppNotificationTransport transport;

    private MasterflixWebAutomation() {}

    public static void request(String phone, String label, WhatsAppNotificationTransport replyTransport) {
        pendingPhone = phone;
        pendingLabel = label;
        transport = replyTransport;
        running = true;
    }

    public static boolean hasRequest() { return running; }

    public static void start(WebView webView, String label, String phone) {
        if (!running && (label == null || label.trim().isEmpty())) return;
        if (label != null && !label.trim().isEmpty()) pendingLabel = label;
        if (phone != null && !phone.trim().isEmpty()) pendingPhone = phone;
        if (!running) running = true;

        webView.addJavascriptInterface(new Bridge(webView), "XCORE");
        String wanted = pendingLabel == null || pendingLabel.trim().isEmpty()
                ? "MASTERFLIX TESTE COMPLETO 1H" : pendingLabel.trim();

        String js = "(function(){"
                + "if(window.__xcoreTimer)return;window.__xcoreTimer=setInterval(function(){"
                + "var all=[].slice.call(document.querySelectorAll('button,a,[role=button],div,span'));"
                + "var wanted=" + quote(wanted) + ";"
                + "var n=all.find(function(e){return (e.innerText||e.textContent||'').trim().toLowerCase()===wanted.toLowerCase();});"
                + "if(n){n.click();window.__xcoreStep=2;clearInterval(window.__xcoreTimer);"
                + "setTimeout(function(){window.XCORE.scan();},1800);}"
                + "},700);})();";
        webView.evaluateJavascript(js, null);
    }

    private static String quote(String value) {
        return "'" + value.replace("\\","\\\\").replace("'","\\'").replace("\n"," ") + "'";
    }

    private static final class Bridge {
        private final WebView webView;
        Bridge(WebView webView) { this.webView = webView; }

        @JavascriptInterface public void scan() {
            webView.postDelayed(() -> {
                String js = "(function(){"
                        + "var body=document.body?document.body.innerText:'';"
                        + "var copy=[].slice.call(document.querySelectorAll('button,a,[role=button]')).find(function(e){return /copiar/i.test((e.innerText||e.textContent||''));});"
                        + "if(copy)copy.click();"
                        + "return JSON.stringify({body:body});"
                        + "})()";
                webView.evaluateJavascript(js, value -> {
                    String decoded = decode(value);
                    if (decoded.length() > 30 && !looksLikeLogin(decoded)) {
                        MasterflixWebAutomation.deliver(webView, decoded);
                    } else {
                        webView.postDelayed(() -> scanAgain(webView), 1200);
                    }
                });
            }, 400);
        }

        private void scanAgain(WebView view) {
            scan();
        }

        private boolean looksLikeLogin(String value) {
            String v = value.toLowerCase(Locale.ROOT);
            return v.contains("usuário ou e-mail") && v.contains("senha");
        }

        private String decode(String value) {
            if (value == null) return "";
            String v = value;
            if (v.startsWith(""") && v.endsWith(""")) v = v.substring(1, v.length()-1);
            return v.replace("\n","\n").replace("\"", """).replace("\\","\\");
        }
    }

    private static void deliver(WebView webView, String text) {
        MasterflixActivity activity = null;
        if (webView.getContext() instanceof MasterflixActivity) {
            activity = (MasterflixActivity) webView.getContext();
        }
        if (activity != null) activity.finishAutomation("🎬 TESTE GERADO\n\n" + text, null);
    }

    public static void finishRequest() {
        running = false;
        pendingPhone = null;
        pendingLabel = null;
    }

    public static void sendToWhatsApp(String phone, String text) {
        if (transport != null && phone != null) transport.sendText(phone, text);
    }
}
