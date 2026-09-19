package com.xcore.app.engine.whatsapp;

/**
 * Estado atual de uma conversa com um cliente.
 */
public final class WhatsAppConversation {
    private final String phone;
    private final String commandId;
    private final String flowId;
    private final long updatedAt;
    private final String lastInboundText;
    private final int step;

    public WhatsAppConversation(String phone, String commandId, String flowId,
                                long updatedAt, String lastInboundText, int step) {
        this.phone = phone == null ? "" : phone;
        this.commandId = commandId == null ? "" : commandId;
        this.flowId = flowId == null ? "" : flowId;
        this.updatedAt = updatedAt;
        this.lastInboundText = lastInboundText == null ? "" : lastInboundText;
        this.step = Math.max(0, step);
    }

    public String getPhone() { return phone; }
    public String getCommandId() { return commandId; }
    public String getFlowId() { return flowId; }
    public long getUpdatedAt() { return updatedAt; }
    public String getLastInboundText() { return lastInboundText; }
    public int getStep() { return step; }
}
