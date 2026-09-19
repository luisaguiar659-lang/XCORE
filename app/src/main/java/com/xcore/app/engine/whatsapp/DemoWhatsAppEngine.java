package com.xcore.app.engine.whatsapp;

/**
 * Implementação local para testes do XCORE.
 * Não acessa WhatsApp nem serviços externos.
 */
public final class DemoWhatsAppEngine implements WhatsAppEngine {
    private boolean running;

    @Override
    public WhatsAppResult start() {
        running = true;
        return WhatsAppResult.success("Motor WhatsApp em modo DEMO", "demo");
    }

    @Override
    public WhatsAppResult stop() {
        running = false;
        return WhatsAppResult.success("Motor WhatsApp parado", null);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public WhatsAppResult receive(WhatsAppMessage message) {
        if (!running) {
            return WhatsAppResult.error("Motor WhatsApp não está iniciado");
        }
        if (message == null || message.getPhone() == null || message.getPhone().trim().isEmpty()) {
            return WhatsAppResult.error("Mensagem sem número de telefone");
        }
        return WhatsAppResult.success(
                "Mensagem recebida em modo DEMO",
                message.getMessageId()
        );
    }

    @Override
    public WhatsAppResult sendText(String phone, String text) {
        if (!running) {
            return WhatsAppResult.error("Motor WhatsApp não está iniciado");
        }
        if (phone == null || phone.trim().isEmpty()) {
            return WhatsAppResult.error("Número de telefone não informado");
        }
        if (text == null || text.trim().isEmpty()) {
            return WhatsAppResult.error("Mensagem não informada");
        }
        return WhatsAppResult.success("Mensagem preparada em modo DEMO", "demo-send");
    }
}
