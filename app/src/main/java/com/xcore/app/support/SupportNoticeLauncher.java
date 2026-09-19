package com.xcore.app.support;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public final class SupportNoticeLauncher {
    private SupportNoticeLauncher() {}

    public static void launch(Context context, SupportGroup group) {
        if (SupportWhatsAppAccessibilityService.isRunning()) {
            SupportWhatsAppAccessibilityService.send(context, group);
            return;
        }

        // Sem o serviço de acessibilidade, mantemos um fallback que abre o WhatsApp
        // com a mensagem preparada. O envio silencioso não é possível pela API pública.
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, group.getMessage());
            intent.setPackage("com.whatsapp.w4b");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception businessMissing) {
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, group.getMessage());
                intent.setPackage("com.whatsapp");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception missing) {
                Toast.makeText(context, "WhatsApp não encontrado para o aviso.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
