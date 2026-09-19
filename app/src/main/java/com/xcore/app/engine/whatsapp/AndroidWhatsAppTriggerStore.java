package com.xcore.app.engine.whatsapp;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Persistência Android dos gatilhos do WhatsApp.
 *
 * Nenhum gatilho é criado automaticamente: somente regras salvas pelo
 * usuário podem acionar um fluxo.
 */
public final class AndroidWhatsAppTriggerStore {

    private static final String PREFS = "xcore_whatsapp_triggers";
    private static final String KEY_TRIGGERS = "triggers";

    private final SharedPreferences preferences;

    public AndroidWhatsAppTriggerStore(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context não pode ser nulo");
        }
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public synchronized void add(WhatsAppTrigger trigger) {
        if (trigger == null) return;

        List<WhatsAppTrigger> current = all();
        current.removeIf(item -> item.getId().equals(trigger.getId()));
        current.add(trigger);
        save(current);
    }

    public synchronized void remove(String id) {
        if (id == null) return;

        List<WhatsAppTrigger> current = all();
        current.removeIf(item -> id.equals(item.getId()));
        save(current);
    }

    public synchronized List<WhatsAppTrigger> all() {
        String raw = preferences.getString(KEY_TRIGGERS, "[]");
        List<WhatsAppTrigger> result = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);

                WhatsAppTrigger.MatchType type;
                try {
                    type = WhatsAppTrigger.MatchType.valueOf(
                            item.optString("matchType", "CONTAINS")
                    );
                } catch (IllegalArgumentException e) {
                    type = WhatsAppTrigger.MatchType.CONTAINS;
                }

                result.add(new WhatsAppTrigger(
                        item.optString("id"),
                        item.optString("name"),
                        type,
                        item.optString("pattern"),
                        item.optString("flowId"),
                        item.optBoolean("active", false)
                ));
            }
        } catch (Exception ignored) {
            return Collections.emptyList();
        }

        return result;
    }

    public synchronized WhatsAppTrigger findMatch(String message) {
        for (WhatsAppTrigger trigger : all()) {
            if (trigger.matches(message)) return trigger;
        }
        return null;
    }

    public synchronized void clear() {
        preferences.edit().remove(KEY_TRIGGERS).apply();
    }

    private void save(List<WhatsAppTrigger> triggers) {
        JSONArray array = new JSONArray();

        for (WhatsAppTrigger trigger : triggers) {
            try {
                JSONObject item = new JSONObject();
                item.put("id", trigger.getId());
                item.put("name", trigger.getName());
                item.put("matchType", trigger.getMatchType().name());
                item.put("pattern", trigger.getPattern());
                item.put("flowId", trigger.getFlowId());
                item.put("active", trigger.isActive());
                array.put(item);
            } catch (Exception ignored) {
                // Uma regra inválida não impede a persistência das demais.
            }
        }

        preferences.edit().putString(KEY_TRIGGERS, array.toString()).apply();
    }
}
