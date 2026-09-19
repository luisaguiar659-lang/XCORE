package com.xcore.app.engine.whatsapp;

import java.util.List;

/**
 * Contrato de armazenamento dos gatilhos.
 */
public interface WhatsAppTriggerRepository {
    void add(WhatsAppTrigger trigger);
    void remove(String id);
    List<WhatsAppTrigger> all();
    void clear();
    WhatsAppTrigger findMatch(String message);
}
