package com.xcore.app.engine.whatsapp;

/**
 * Canal local de entrada/saída do WhatsApp.
 *
 * No XCORE, a implementação Android usa as notificações do WhatsApp
 * e o RemoteInput da própria notificação para responder.
 */
public interface WhatsAppTransport {
    WhatsAppResult start();
    WhatsAppResult stop();
    boolean isRunning();
    WhatsAppResult sendText(String phone, String text);
}
