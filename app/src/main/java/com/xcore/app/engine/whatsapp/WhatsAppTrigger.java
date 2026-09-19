package com.xcore.app.engine.whatsapp;

import java.util.Locale;
import java.util.UUID;

/**
 * Regra configurável que decide quando uma mensagem deve iniciar um fluxo.
 */
public final class WhatsAppTrigger {

    public enum MatchType {
        EXACT,
        CONTAINS,
        STARTS_WITH,
        KEYWORD
    }

    private final String id;
    private final String name;
    private final MatchType matchType;
    private final String pattern;
    private final String flowId;
    private boolean active;

    public WhatsAppTrigger(String name, MatchType matchType, String pattern, String flowId, boolean active) {
        this(UUID.randomUUID().toString(), name, matchType, pattern, flowId, active);
    }

    public WhatsAppTrigger(String id, String name, MatchType matchType, String pattern,
                            String flowId, boolean active) {
        this.id = id;
        this.name = name == null ? "" : name.trim();
        this.matchType = matchType == null ? MatchType.CONTAINS : matchType;
        this.pattern = pattern == null ? "" : pattern.trim();
        this.flowId = flowId == null ? "" : flowId.trim();
        this.active = active;
    }

    public boolean matches(String message) {
        if (!active || pattern.isEmpty() || message == null) return false;

        String value = normalize(message);
        String target = normalize(pattern);

        switch (matchType) {
            case EXACT:
                return value.equals(target);
            case STARTS_WITH:
                return value.startsWith(target);
            case KEYWORD:
                for (String word : value.split("\\s+")) {
                    if (word.equals(target)) return true;
                }
                return false;
            case CONTAINS:
            default:
                return value.contains(target);
        }
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public MatchType getMatchType() { return matchType; }
    public String getPattern() { return pattern; }
    public String getFlowId() { return flowId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
