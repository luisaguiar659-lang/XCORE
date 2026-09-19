package com.xcore.app.engine.whatsapp;

/**
 * Roteia um gatilho para o fluxo que será executado por outro módulo.
 *
 * O WhatsApp não conhece a implementação do Masterflix, Master XCloud
 * ou Master IBO. Ele apenas entrega o identificador do fluxo acionado.
 */
public interface WhatsAppFlowRouter {

    /**
     * Executa o fluxo associado ao gatilho.
     *
     * @param flowId identificador configurado no gatilho
     * @param message mensagem original recebida
     */
    WhatsAppResult execute(String flowId, WhatsAppMessage message);
}
