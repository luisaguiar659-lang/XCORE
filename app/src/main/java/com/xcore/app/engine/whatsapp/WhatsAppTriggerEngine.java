package com.xcore.app.engine.whatsapp;

/**
 * Motor responsável por receber mensagens, encontrar gatilhos ativos
 * e encaminhar o fluxo correspondente.
 */
public final class WhatsAppTriggerEngine implements WhatsAppEngine {

    private final WhatsAppTriggerRepository triggerRepository;
    private final WhatsAppFlowRouter flowRouter;
    private boolean running;

    public WhatsAppTriggerEngine(WhatsAppTriggerRepository triggerRepository,
                                 WhatsAppFlowRouter flowRouter) {
        if (triggerRepository == null) {
            throw new IllegalArgumentException("triggerRepository não pode ser nulo");
        }
        if (flowRouter == null) {
            throw new IllegalArgumentException("flowRouter não pode ser nulo");
        }
        this.triggerRepository = triggerRepository;
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

        WhatsAppTrigger trigger = triggerRepository.findMatch(message.getText());

        // Sem gatilho correspondente: nenhuma resposta e nenhum módulo é executado.
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
                "Envio WhatsApp depende do conector oficial do WhatsApp Business"
        );
    }
}
