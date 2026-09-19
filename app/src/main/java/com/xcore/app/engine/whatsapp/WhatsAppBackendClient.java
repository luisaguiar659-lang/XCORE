package com.xcore.app.engine.whatsapp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Cliente HTTP mínimo para sincronizar comandos do app com o backend XCORE.
 * Não armazena o token do WhatsApp; usa apenas a chave do backend.
 */
public final class WhatsAppBackendClient {
    private final String baseUrl;
    private final String apiKey;

    public WhatsAppBackendClient(String baseUrl, String apiKey) {
        this.baseUrl = trimUrl(baseUrl);
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public boolean isConfigured() {
        return !baseUrl.isEmpty() && !apiKey.isEmpty();
    }

    public String health() throws Exception {
        return request("/health", "GET", null, false);
    }

    public String upsertCommand(WhatsAppTrigger trigger) throws Exception {
        if (trigger == null) throw new IllegalArgumentException("Comando não informado");
        String json = "{"
                + "\"id\":\"" + esc(trigger.getId()) + "\","
                + "\"name\":\"" + esc(trigger.getName()) + "\","
                + "\"matchType\":\"" + trigger.getMatchType().name() + "\","
                + "\"pattern\":\"" + esc(trigger.getPattern()) + "\","
                + "\"flowId\":\"" + esc(trigger.getFlowId()) + "\","
                + "\"response\":\"" + esc(trigger.getResponse()) + "\","
                + "\"question\":\"" + esc(trigger.getQuestion()) + "\","
                + "\"active\":" + trigger.isActive()
                + "}";
        return request("/api/commands", "POST", json, true);
    }

    public String deleteCommand(String id) throws Exception {
        return request("/api/commands/" + java.net.URLEncoder.encode(id, "UTF-8"), "DELETE", null, true);
    }

    public String listCommands() throws Exception {
        return request("/api/commands", "GET", null, true);
    }

    private String request(String path, String method, String body, boolean auth) throws Exception {
        if (baseUrl.isEmpty()) throw new IllegalStateException("Backend XCORE não configurado");
        URL url = new URL(baseUrl + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.setRequestProperty("Accept", "application/json");
        if (auth) connection.setRequestProperty("X-XCORE-API-KEY", apiKey);
        if (body != null) {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
            try (OutputStream out = connection.getOutputStream()) {
                out.write(bytes);
            }
        }
        int status = connection.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                status >= 400 ? connection.getErrorStream() : connection.getInputStream(),
                StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) result.append(line);
        if (status >= 400) throw new IllegalStateException("Backend HTTP " + status + ": " + result);
        return result.toString();
    }

    private static String trimUrl(String value) {
        if (value == null) return "";
        String v = value.trim();
        while (v.endsWith("/")) v = v.substring(0, v.length() - 1);
        return v;
    }

    private static String esc(String value) {
        if (value == null) return "";
        return value.replace("\\\\", "\\\\\\\\")
                .replace("\"", "\\\"");
    }
}
