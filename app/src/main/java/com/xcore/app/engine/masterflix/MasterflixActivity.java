package com.xcore.app.engine.masterflix;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public final class MasterflixActivity extends Activity {
    public static final String EXTRA_AUTO_TEST = "auto_test";
    public static final String EXTRA_TEST_LABEL = "test_label";
    public static final String EXTRA_PHONE = "phone";

    private static final String URL = "https://masterflix.sigmab.pro/#/dashboard";
    private static final String TESTE_1H = "MASTERFLIX TESTE COMPLETO 1H";

    private WebView webView;
    private ProgressBar progress;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(7, 11, 23));

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(10, 8, 10, 8);
        bar.setBackgroundColor(Color.rgb(16, 24, 43));

        android.widget.Button back = new android.widget.Button(this);
        back.setText("‹");
        back.setTextSize(28);
        back.setTextColor(Color.WHITE);
        back.setAllCaps(false);
        back.setOnClickListener(v -> finish());
        bar.addView(back, new LinearLayout.LayoutParams(54, 52));

        TextView title = new TextView(this);
        title.setText("Masterflix");
        title.setTextColor(Color.WHITE);
        title.setTextSize(19);
        title.setTypeface(null, 1);
        title.setGravity(Gravity.CENTER_VERTICAL);
        bar.addView(title, new LinearLayout.LayoutParams(0, 52, 1));

        Button reload = new Button(this);
        reload.setText("↻");
        reload.setTextSize(20);
        reload.setTextColor(Color.WHITE);
        reload.setAllCaps(false);
        reload.setOnClickListener(v -> webView.reload());
        bar.addView(reload, new LinearLayout.LayoutParams(54, 52));

        root.addView(bar);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        root.addView(progress, new LinearLayout.LayoutParams(-1, 3));

        FrameLayout content = new FrameLayout(this);
        webView = new WebView(this);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setLoadWithOverviewMode(false);
        s.setUseWideViewPort(false);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.GONE);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int newProgress) {
                progress.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
                progress.setProgress(newProgress);
            }
        });

        content.addView(webView, new FrameLayout.LayoutParams(-1, -1));

        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        webView.loadUrl(URL);

        if (getIntent().getBooleanExtra(EXTRA_AUTO_TEST, false)) {
            final String label = getIntent().getStringExtra(EXTRA_TEST_LABEL);
            final String phone = getIntent().getStringExtra(EXTRA_PHONE);
            if (phone != null && !phone.trim().isEmpty()
                    && !MasterflixWebAutomation.hasRequest()) {
                MasterflixWebAutomation.request(phone, label, null);
            }
            webView.postDelayed(() -> MasterflixWebAutomation.start(
                    webView,
                    label == null ? TESTE_1H : label,
                    phone
            ), 1800);
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    public void finishAutomation(String message, String error) {
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            MasterflixWebAutomation.finishRequest();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Teste gerado")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();

        MasterflixWebAutomation.finishRequest();
    }
}
