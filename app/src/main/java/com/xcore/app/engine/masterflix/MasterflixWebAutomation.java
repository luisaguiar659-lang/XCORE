package com.xcore.app.engine.masterflix;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.xcore.app.engine.whatsapp.WhatsAppNotificationTransport;
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

    public static boolean hasRequest() {
        return running;
    }

    public static void start(WebView webView, String label, String phone) {
        if (!running && (label == null || label.trim().isEmpty())) return;
        if (label != null && !label.trim().isEmpty()) pendingLabel = label;
        if (phone != null && !phone.trim().isEmpty()) pendingPhone = phone;
        running = true;

        webView.addJavascriptInterface(new Bridge(webView), "XCORE");

        final String wanted = pendingLabel == null || pendingLabel.trim().isEmpty()
                ? "MASTERFLIX TESTE COMPLETO 1H"
                : pendingLabel.trim();

        // Fluxo reproduzido do vídeo:
        // 1) Dashboard / Teste Rápido
        // 2) tocar no produto exato
        // 3) aguardar "Detalhes do Cliente"
        // 4) copiar o conteúdo e fechar a janela
        // 5) devolver o mesmo conteúdo ao cliente no WhatsApp.
        String js = "(function(){"
                + "if(window.__xcoreMasterflixStarted)return;"
                + "window.__xcoreMasterflixStarted=true;"
                + "var wanted=" + quote(wanted) + ";"
                + "var tries=0;"
                + "window.__xcoreMasterflixTimer=setInterval(function(){"
                + "tries++;"
                + "var all=[].slice.call(document.querySelectorAll('button,a,[role=button],div,span'));"
                + "var target=all.find(function(e){"
                + "var t=(e.innerText||e.textContent||'').trim();"
                + "return t.toLowerCase()===wanted.toLowerCase();"
                + "});"
                + "if(target){"
                + "clearInterval(window.__xcoreMasterflixTimer);"
                + "var clickable=target.closest('button,a,[role=button]')||target;"
                + "clickable.scrollIntoView({block:'center',behavior:'instant'});"
                + "setTimeout(function(){"
                + "clickable.dispatchEvent(new MouseEvent('click',{bubbles:true,cancelable:true,view:window}));"
                + "setTimeout(function(){window.XCORE.waitDetails();},900);"
                + "},250);"
                + "}"
                + "if(tries>45){clearInterval(window.__xcoreMasterflixTimer);window.XCORE.error('Não encontrei o teste '+wanted+'.');}"
                + "},500);"
                + "})();";

        webView.evaluateJavascript(js, null);
    }

    private static String quote(String value) {
        return "'" + value.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", " ") + "'";
    }

    private static final class Bridge {
        private final WebView webView;

        Bridge(WebView webView) {
            this.webView = webView;
        }

        @JavascriptInterface
        public void waitDetails() {
            webView.postDelayed(() -> {
                String js = "(function(){"
                        + "var body=document.body?document.body.innerText:'';"
                        + "var title=(document.body&&document.body.innerText||'');"
                        + "var details=/Detalhes do Cliente/i.test(title);"
                        + "var hasCredentials=/Usu[aá]rio:/i.test(title)&&/Senha:/i.test(title);"
                        + "var buttons=[].slice.call(document.querySelectorAll('button,a,[role=button]'));"
                        + "var closeCopy=buttons.find(function(e){return /Copiar\\s*e\\s*Fechar/i.test((e.innerText||e.textContent||''));});"
                        + "if(details&&hasCredentials){"
                        + "var result=JSON.stringify({ok:true,body:body});"
                        + "if(closeCopy){setTimeout(function(){closeCopy.click();},150);}"
                        + "return result;"
                        + "}"
                        + "return JSON.stringify({ok:false,body:body});"
                        + "})()";

                webView.evaluateJavascript(js, value -> {
                    String result = decode(value);
                    if (result.startsWith("{")) {
                        String body = extractJsonField(result, "body");
                        if (body.length() > 80 && containsCredentials(body)) {
                            deliver(webView, body);
                            return;
                        }
                    }
                    webView.postDelayed(this::waitDetails, 800);
                });
            }, 500);
        }

        @JavascriptInterface
        public void error(String error) {
            MasterflixWebAutomation.fail(error);
        }

        private boolean containsCredentials(String body) {
            String v = body.toLowerCase(Locale.ROOT);
            return v.contains("usuário:") && v.contains("senha:");
        }

        private String extractJsonField(String json, String key) {
            String marker = "\"" + key + "\":\"";
            int start = json.indexOf(marker);
            if (start < 0) return "";
            start += marker.length();
            int end = json.lastIndexOf("\"}");
            if (end < start) end = json.length();
            return json.substring(start, end)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }

        private String decode(String value) {
            if (value == null) return "";
            String v = value.trim();
            if (v.startsWith("\"") && v.endsWith("\"")) {
                v = v.substring(1, v.length() - 1);
            }
            return v.replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
    }

    private static void deliver(WebView webView, String text) {
        String message = "🎬 TESTE GERADO\n\n" + text;
        if (transport != null && pendingPhone != null && !pendingPhone.trim().isEmpty()) {
            transport.sendText(pendingPhone, message);
            transport.clearReplyTarget();
        }
        if (webView.getContext() instanceof MasterflixActivity) {
            ((MasterflixActivity) webView.getContext()).finishAutomation(message, null);
        } else {
            finishRequest();
        }
    }

    private static void fail(String error) {
        running = false;
        if (transport != null && pendingPhone != null) {
            transport.sendText(pendingPhone, "⚠️ Não foi possível gerar o teste no Masterflix: " + error);
            transport.clearReplyTarget();
        }
        pendingPhone = null;
        pendingLabel = null;
        transport = null;
    }

    public static void finishRequest() {
        running = false;
        pendingPhone = null;
        pendingLabel = null;
        transport = null;
    }

    public static void sendToWhatsApp(String phone, String text) {
        if (transport != null && phone != null) {
            transport.sendText(phone, text);
            transport.clearReplyTarget();
        }
    }
}
