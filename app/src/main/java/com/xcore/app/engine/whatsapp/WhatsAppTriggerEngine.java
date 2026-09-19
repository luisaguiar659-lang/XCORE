package com.xcore.app.engine.whatsapp;

/**
 * Motor de atendimento: identifica comandos, mantém o estado da conversa
 * e executa o fluxo configurado.
 */
public final class WhatsAppTriggerEngine implements WhatsAppEngine {

    private final WhatsAppTriggerRepository triggerRepository;
    private final WhatsAppFlowRouter flowRouter;
    private final WhatsAppConversationStore conversationStore;
    private final WhatsAppTransport transport;
    private boolean running;

    public WhatsAppTriggerEngine(WhatsAppTriggerRepository triggerRepository,
                                 WhatsAppFlowRouter flowRouter) {
        this(triggerRepository, flowRouter, null, null);
    }

    public WhatsAppTriggerEngine(WhatsAppTriggerRepository triggerRepository,
                                 WhatsAppFlowRouter flowRouter,
                                 WhatsAppConversationStore conversationStore,
                                 WhatsAppTransport transport) {
        if (triggerRepository == null) {
            throw new IllegalArgumentException("triggerRepository não pode ser nulo");
        }
        if (flowRouter == null) {
            throw new IllegalArgumentException("flowRouter não pode ser nulo");
        }
        this.triggerRepository = triggerRepository;
        this.flowRouter = flowRouter;
        this.conversationStore = conversationStore;
        this.transport = transport;
    }

    @Override
    public synchronized WhatsAppResult start() {
        running = true;
        if (transport != null) {
            WhatsAppResult result = transport.start();
            if (!result.isSuccess()) return result;
        }
        return WhatsAppResult.success("Motor de atendimento WhatsApp iniciado", null);
    }

    @Override
    public synchronized WhatsAppResult stop() {
        running = false;
        if (transport != null) transport.stop();
        return WhatsAppResult.success("Motor de atendimento WhatsApp parado", null);
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }

    @Override
    public WhatsAppResult receive(WhatsAppMessage message) {
        if (!running) return WhatsAppResult.error("Motor WhatsApp não está iniciado");
        if (message == null) return WhatsAppResult.error("Mensagem não informada");
        if (message.getPhone() == null || message.getPhone().trim().isEmpty()) {
            return WhatsAppResult.error("Mensagem sem número de telefone");
        }

        String phone = message.getPhone().trim();

        /*
         * Um novo comando sempre tem prioridade sobre uma conversa existente.
         * Isso permite enviar o mesmo comando novamente e reiniciar o fluxo,
         * em vez de tratá-lo como uma resposta da etapa anterior.
         */
        WhatsAppTrigger trigger = triggerRepository.findMatch(message.getText());
        if (trigger != null) {
            return executeTrigger(phone, message, trigger);
        }

        WhatsAppConversation conversation =
                conversationStore == null ? null : conversationStore.get(phone);

        /*
         * Se não for um comando novo e já existir uma conversa ativa,
         * a mensagem é tratada como resposta da etapa atual.
         */
        if (conversation != null) {
            WhatsAppResult continued = flowRouter.execute(conversation.getFlowId(), message);
            if (continued.isSuccess()) {
                saveNextStep(conversation, message);
            }
            return continued;
        }

        // Sem comando correspondente e sem conversa ativa: ignora completamente.
        if (trigger == null) {
            return WhatsAppResult.success("Nenhum comando correspondente", null);
        }

        return executeTrigger(phone, message, trigger);
    }

    private WhatsAppResult executeTrigger(String phone, WhatsAppMessage message, WhatsAppTrigger trigger) {
        // Um comando pode ser apenas uma resposta automática. Nesse caso
        // não existe flowId e isso não deve impedir o envio da resposta.
        if (trigger.getFlowId().isEmpty() && trigger.getResponse().trim().isEmpty()) {
            return WhatsAppResult.error("Comando sem ação configurada: " + trigger.getName());
        }

        if (conversationStore != null) {
            conversationStore.save(new WhatsAppConversation(
                    phone, trigger.getId(), trigger.getFlowId(),
                    System.currentTimeMillis(), message.getText(), 1
            ));
        }

        if (!trigger.getResponse().trim().isEmpty()) {
            if (transport == null) {
                return WhatsAppResult.error("Comando encontrado, mas o transporte WhatsApp não está configurado");
            }
            return transport.sendText(phone, trigger.getResponse());
        }

        return flowRouter.execute(trigger.getFlowId(), message);
    }

    private void saveNextStep(WhatsAppConversation conversation, WhatsAppMessage message) {
        if (conversationStore == null) return;
        conversationStore.save(new WhatsAppConversation(
                conversation.getPhone(),
                conversation.getCommandId(),
                conversation.getFlowId(),
                System.currentTimeMillis(),
                message.getText(),
                conversation.getStep() + 1
        ));
    }

    @Override
    public WhatsAppResult sendText(String phone, String text) {
        if (transport == null) {
            return WhatsAppResult.error("Transporte WhatsApp não configurado");
        }
        return transport.sendText(phone, text);
    }
}
