package com.xcore.app.engine.whatsapp;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

/**
 * Persistência local da etapa de atendimento por telefone.
 */
public final class AndroidWhatsAppConversationStore implements WhatsAppConversationStore {
    private static final String PREFS = "xcore_whatsapp_conversations";
    private final SharedPreferences preferences;

    public AndroidWhatsAppConversationStore(Context context) {
        if (context == null) throw new IllegalArgumentException("context não pode ser nulo");
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    @Override
    public synchronized WhatsAppConversation get(String phone) {
        if (phone == null || phone.trim().isEmpty()) return null;
        String raw = preferences.getString(key(phone), null);
        if (raw == null) return null;
        try {
            JSONObject o = new JSONObject(raw);
            return new WhatsAppConversation(
                    phone,
                    o.optString("commandId"),
                    o.optString("flowId"),
                    o.optLong("updatedAt"),
                    o.optString("lastInboundText"),
                    o.optInt("step", 0)
            );
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public synchronized void save(WhatsAppConversation conversation) {
        if (conversation == null || conversation.getPhone().trim().isEmpty()) return;
        try {
            JSONObject o = new JSONObject();
            o.put("commandId", conversation.getCommandId());
            o.put("flowId", conversation.getFlowId());
            o.put("updatedAt", conversation.getUpdatedAt());
            o.put("lastInboundText", conversation.getLastInboundText());
            o.put("step", conversation.getStep());
            preferences.edit().putString(key(conversation.getPhone()), o.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    @Override
    public synchronized void remove(String phone) {
        if (phone != null) preferences.edit().remove(key(phone)).apply();
    }

    @Override
    public synchronized void clear() {
        preferences.edit().clear().apply();
    }

    private static String key(String phone) {
        return "conversation_" + phone.trim();
    }
}
