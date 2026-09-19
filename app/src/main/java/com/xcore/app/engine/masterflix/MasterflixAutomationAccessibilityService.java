package com.xcore.app.engine.masterflix;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Automatiza somente o fluxo de geração de teste no painel web do Masterflix.
 * Não usa API do Masterflix: opera no Chrome pela árvore de acessibilidade.
 */
public final class MasterflixAutomationAccessibilityService extends AccessibilityService {
    public interface Callback {
        void onSuccess(String text);
        void onError(String message);
    }

    private static final String CHROME_PACKAGE = "com.android.chrome";
    private static final String MASTERFLIX_URL = "https://masterflix.sigmab.pro/#/dashboard";
    private static final String DEFAULT_TEST_LABEL = "MASTERFLIX TESTE COMPLETO 1H";

    private static MasterflixAutomationAccessibilityService instance;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Callback callback;
    private String testLabel = DEFAULT_TEST_LABEL;
    private int step = 0;
    private boolean busy;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!busy || event == null) return;
        CharSequence packageName = event.getPackageName();
        if (packageName == null || !CHROME_PACKAGE.equals(packageName.toString())) return;
        handleChromeWindow();
    }

    @Override
    public void onInterrupt() {
        fail("Automação do Masterflix interrompida.");
    }

    @Override
    public void onDestroy() {
        if (instance == this) instance = null;
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public static boolean isEnabled() {
        return instance != null;
    }

    public static void startTest(String requestedLabel, Callback callback) {
        MasterflixAutomationAccessibilityService service = instance;
        if (service == null) {
            if (callback != null) callback.onError("Ative a automação do Masterflix nas Configurações do XCORE.");
            return;
        }
        service.start(requestedLabel, callback);
    }

    private void start(String requestedLabel, Callback callback) {
        if (busy) {
            if (callback != null) callback.onError("Já existe uma geração de teste em andamento.");
            return;
        }

        this.callback = callback;
        this.testLabel = requestedLabel == null || requestedLabel.trim().isEmpty()
                ? DEFAULT_TEST_LABEL : requestedLabel.trim();
        this.step = 1;
        this.busy = true;

        openChrome();
        handler.postDelayed(() -> handleChromeWindow(), 1200L);
        handler.postDelayed(() -> {
            if (busy) fail("Tempo limite ao abrir o Masterflix.");
        }, 30000L);
    }

    private void openChrome() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse(MASTERFLIX_URL));
            intent.setPackage(CHROME_PACKAGE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception first) {
            try {
                Intent fallback = new Intent(Intent.ACTION_VIEW);
                fallback.setData(android.net.Uri.parse(MASTERFLIX_URL));
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(fallback);
            } catch (Exception error) {
                fail("Não foi possível abrir o navegador.");
            }
        }
    }

    private void handleChromeWindow() {
        if (!busy) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            handler.postDelayed(this::handleChromeWindow, 800L);
            return;
        }

        if (step == 1) {
            if (clickByText(root, testLabel, true)) {
                step = 2;
                handler.postDelayed(this::handleChromeWindow, 1200L);
                return;
            }

            // O painel pode estar no topo de uma página rolável.
            if (clickByText(root, "Teste Rápido", false)) {
                handler.postDelayed(this::handleChromeWindow, 700L);
                return;
            }

            handler.postDelayed(this::handleChromeWindow, 900L);
            return;
        }

        if (step == 2) {
            if (clickByText(root, "Copiar", true)) {
                step = 3;
                handler.postDelayed(this::readClipboard, 500L);
                return;
            }
            // Alguns carregamentos exibem o botão somente depois de alguns eventos.
            handler.postDelayed(this::handleChromeWindow, 700L);
        }
    }

    private void readClipboard() {
        if (!busy) return;

        try {
            ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (manager == null || !manager.hasPrimaryClip()) {
                retryClipboard();
                return;
            }

            ClipData clip = manager.getPrimaryClip();
            if (clip == null || clip.getItemCount() == 0) {
                retryClipboard();
                return;
            }

            CharSequence text = clip.getItemAt(0).coerceToText(this);
            String value = text == null ? "" : text.toString().trim();
            if (value.length() < 20) {
                retryClipboard();
                return;
            }

            succeed(value);
        } catch (Exception error) {
            fail("Não foi possível ler o teste gerado: " + safe(error.getMessage()));
        }
    }

    private void retryClipboard() {
        handler.postDelayed(this::readClipboard, 500L);
    }

    private boolean clickByText(AccessibilityNodeInfo root, String wanted, boolean exact) {
        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        collect(root, nodes);

        String target = normalize(wanted);
        for (AccessibilityNodeInfo node : nodes) {
            CharSequence text = node.getText();
            CharSequence description = node.getContentDescription();
            String a = normalize(text == null ? "" : text.toString());
            String b = normalize(description == null ? "" : description.toString());

            boolean match = exact
                    ? target.equals(a) || target.equals(b)
                    : a.contains(target) || b.contains(target);

            if (match && performClick(node)) return true;
        }
        return false;
    }

    private void collect(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> out) {
        if (node == null) return;
        out.add(node);
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) collect(child, out);
        }
    }

    private boolean performClick(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo current = node;
        while (current != null) {
            if (current.isClickable()) {
                return current.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            current = current.getParent();
        }
        return false;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\s+", " ");
    }

    private void succeed(String text) {
        Callback cb = callback;
        reset();
        if (cb != null) cb.onSuccess(text);
    }

    private void fail(String message) {
        Callback cb = callback;
        reset();
        if (cb != null) cb.onError(message);
    }

    private void reset() {
        busy = false;
        callback = null;
        step = 0;
        handler.removeCallbacksAndMessages(null);
    }

    private String safe(String value) {
        if (value == null || value.trim().isEmpty()) return "erro desconhecido";
        return value.length() > 80 ? value.substring(0, 80) + "…" : value;
    }
}
