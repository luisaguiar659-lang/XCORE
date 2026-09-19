package com.xcore.app.engine.whatsapp;

/**
 * Contrato do motor WhatsApp do XCORE.
 *
 * A implementação real do provedor será conectada aqui quando
 * tivermos a API/endpoints oficiais. Nenhuma credencial fica no código.
 */
public interface WhatsAppEngine {

    /** Inicializa o motor. */
    WhatsAppResult start();

    /** Encerra o motor e libera recursos. */
    WhatsAppResult stop();

    /** Indica se o motor está operacional. */
    boolean isRunning();

    /** Processa uma mensagem recebida e devolve o resultado do fluxo. */
    WhatsAppResult receive(WhatsAppMessage message);

    /** Envia uma mensagem para o número informado. */
    WhatsAppResult sendText(String phone, String text);
}
