package com.xcore.app.engine.whatsapp;

/**
 * Motor responsável exclusivamente por receber mensagens, encontrar gatilhos
 * ativos e encaminhar o fluxo correspondente.
 */
public final class WhatsAppTriggerEngine implements WhatsAppEngine {

    private final WhatsAppTriggerStore triggerStore;
    private final WhatsAppFlowRouter flowRouter;
    private boolean running;

    public WhatsAppTriggerEngine(WhatsAppTriggerStore triggerStore, WhatsAppFlowRouter flowRouter) {
        if (triggerStore == null) {
            throw new IllegalArgumentException("triggerStore não pode ser nulo");
        }
        if (flowRouter == null) {
            throw new IllegalArgumentException("flowRouter não pode ser nulo");
        }
        this.triggerStore = triggerStore;
        this.flowRouter = flowRouter;
    }

    @Override
    public synchronized WhatsAppResult start() {
        running = true;
        return WhatsAppResult.success("Motor de gatilhos WhatsApp iniciado", null);
    }

    @Override
    public synchronized WhatsAppResult stop() {
        running = false;
        return WhatsAppResult.success("Motor de gatilhos WhatsApp parado", null);
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }

    @Override
    public WhatsAppResult receive(WhatsAppMessage message) {
        if (!running) {
            return WhatsAppResult.error("Motor WhatsApp não está iniciado");
        }
        if (message == null) {
            return WhatsAppResult.error("Mensagem não informada");
        }
        if (message.getPhone() == null || message.getPhone().trim().isEmpty()) {
            return WhatsAppResult.error("Mensagem sem número de telefone");
        }

        WhatsAppTrigger trigger = triggerStore.findMatch(message.getText());

        // Nenhum gatilho: não responde e não executa nenhum módulo.
        if (trigger == null) {
            return WhatsAppResult.success("Nenhum gatilho correspondente", null);
        }

        if (trigger.getFlowId().isEmpty()) {
            return WhatsAppResult.error("Gatilho sem fluxo configurado: " + trigger.getName());
        }

        return flowRouter.execute(trigger.getFlowId(), message);
    }

    @Override
    public WhatsAppResult sendText(String phone, String text) {
        return WhatsAppResult.error(
                "Envio WhatsApp ainda depende do conector oficial do WhatsApp Business"
        );
    }
}
