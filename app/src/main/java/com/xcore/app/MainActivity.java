package com.xcore.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    private LinearLayout content;
    private TextView status;
    private final int bg = Color.rgb(8, 13, 27);
    private final int surface = Color.rgb(17, 25, 45);
    private final int surface2 = Color.rgb(22, 31, 54);
    private final int text = Color.rgb(239, 242, 250);
    private final int muted = Color.rgb(151, 163, 190);
    private final int accent = Color.rgb(108, 99, 255);
    private final int success = Color.rgb(65, 202, 139);
    private final int warning = Color.rgb(245, 180, 70);

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        build();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private TextView label(String s, int size) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextColor(text);
        v.setTextSize(size);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setFontFeatureSettings("kern");
        return v;
    }

    private TextView muted(String s) {
        TextView v = label(s, 13);
        v.setTextColor(muted);
        return v;
    }

    private GradientDrawable background(int color, float radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp((int) radius));
        return d;
    }

    private void margin(View v, int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(top), 0, dp(bottom));
        v.setLayoutParams(p);
    }

    private Button nav(String title, final Runnable action) {
        Button b = new Button(this);
        b.setText(title);
        b.setTextColor(text);
        b.setTextSize(13);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(4), 0, dp(4), 0);
        b.setBackground(background(surface2, 14));
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(46), 1);
        p.setMargins(dp(3), 0, dp(3), 0);
        b.setLayoutParams(p);
        return b;
    }

    private LinearLayout box() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(18), dp(16), dp(18), dp(16));
        l.setBackground(background(surface, 18));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(6), 0, dp(6));
        l.setLayoutParams(p);
        return l;
    }

    private LinearLayout sectionTitle(String title, String subtitle) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        TextView h = label(title, 24);
        h.setTypeface(null, 1);
        wrap.addView(h);
        TextView sub = muted(subtitle);
        sub.setPadding(0, dp(4), 0, 0);
        wrap.addView(sub);
        margin(wrap, 8, 8);
        return wrap;
    }

    private void addIntegration(String icon, String name) {
        LinearLayout card = box();
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView iconView = label(icon, 22);
        iconView.setGravity(Gravity.CENTER);
        iconView.setBackground(background(surface2, 14));
        row.addView(iconView, new LinearLayout.LayoutParams(dp(46), dp(46)));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(12), 0, 0, 0);
        TextView title = label(name, 16);
        title.setTypeface(null, 1);
        info.addView(title);
        info.addView(muted("Integração preparada para conexão."));
        row.addView(info, new LinearLayout.LayoutParams(0, -2, 1));

        TextView badge = label("PENDENTE", 10);
        badge.setTextColor(warning);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(8), dp(5), dp(8), dp(5));
        badge.setBackground(background(Color.rgb(62, 48, 27), 10));
        row.addView(badge);

        card.addView(row);
        content.addView(card);
    }

    private void build() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(14), dp(16), dp(10));
        root.setBackgroundColor(bg);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = label("XCORE", 28);
        title.setTypeface(null, 1);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));

        TextView pill = label("●  ONLINE", 11);
        pill.setTextColor(success);
        pill.setGravity(Gravity.CENTER);
        pill.setPadding(dp(10), dp(6), dp(10), dp(6));
        pill.setBackground(background(Color.rgb(20, 55, 46), 12));
        header.addView(pill);
        root.addView(header);

        TextView subtitle = muted("Central de automação • Android");
        subtitle.setPadding(0, dp(2), 0, dp(12));
        root.addView(subtitle);

        LinearLayout navs = new LinearLayout(this);
        navs.setPadding(0, 0, 0, dp(4));
        navs.addView(nav("Dashboard", this::dashboard));
        navs.addView(nav("Automação", this::automation));
        navs.addView(nav("Configurações", this::settings));
        root.addView(navs);

        status = muted("●  Sistema pronto para demonstração");
        status.setTextColor(success);
        status.setPadding(dp(4), dp(6), 0, dp(8));
        root.addView(status);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        ScrollView scroll = new ScrollView(this);
        scroll.setClipToPadding(false);
        scroll.setPadding(0, 0, 0, dp(8));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
        dashboard();
    }

    private void dashboard() {
        content.removeAllViews();
        content.addView(sectionTitle("Dashboard", "Visão geral das integrações do XCORE."));

        addIntegration("W", "WhatsApp Business");
        addIntegration("M", "Masterflix");
        addIntegration("X", "XCloud");
        addIntegration("G", "GerênciaApp");

        LinearLayout demo = box();
        TextView h = label("Teste rápido", 18);
        h.setTypeface(null, 1);
        demo.addView(h);
        demo.addView(muted("Execute um fluxo local sem acessar serviços externos."));

        EditText name = new EditText(this);
        name.setHint("Nome do cliente");
        name.setText("Cliente Demo");
        name.setTextColor(text);
        name.setHintTextColor(muted);
        name.setSingleLine(true);
        name.setPadding(dp(4), dp(10), dp(4), dp(6));

        EditText phone = new EditText(this);
        phone.setHint("WhatsApp com DDI");
        phone.setText("5511999999999");
        phone.setTextColor(text);
        phone.setHintTextColor(muted);
        phone.setInputType(2);
        phone.setSingleLine(true);
        phone.setPadding(dp(4), dp(10), dp(4), dp(6));

        demo.addView(name);
        demo.addView(phone);

        Button run = new Button(this);
        run.setText("Executar demonstração");
        run.setTextColor(Color.WHITE);
        run.setTextSize(14);
        run.setAllCaps(false);
        run.setTypeface(null, 1);
        run.setBackground(background(accent, 14));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(50));
        bp.setMargins(0, dp(10), 0, dp(8));
        run.setLayoutParams(bp);

        TextView out = muted("Aguardando execução…");
        out.setPadding(dp(4), dp(4), 0, 0);
        run.setOnClickListener(v -> {
            out.setText("✓  Teste criado\n✓  MEC / M3U simulado\n✓  XCloud simulado\n✓  GerênciaApp simulado\n✓  Resposta WhatsApp simulada");
            out.setTextColor(success);
            status.setText("●  Demonstração concluída com sucesso");
            status.setTextColor(success);
        });

        demo.addView(run);
        demo.addView(out);
        content.addView(demo);
    }

    private void automation() {
        content.removeAllViews();
        content.addView(sectionTitle("Automação", "Fluxo planejado para o provisionamento."));

        String[] steps = {
            "01", "Receber mensagem do WhatsApp Business",
            "02", "Criar teste no Masterflix",
            "03", "Ativar MEC e obter M3U",
            "04", "Enviar M3U + MEC para XCloud",
            "05", "Enviar M3U + MEC para GerênciaApp",
            "06", "Responder o cliente"
        };

        for (int i = 0; i < steps.length; i += 2) {
            LinearLayout card = box();
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView number = label(steps[i], 13);
            number.setTextColor(Color.WHITE);
            number.setGravity(Gravity.CENTER);
            number.setTypeface(null, 1);
            number.setBackground(background(accent, 12));
            row.addView(number, new LinearLayout.LayoutParams(dp(40), dp(40)));

            TextView description = label(steps[i + 1], 15);
            description.setPadding(dp(12), 0, 0, 0);
            row.addView(description, new LinearLayout.LayoutParams(0, -2, 1));
            card.addView(row);
            content.addView(card);
        }
    }

    private void settings() {
        content.removeAllViews();
        content.addView(sectionTitle("Configurações", "Controle o ambiente e as integrações."));

        LinearLayout mode = box();
        TextView modeTitle = label("Ambiente", 16);
        modeTitle.setTypeface(null, 1);
        mode.addView(modeTitle);
        mode.addView(muted("Modo atual: DEMO / desenvolvimento"));

        Button save = new Button(this);
        save.setText("Salvar configurações");
        save.setTextColor(Color.WHITE);
        save.setAllCaps(false);
        save.setBackground(background(accent, 14));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, dp(48));
        sp.setMargins(0, dp(12), 0, 0);
        save.setLayoutParams(sp);
        save.setOnClickListener(v -> {
            status.setText("●  Configuração salva localmente");
            status.setTextColor(success);
        });
        mode.addView(save);
        content.addView(mode);

        LinearLayout security = box();
        TextView sh = label("Segurança", 16);
        sh.setTypeface(null, 1);
        security.addView(sh);
        security.addView(muted("Credenciais e endpoints de produção devem ser configurados somente com dados autorizados."));
        content.addView(security);
    }
}
