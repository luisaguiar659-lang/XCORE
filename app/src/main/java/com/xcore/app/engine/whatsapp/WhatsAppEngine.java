package com.xcore.app.engine.whatsapp;

/**
 * Motor de atendimento WhatsApp do XCORE.
 *
 * A entrada/saída do WhatsApp é feita pelo Android NotificationListenerService.
 * O backend é usado somente para sincronização de configurações/comandos;
 * ele não recebe nem envia mensagens do WhatsApp.
 */
public interface WhatsAppEngine {

    WhatsAppResult start();

    WhatsAppResult stop();

    boolean isRunning();

    WhatsAppResult receive(WhatsAppMessage message);

    WhatsAppResult sendText(String phone, String text);
}
