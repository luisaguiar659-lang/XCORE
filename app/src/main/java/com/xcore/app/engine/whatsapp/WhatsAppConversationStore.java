package com.xcore.app.engine.whatsapp;

/**
 * Armazena a etapa atual de cada conversa.
 */
public interface WhatsAppConversationStore {
    WhatsAppConversation get(String phone);
    void save(WhatsAppConversation conversation);
    void remove(String phone);
    void clear();
}
