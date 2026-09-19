package com.xcore.app.engine.whatsapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Armazenamento em memória das regras do motor.
 */
public final class WhatsAppTriggerStore implements WhatsAppTriggerRepository {

    private final List<WhatsAppTrigger> triggers = new ArrayList<>();

    @Override
    public synchronized void add(WhatsAppTrigger trigger) {
        if (trigger == null) return;
        remove(trigger.getId());
        triggers.add(trigger);
    }

    @Override
    public synchronized void remove(String id) {
        if (id == null) return;
        triggers.removeIf(trigger -> id.equals(trigger.getId()));
    }

    @Override
    public synchronized List<WhatsAppTrigger> all() {
        return Collections.unmodifiableList(new ArrayList<>(triggers));
    }

    @Override
    public synchronized void clear() {
        triggers.clear();
    }

    @Override
    public synchronized WhatsAppTrigger findMatch(String message) {
        for (WhatsAppTrigger trigger : triggers) {
            if (trigger.matches(message)) return trigger;
        }
        return null;
    }
}
