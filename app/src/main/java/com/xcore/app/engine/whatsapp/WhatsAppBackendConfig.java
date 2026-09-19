package com.xcore.app.engine.whatsapp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Configuração local do backend XCORE.
 * Acesso token do WhatsApp nunca é armazenado aqui.
 */
public final class WhatsAppBackendConfig {
    private static final String PREFS = "xcore_whatsapp_backend";
    private static final String URL = "backend_url";
    private static final String KEY = "backend_api_key";

    private final SharedPreferences prefs;

    public WhatsAppBackendConfig(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String getUrl() {
        return prefs.getString(URL, "");
    }

    public String getApiKey() {
        return prefs.getString(KEY, "");
    }

    public void save(String url, String apiKey) {
        prefs.edit()
                .putString(URL, url == null ? "" : url.trim())
                .putString(KEY, apiKey == null ? "" : apiKey.trim())
                .apply();
    }

    public WhatsAppBackendClient client() {
        return new WhatsAppBackendClient(getUrl(), getApiKey());
    }

    public boolean isConfigured() {
        return client().isConfigured();
    }
}
