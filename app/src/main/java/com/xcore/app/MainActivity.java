package com.xcore.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    private LinearLayout content;
    private TextView status;
    private int bg = Color.rgb(11,16,32);
    private int panel = Color.rgb(17,24,43);
    private int text = Color.rgb(232,236,247);
    private int muted = Color.rgb(143,155,183);

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        build();
    }

    private TextView label(String s, int size) {
        TextView v = new TextView(this);
        v.setText(s); v.setTextColor(text); v.setTextSize(size); v.setPadding(0,8,0,8);
        return v;
    }

    private TextView muted(String s) {
        TextView v = label(s,13); v.setTextColor(muted); return v;
    }

    private Button nav(String title, final Runnable action) {
        Button b = new Button(this); b.setText(title); b.setTextColor(text);
        b.setAllCaps(false); b.setOnClickListener(v -> action.run());
        return b;
    }

    private LinearLayout box() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(20,18,20,18); l.setBackgroundColor(panel);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,8,0,8);
        l.setLayoutParams(p); return l;
    }

    private void build() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20,20,20,12); root.setBackgroundColor(bg);

        TextView title = label("XCORE", 26); title.setTypeface(null,1);
        root.addView(title);
        root.addView(muted("Automação • Android"));

        LinearLayout navs = new LinearLayout(this); navs.setOrientation(LinearLayout.HORIZONTAL);
        navs.addView(nav("Dashboard", this::dashboard), new LinearLayout.LayoutParams(0,-2,1));
        navs.addView(nav("Automação", this::automation), new LinearLayout.LayoutParams(0,-2,1));
        navs.addView(nav("Config.", this::settings), new LinearLayout.LayoutParams(0,-2,1));
        root.addView(navs);

        status = muted("● Aplicativo pronto");
        root.addView(status);

        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        ScrollView scroll = new ScrollView(this); scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        dashboard();
    }

    private void dashboard() {
        content.removeAllViews();
        TextView h=label("Dashboard",22); h.setTypeface(null,1); content.addView(h);
        content.addView(muted("Central de automação do XCORE."));
        String[] cards={"WhatsApp Business • Pendente","Masterflix • Pendente","XCloud • Pendente","GerênciaApp • Pendente"};
        for(String c:cards){ LinearLayout b=box(); b.addView(label(c,16)); b.addView(muted("Integração preparada; conexão real será configurada depois.")); content.addView(b); }

        LinearLayout demo=box(); demo.addView(label("Teste rápido",18));
        demo.addView(muted("Demonstração local, sem acessar painéis externos."));
        EditText name=new EditText(this); name.setHint("Nome do cliente"); name.setText("Cliente Demo"); name.setTextColor(text); name.setHintTextColor(muted);
        EditText phone=new EditText(this); phone.setHint("WhatsApp"); phone.setText("5511999999999"); phone.setTextColor(text); phone.setHintTextColor(muted);
        demo.addView(name); demo.addView(phone);
        Button run=new Button(this); run.setText("Executar demonstração"); run.setAllCaps(false);
        TextView out=muted("Aguardando execução…");
        run.setOnClickListener(v -> out.setText("✓ Teste Masterflix criado\n✓ MEC/M3U simulado\n✓ XCloud simulado\n✓ GerênciaApp simulado\n✓ Resposta WhatsApp simulada"));
        demo.addView(run); demo.addView(out); content.addView(demo);
    }

    private void automation() {
        content.removeAllViews();
        content.addView(label("Fluxo de automação",22));
        content.addView(muted("Sequência planejada para o provisionamento."));
        String[] steps={"01  Receber mensagem do WhatsApp Business","02  Criar teste no Masterflix","03  Ativar MEC e obter M3U","04  Enviar M3U + MEC para XCloud","05  Enviar M3U + MEC para GerênciaApp","06  Responder o cliente"};
        for(String s:steps){LinearLayout b=box(); b.addView(label(s,15)); content.addView(b);}
    }

    private void settings() {
        content.removeAllViews();
        content.addView(label("Configurações",22));
        content.addView(muted("Configuração local do aplicativo."));
        content.addView(muted("Modo atual: DEMO / desenvolvimento"));
        Button save=new Button(this); save.setText("Salvar"); save.setAllCaps(false);
        save.setOnClickListener(v -> status.setText("● Configuração salva localmente"));
        content.addView(save);
        content.addView(muted("As integrações de produção serão conectadas somente com endpoints e credenciais autorizados."));
    }
}
