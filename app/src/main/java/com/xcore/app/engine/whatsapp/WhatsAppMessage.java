package com.xcore.app.engine.whatsapp;

import java.util.Collections;
import java.util.Map;

/**
 * Mensagem recebida pelo motor WhatsApp.
 * Mantém o provedor isolado do restante do XCORE.
 */
public final class WhatsAppMessage {
    private final String messageId;
    private final String phone;
    private final String text;
    private final long timestamp;
    private final Map<String, String> metadata;

    public WhatsAppMessage(String messageId, String phone, String text, long timestamp) {
        this(messageId, phone, text, timestamp, Collections.emptyMap());
    }

    public WhatsAppMessage(String messageId, String phone, String text,
                           long timestamp, Map<String, String> metadata) {
        this.messageId = messageId;
        this.phone = phone;
        this.text = text == null ? "" : text;
        this.timestamp = timestamp;
        this.metadata = metadata == null ? Collections.emptyMap() : Collections.unmodifiableMap(metadata);
    }

    public String getMessageId() { return messageId; }
    public String getPhone() { return phone; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
    public Map<String, String> getMetadata() { return metadata; }
}
