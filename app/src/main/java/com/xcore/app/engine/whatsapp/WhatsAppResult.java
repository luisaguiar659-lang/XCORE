package com.xcore.app.engine.whatsapp;

/**
 * Resultado padronizado das operações do motor WhatsApp.
 */
public final class WhatsAppResult {
    private final boolean success;
    private final String message;
    private final String externalId;

    private WhatsAppResult(boolean success, String message, String externalId) {
        this.success = success;
        this.message = message;
        this.externalId = externalId;
    }

    public static WhatsAppResult success(String message, String externalId) {
        return new WhatsAppResult(true, message, externalId);
    }

    public static WhatsAppResult error(String message) {
        return new WhatsAppResult(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getExternalId() { return externalId; }
}
