package com.xcore.app.engine.whatsapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Armazenamento em memória das regras do motor.
 *
 * A camada foi isolada para que a persistência do Android possa ser ligada
 * posteriormente sem alterar o roteador de mensagens.
 */
public final class WhatsAppTriggerStore {

    private final List<WhatsAppTrigger> triggers = new ArrayList<>();

    public synchronized void add(WhatsAppTrigger trigger) {
        if (trigger == null) return;
        remove(trigger.getId());
        triggers.add(trigger);
    }

    public synchronized void remove(String id) {
        if (id == null) return;
        triggers.removeIf(trigger -> id.equals(trigger.getId()));
    }

    public synchronized List<WhatsAppTrigger> all() {
        return Collections.unmodifiableList(new ArrayList<>(triggers));
    }

    public synchronized void clear() {
        triggers.clear();
    }

    public synchronized WhatsAppTrigger findMatch(String message) {
        for (WhatsAppTrigger trigger : triggers) {
            if (trigger.matches(message)) return trigger;
        }
        return null;
    }
}
