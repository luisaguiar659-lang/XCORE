package com.xcore.app.support;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public final class SupportNoticeLauncher {
    private SupportNoticeLauncher() {}

    public static void openGroup(Context context, SupportGroup group) {
        if (SupportWhatsAppAccessibilityService.isRunning()) {
            SupportWhatsAppAccessibilityService.openGroup(context, group);
            return;
        }
        Toast.makeText(context, "Ative o Acesso de acessibilidade do XCORE para o Motor de Suporte.", Toast.LENGTH_LONG).show();
    }

    public static void launch(Context context, SupportGroup group) {
        if (SupportWhatsAppAccessibilityService.isRunning()) {
            SupportWhatsAppAccessibilityService.send(context, group);
            return;
        }
        Toast.makeText(context, "Ative o Acesso de acessibilidade do XCORE para o Motor de Suporte.", Toast.LENGTH_LONG).show();
    }
}
