package com.xcore.app.engine.whatsapp;

/**
 * Canal de entrada/saída do provedor WhatsApp.
 * A implementação oficial será responsável por webhook e envio autenticado.
 */
public interface WhatsAppTransport {
    WhatsAppResult start();
    WhatsAppResult stop();
    boolean isRunning();
    WhatsAppResult sendText(String phone, String text);
}
