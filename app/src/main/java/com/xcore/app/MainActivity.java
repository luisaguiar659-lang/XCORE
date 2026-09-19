package com.xcore.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.xcore.app.engine.whatsapp.AndroidWhatsAppTriggerStore;
import com.xcore.app.engine.whatsapp.WhatsAppFlowIds;
import com.xcore.app.engine.whatsapp.WhatsAppTrigger;
import java.util.List;

public class MainActivity extends Activity {
    private LinearLayout content;
    private TextView status;
    private Button dashboardTab, automationTab, settingsTab;
    private AndroidWhatsAppTriggerStore triggerStore;

    private final int bg = Color.rgb(7, 11, 23);
    private final int surface = Color.rgb(16, 24, 43);
    private final int surface2 = Color.rgb(23, 32, 57);
    private final int text = Color.rgb(242, 244, 250);
    private final int muted = Color.rgb(151, 163, 190);
    private final int accent = Color.rgb(255, 35, 58);
    private final int accentDark = Color.rgb(190, 12, 38);
    private final int success = Color.rgb(65, 202, 139);
    private final int warning = Color.rgb(245, 180, 70);

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        triggerStore = new AndroidWhatsAppTriggerStore(this);
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
        b.setBackground(background(surface2, 16));
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(48), 1);
        p.setMargins(dp(3), 0, dp(3), 0);
        b.setLayoutParams(p);
        return b;
    }

    private void setActive(Button active) {
        Button[] all = {dashboardTab, automationTab, settingsTab};
        for (Button b : all) {
            if (b == null) continue;
            b.setBackground(background(b == active ? accent : surface2, 16));
            b.setTextColor(text);
        }
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
        TextView h = label(title, 25);
        h.setTypeface(null, 1);
        wrap.addView(h);
        TextView sub = muted(subtitle);
        sub.setPadding(0, dp(4), 0, 0);
        wrap.addView(sub);
        margin(wrap, 10, 8);
        return wrap;
    }

    private TextView chip(String textValue, int color, int bgColor) {
        TextView v = label(textValue, 10);
        v.setTextColor(color);
        v.setGravity(Gravity.CENTER);
        v.setTypeface(null, 1);
        v.setPadding(dp(9), dp(6), dp(9), dp(6));
        v.setBackground(background(bgColor, 12));
        return v;
    }

    private void addIntegration(int iconRes, String name, String detail) {
        LinearLayout card = box();
        card.setPadding(0, 0, 0, 0);
        card.setOnClickListener(v -> showIntegration(name));

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);

        View stripe = new View(this);
        int stripeColor = name.equals("WhatsApp Business") ? Color.rgb(0, 220, 125)
                : name.equals("Masterflix") ? Color.rgb(255, 153, 0)
                : name.equals("Master XCloud") ? Color.rgb(20, 145, 255)
                : Color.rgb(170, 55, 255);
        stripe.setBackgroundColor(stripeColor);
        row.addView(stripe, new LinearLayout.LayoutParams(dp(5), -1));

        ImageView iconView = new ImageView(this);
        iconView.setImageResource(iconRes);
        iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iconView.setPadding(dp(4), dp(4), dp(4), dp(4));
        iconView.setBackground(background(surface2, 14));
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(58), dp(58));
        ip.setMargins(dp(14), dp(10), 0, dp(10));
        row.addView(iconView, ip);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(13), 0, dp(8), 0);

        TextView title = label(name, 16);
        title.setTypeface(null, 1);
        info.addView(title);

        TextView desc = muted(detail);
        desc.setPadding(0, dp(3), 0, 0);
        info.addView(desc);

        row.addView(info, new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = label("›", 25);
        arrow.setTextColor(muted);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(26), dp(60)));

        TextView pending = chip("PENDENTE", warning, Color.rgb(62, 48, 27));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-2, -2);
        cp.setMargins(0, 0, dp(7), 0);
        row.addView(pending, cp);

        card.addView(row);
        content.addView(card);
    }

    private void addStat(String value, String caption) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(13), dp(14), dp(13));
        card.setBackground(background(surface, 16));

        TextView number = label(value, 23);
        number.setTypeface(null, 1);
        card.addView(number);

        TextView textView = muted(caption);
        textView.setPadding(0, dp(2), 0, 0);
        card.addView(textView);

        View line = new View(this);
        line.setBackgroundColor(accent);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(58), dp(3));
        lp.setMargins(0, dp(9), 0, 0);
        card.addView(line, lp);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1);
        p.setMargins(dp(3), 0, dp(3), 0);
        card.setLayoutParams(p);
        statsRow.addView(card);
    }

    private LinearLayout statsRow;

    private void build() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(14), dp(16), dp(10));
        root.setBackgroundColor(bg);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);

        ImageView logo = new ImageView(this);
        logo.setImageResource(com.xcore.app.R.drawable.xcore_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        logo.setPadding(dp(2), dp(2), dp(2), dp(2));
        header.addView(logo, new LinearLayout.LayoutParams(dp(44), dp(44)));

        TextView title = label("XCORE", 29);
        title.setTypeface(null, 1);
        title.setPadding(dp(10), 0, 0, 0);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));

        TextView pill = label("●  ONLINE", 11);
        pill.setTextColor(success);
        pill.setGravity(Gravity.CENTER);
        pill.setPadding(dp(12), dp(8), dp(12), dp(8));
        pill.setBackground(background(Color.rgb(12, 55, 44), 16));
        header.addView(pill);
        root.addView(header);

        TextView subtitle = muted("Central de automação • Android");
        subtitle.setPadding(0, dp(2), 0, dp(12));
        root.addView(subtitle);

        LinearLayout navs = new LinearLayout(this);
        dashboardTab = nav("Dashboard", this::dashboard);
        automationTab = nav("Automação", this::automation);
        settingsTab = nav("Configurações", this::settings);
        navs.addView(dashboardTab);
        navs.addView(automationTab);
        navs.addView(settingsTab);
        root.addView(navs);

        status = muted("●  Sistema pronto para demonstração");
        status.setTextColor(success);
        status.setPadding(dp(10), dp(10), dp(10), dp(10));
        status.setBackground(background(Color.rgb(8, 22, 34), 14));
        root.addView(status);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        ScrollView scroll = new ScrollView(this);
        scroll.setClipToPadding(false);
        scroll.setPadding(0, 0, 0, dp(8));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.CENTER);
        bottom.setPadding(0, dp(6), 0, 0);
        bottom.setBackground(background(Color.rgb(9, 15, 29), 14));

        String[] bottomItems = {"⌂\nInício", "ϟ\nAutomação", "◷\nHistórico", "⚙\nConfigurações"};
        for (int i = 0; i < bottomItems.length; i++) {
            TextView item = label(bottomItems[i], 11);
            item.setGravity(Gravity.CENTER);
            item.setTextColor(i == 0 ? accent : muted);
            final int index = i;
            item.setOnClickListener(v -> {
                if (index == 0) dashboard();
                else if (index == 1) automation();
                else if (index == 3) settings();
                else Toast.makeText(this, "Histórico • disponível em breve", Toast.LENGTH_SHORT).show();
            });
            bottom.addView(item, new LinearLayout.LayoutParams(0, dp(52), 1));
        }
        root.addView(bottom);

        setContentView(root);
        dashboard();
    }

    private void dashboard() {
        setActive(dashboardTab);
        content.removeAllViews();

        content.addView(sectionTitle("Dashboard", "Visão geral das integrações e operações."));

        statsRow = new LinearLayout(this);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        addStat("04", "Integrações");
        addStat("00", "Ativas");
        addStat("00", "Execuções");
        content.addView(statsRow);

        TextView integrations = label("Integrações", 17);
        integrations.setTypeface(null, 1);
        integrations.setPadding(dp(2), dp(15), 0, dp(3));
        content.addView(integrations);

        addIntegration(R.drawable.whatsapp_logo, "WhatsApp Business", "Canal de atendimento");
        addIntegration(R.drawable.masterflix_logo, "Masterflix", "Criação de testes");
        addIntegration(R.drawable.master_xcloud_logo, "Master XCloud", "Provisionamento");
        addIntegration(R.drawable.master_ibo_logo, "Master IBO", "Gestão do cliente");

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
        phone.setSingleLine(true);
        phone.setInputType(2);
        phone.setPadding(dp(4), dp(10), dp(4), dp(6));

        LinearLayout formRow = new LinearLayout(this);
        formRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout fields = new LinearLayout(this);
        fields.setOrientation(LinearLayout.VERTICAL);
        fields.addView(name);
        fields.addView(phone);
        formRow.addView(fields, new LinearLayout.LayoutParams(0, -2, 1));

        Button run = new Button(this);
        run.setText("▶  EXECUTAR");
        run.setTextColor(Color.WHITE);
        run.setTextSize(14);
        run.setAllCaps(false);
        run.setTypeface(null, 1);
        run.setBackground(background(accent, 16));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(dp(142), dp(52));
        bp.setMargins(dp(10), dp(11), 0, dp(8));
        run.setLayoutParams(bp);

        formRow.addView(run);
        demo.addView(formRow);

        TextView out = muted("Aguardando execução…");
        out.setPadding(dp(4), dp(4), 0, 0);

        run.setOnClickListener(v -> {
            run.setEnabled(false);
            run.setText("Executando…");
            out.setTextColor(muted);
            out.setText("1/5  Criando teste…");
            out.postDelayed(() -> out.setText("2/5  Preparando MEC / M3U…"), 450);
            out.postDelayed(() -> out.setText("3/5  Simulando Master XCloud…"), 900);
            out.postDelayed(() -> out.setText("4/5  Simulando Master IBO…"), 1350);
            out.postDelayed(() -> {
                out.setText("✓  Demonstração concluída");
                out.setTextColor(success);
                status.setText("●  Demonstração concluída com sucesso");
                status.setTextColor(success);
                run.setText("Executar novamente");
                run.setEnabled(true);
            }, 1800);
        });

        demo.addView(out);
        content.addView(demo);

        // Espaço extra para que a barra inferior fixa não cubra o final do formulário.
        Space bottomSpace = new Space(this);
        content.addView(bottomSpace, new LinearLayout.LayoutParams(1, dp(82)));
    }

    private void automation() {
        setActive(automationTab);
        content.removeAllViews();
        content.addView(sectionTitle("Automação", "Fluxo planejado para o provisionamento."));

        LinearLayout triggerBox = box();
        TextView th = label("Comandos WhatsApp", 18);
        th.setTypeface(null, 1);
        triggerBox.addView(th);
        triggerBox.addView(muted("Configure comandos, gatilhos e respostas automáticas para seus clientes."));
        Button manage = new Button(this);
        manage.setText("⚡  Gerenciar comandos");
        manage.setTextColor(Color.WHITE);
        manage.setAllCaps(false);
        manage.setTypeface(null, 1);
        manage.setBackground(background(accent, 16));
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(-1, dp(50));
        mp.setMargins(0, dp(12), 0, 0);
        manage.setLayoutParams(mp);
        manage.setOnClickListener(v -> whatsappTriggers());
        triggerBox.addView(manage);
        content.addView(triggerBox);

        String[][] steps = {
            {"01", "Receber mensagem", "WhatsApp Business"},
            {"02", "Criar teste", "Masterflix"},
            {"03", "Ativar MEC e obter M3U", "Masterflix"},
            {"04", "Enviar M3U + MEC", "Master XCloud"},
            {"05", "Enviar M3U + MEC", "Master IBO"},
            {"06", "Responder cliente", "WhatsApp Business"}
        };

        for (int i = 0; i < steps.length; i++) {
            LinearLayout card = box();
            card.setPadding(dp(14), dp(14), dp(14), dp(14));

            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView number = label(steps[i][0], 12);
            number.setTextColor(Color.WHITE);
            number.setGravity(Gravity.CENTER);
            number.setTypeface(null, 1);
            number.setBackground(background(accent, 12));
            row.addView(number, new LinearLayout.LayoutParams(dp(42), dp(42)));

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setPadding(dp(13), 0, 0, 0);

            TextView step = label(steps[i][1], 15);
            step.setTypeface(null, 1);
            info.addView(step);
            info.addView(muted(steps[i][2]));

            row.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
            row.addView(chip("PENDENTE", warning, Color.rgb(62, 48, 27)));
            card.addView(row);
            content.addView(card);
        }
    }

    private void whatsappTriggers() {
        setActive(automationTab);
        content.removeAllViews();

        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = label("‹", 32);
        back.setTextColor(text);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> automation());
        heading.addView(back, new LinearLayout.LayoutParams(dp(42), dp(48)));

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        TextView h = label("Comandos WhatsApp", 23);
        h.setTypeface(null, 1);
        titles.addView(h);
        titles.addView(muted("Cada comando define o gatilho que inicia o atendimento e a resposta enviada ao cliente."));
        heading.addView(titles, new LinearLayout.LayoutParams(0, -2, 1));
        content.addView(heading);

        Button add = new Button(this);
        add.setText("+  Novo comando");
        add.setTextColor(Color.WHITE);
        add.setAllCaps(false);
        add.setTypeface(null, 1);
        add.setBackground(background(accent, 16));
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, dp(50));
        ap.setMargins(0, dp(10), 0, dp(8));
        add.setLayoutParams(ap);
        add.setOnClickListener(v -> showTriggerDialog(null));
        content.addView(add);

        List<WhatsAppTrigger> triggers = triggerStore.all();
        if (triggers.isEmpty()) {
            LinearLayout empty = box();
            TextView eh = label("Nenhum comando configurado", 16);
            eh.setTypeface(null, 1);
            empty.addView(eh);
            empty.addView(muted("Adicione um comando para configurar o gatilho e a resposta automática."));
            content.addView(empty);
        } else {
            for (WhatsAppTrigger trigger : triggers) {
                addTriggerCard(trigger);
            }
        }

        Space bottomSpace = new Space(this);
        content.addView(bottomSpace, new LinearLayout.LayoutParams(1, dp(60)));
    }

    private void addTriggerCard(WhatsAppTrigger trigger) {
        LinearLayout card = box();

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);

        TextView name = label(trigger.getName().isEmpty() ? "Sem nome" : trigger.getName(), 17);
        name.setTypeface(null, 1);
        info.addView(name);

        String rule = trigger.getMatchType().name() + " • \"" + trigger.getPattern() + "\"";
        info.addView(muted(rule));
        info.addView(muted("Resposta: " + (trigger.getResponse().isEmpty() ? "não configurada" : trigger.getResponse())));
        if (!trigger.getQuestion().isEmpty()) {
            info.addView(muted("Pergunta: " + trigger.getQuestion()));
        }
        info.addView(muted("Fluxo: " + trigger.getFlowId()));

        top.addView(info, new LinearLayout.LayoutParams(0, -2, 1));

        CheckBox active = new CheckBox(this);
        active.setChecked(trigger.isActive());
        active.setText("Ativo");
        active.setTextColor(trigger.isActive() ? success : muted);
        active.setOnCheckedChangeListener((buttonView, checked) -> {
            trigger.setActive(checked);
            triggerStore.add(trigger);
            buttonView.setTextColor(checked ? success : muted);
            status.setText(checked ? "●  Comando ativado" : "●  Comando desativado");
            status.setTextColor(checked ? success : warning);
        });
        top.addView(active);
        card.addView(top);

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END);
        actions.setPadding(0, dp(8), 0, 0);

        Button edit = smallAction("Editar");
        edit.setOnClickListener(v -> showTriggerDialog(trigger));
        actions.addView(edit);

        Button delete = smallAction("Excluir");
        delete.setTextColor(Color.rgb(255, 110, 120));
        delete.setOnClickListener(v -> confirmDeleteTrigger(trigger));
        actions.addView(delete);

        card.addView(actions);
        content.addView(card);
    }

    private Button smallAction(String title) {
        Button b = new Button(this);
        b.setText(title);
        b.setTextColor(text);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setBackground(background(surface2, 12));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(92), dp(42));
        p.setMargins(dp(5), 0, 0, 0);
        b.setLayoutParams(p);
        return b;
    }

    private void showTriggerDialog(final WhatsAppTrigger existing) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(4), dp(20), 0);

        EditText name = new EditText(this);
        name.setHint("Nome do gatilho");
        name.setTextColor(text);
        name.setHintTextColor(muted);
        name.setSingleLine(true);
        if (existing != null) name.setText(existing.getName());
        form.addView(name);

        EditText pattern = new EditText(this);
        pattern.setHint("Gatilho / palavra-chave");
        pattern.setTextColor(text);
        pattern.setHintTextColor(muted);
        pattern.setSingleLine(true);
        if (existing != null) pattern.setText(existing.getPattern());
        form.addView(pattern);

        EditText response = new EditText(this);
        response.setHint("Resposta enviada ao cliente");
        response.setTextColor(text);
        response.setHintTextColor(muted);
        response.setGravity(Gravity.TOP);
        response.setMinLines(3);
        response.setMaxLines(6);
        response.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        if (existing != null) response.setText(existing.getResponse());
        form.addView(response);

        EditText question = new EditText(this);
        question.setHint("Pergunta para a próxima resposta (opcional)");
        question.setTextColor(text);
        question.setHintTextColor(muted);
        question.setGravity(Gravity.TOP);
        question.setMinLines(2);
        question.setMaxLines(4);
        question.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        if (existing != null) question.setText(existing.getQuestion());
        form.addView(question);

        TextView help = muted("A resposta é enviada quando o comando corresponder. A pergunta pode preparar a próxima etapa da conversa.");
        help.setPadding(0, dp(4), 0, dp(6));
        form.addView(help);

        Spinner type = new Spinner(this);
        String[] types = {"CONTÉM", "EXATO", "COMEÇA COM", "PALAVRA-CHAVE"};
        type.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, types));
        if (existing != null) {
            type.setSelection(matchTypePosition(existing.getMatchType()));
        }
        form.addView(type);

        Spinner flow = new Spinner(this);
        String[] flows = {WhatsAppFlowIds.TESTE_CLIENTE, WhatsAppFlowIds.VENDA, WhatsAppFlowIds.SUPORTE};
        flow.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, flows));
        if (existing != null) flow.setSelection(flowPosition(existing.getFlowId()));
        form.addView(flow);

        CheckBox active = new CheckBox(this);
        active.setText("Comando ativo");
        active.setTextColor(text);
        active.setChecked(existing == null || existing.isActive());
        form.addView(active);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Novo gatilho" : "Editar comando")
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(existing == null ? "Criar" : "Salvar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String triggerName = name.getText().toString().trim();
            String triggerPattern = pattern.getText().toString().trim();

            if (triggerName.isEmpty() || triggerPattern.isEmpty()) {
                Toast.makeText(this, "Preencha o nome e o gatilho.", Toast.LENGTH_SHORT).show();
                return;
            }

            WhatsAppTrigger.MatchType matchType = positionMatchType(type.getSelectedItemPosition());
            String flowId = flows[flow.getSelectedItemPosition()];

            String responseText = response.getText().toString().trim();
            String questionText = question.getText().toString().trim();

            if (responseText.isEmpty()) {
                Toast.makeText(this, "Informe a resposta que o cliente receberá.", Toast.LENGTH_SHORT).show();
                return;
            }

            WhatsAppTrigger saved = existing == null
                    ? new WhatsAppTrigger(java.util.UUID.randomUUID().toString(), triggerName, matchType, triggerPattern,
                    flowId, responseText, questionText, active.isChecked())
                    : new WhatsAppTrigger(existing.getId(), triggerName, matchType, triggerPattern,
                    flowId, responseText, questionText, active.isChecked());

            triggerStore.add(saved);
            dialog.dismiss();
            status.setText("●  Comando salvo");
            status.setTextColor(success);
            whatsappTriggers();
        }));

        dialog.show();
    }

    private int matchTypePosition(WhatsAppTrigger.MatchType type) {
        if (type == WhatsAppTrigger.MatchType.EXACT) return 1;
        if (type == WhatsAppTrigger.MatchType.STARTS_WITH) return 2;
        if (type == WhatsAppTrigger.MatchType.KEYWORD) return 3;
        return 0;
    }

    private WhatsAppTrigger.MatchType positionMatchType(int position) {
        if (position == 1) return WhatsAppTrigger.MatchType.EXACT;
        if (position == 2) return WhatsAppTrigger.MatchType.STARTS_WITH;
        if (position == 3) return WhatsAppTrigger.MatchType.KEYWORD;
        return WhatsAppTrigger.MatchType.CONTAINS;
    }

    private int flowPosition(String flowId) {
        if (WhatsAppFlowIds.VENDA.equals(flowId)) return 1;
        if (WhatsAppFlowIds.SUPORTE.equals(flowId)) return 2;
        return 0;
    }

    private void confirmDeleteTrigger(final WhatsAppTrigger trigger) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir comando")
                .setMessage("Excluir \"" + trigger.getName() + "\"?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {
                    triggerStore.remove(trigger.getId());
                    status.setText("●  Comando excluído");
                    status.setTextColor(success);
                    whatsappTriggers();
                })
                .show();
    }

    private void settings() {
        setActive(settingsTab);
        content.removeAllViews();
        content.addView(sectionTitle("Configurações", "Controle o ambiente e as integrações."));

        LinearLayout mode = box();
        TextView modeTitle = label("Ambiente", 17);
        modeTitle.setTypeface(null, 1);
        mode.addView(modeTitle);
        mode.addView(muted("Selecione o ambiente usado pelo XCORE."));

        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.VERTICAL);
        RadioButton demo = new RadioButton(this);
        demo.setText("  DEMO / desenvolvimento");
        demo.setTextColor(text);
        demo.setChecked(true);
        RadioButton production = new RadioButton(this);
        production.setText("  PRODUÇÃO");
        production.setTextColor(text);
        group.addView(demo);
        group.addView(production);
        mode.addView(group);

        Button save = new Button(this);
        save.setText("Salvar configurações");
        save.setTextColor(Color.WHITE);
        save.setAllCaps(false);
        save.setTypeface(null, 1);
        save.setBackground(background(accent, 16));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, dp(50));
        sp.setMargins(0, dp(10), 0, 0);
        save.setLayoutParams(sp);
        save.setOnClickListener(v -> {
            status.setText("●  Configuração salva localmente");
            status.setTextColor(success);
        });
        mode.addView(save);
        content.addView(mode);

        LinearLayout security = box();
        TextView sh = label("Segurança", 17);
        sh.setTypeface(null, 1);
        security.addView(sh);
        security.addView(muted("Credenciais e endpoints de produção devem ser configurados somente com dados autorizados."));
        security.addView(chip("CREDENCIAIS PROTEGIDAS", success, Color.rgb(18, 55, 45)));
        content.addView(security);
    }

    private void showIntegration(String name) {
        Toast.makeText(this, name + " • configuração disponível em breve", Toast.LENGTH_SHORT).show();
    }
}
