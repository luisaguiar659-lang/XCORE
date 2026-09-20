package com.xcore.app;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.OutputStream;

public class MasterBannerActivity extends Activity {
    private final int bg = Color.rgb(8, 11, 16);
    private final int surface = Color.rgb(17, 23, 32);
    private final int green = Color.rgb(155, 255, 56);
    private final int text = Color.rgb(244, 247, 251);
    private final int muted = Color.rgb(154, 168, 184);
    private EditText brand, competition, home, away, date, time;
    private Spinner template;

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
    private TextView label(String s, int size) {
        TextView v = new TextView(this); v.setText(s); v.setTextColor(text); v.setTextSize(size); return v;
    }
    private EditText field(String hint) {
        EditText e = new EditText(this); e.setHint(hint); e.setHintTextColor(muted); e.setTextColor(text); e.setSingleLine(true);
        e.setPadding(dp(14), 0, dp(14), 0); e.setBackgroundColor(surface);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(52)); p.setMargins(0, dp(5), 0, dp(5)); e.setLayoutParams(p); return e;
    }
    @Override public void onCreate(Bundle b) {
        super.onCreate(b); getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(16), dp(14), dp(16), dp(10)); root.setBackgroundColor(bg);
        LinearLayout head = new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = label("‹", 34); back.setGravity(Gravity.CENTER); back.setOnClickListener(v -> finish()); head.addView(back, new LinearLayout.LayoutParams(dp(42), dp(48)));
        LinearLayout titles = new LinearLayout(this); titles.setOrientation(LinearLayout.VERTICAL);
        TextView h = label("Master Banner", 25); h.setTypeface(null, Typeface.BOLD); titles.addView(h); TextView sub = label("Crie banners esportivos prontos para compartilhar.", 13); sub.setTextColor(muted); titles.addView(sub);
        head.addView(titles, new LinearLayout.LayoutParams(0, -2, 1)); root.addView(head);
        ScrollView scroll = new ScrollView(this); LinearLayout content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        content.addView(label("Marca", 18)); brand=field("Nome da marca"); content.addView(brand);
        content.addView(label("Template",18)); template=new Spinner(this); template.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,new String[]{"Match Day","Resultado Final","Próximo Jogo"})); content.addView(template);
        content.addView(label("Jogo",18)); competition=field("Competição"); home=field("Time mandante"); away=field("Time visitante"); date=field("Data"); time=field("Horário"); content.addView(competition); content.addView(home); content.addView(away); content.addView(date); content.addView(time);
        Button preview=new Button(this); preview.setText("Gerar prévia"); preview.setTextColor(Color.WHITE); preview.setAllCaps(false); preview.setBackgroundColor(Color.rgb(40,70,30)); content.addView(preview);
        Button save=new Button(this); save.setText("Salvar banner PNG"); save.setTextColor(Color.WHITE); save.setAllCaps(false); save.setBackgroundColor(green); content.addView(save);
        preview.setOnClickListener(v -> showPreview(false)); save.setOnClickListener(v -> showPreview(true));
        Space sp=new Space(this); content.addView(sp,new LinearLayout.LayoutParams(1,dp(50))); scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
    }
    private Bitmap render() {
        int w=1080,h=1920; Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888); Canvas c=new Canvas(b); c.drawColor(Color.rgb(8,11,16));
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD)); p.setColor(green); p.setTextAlign(Paint.Align.CENTER); p.setTextSize(58); c.drawText(template.getSelectedItem().toString().toUpperCase(),w/2f,210,p);
        p.setColor(text); p.setTextSize(70); c.drawText(value(brand,"MASTER BANNER"),w/2f,350,p); p.setTextSize(44); p.setColor(muted); c.drawText(value(competition,"JOGO"),w/2f,470,p);
        p.setColor(text); p.setTextSize(72); c.drawText(value(home,"CASA"),w/2f,760,p); p.setColor(green); p.setTextSize(52); c.drawText("×",w/2f,900,p); p.setColor(text); p.setTextSize(72); c.drawText(value(away,"VISITANTE"),w/2f,1040,p);
        p.setColor(muted); p.setTextSize(42); c.drawText(value(date,"DATA")+"  •  "+value(time,"HORÁRIO"),w/2f,1240,p);
        p.setColor(green); p.setTextSize(36); c.drawText("MASTER BANNER",w/2f,1740,p); return b;
    }
    private String value(EditText e,String fallback){String s=e.getText().toString().trim();return s.isEmpty()?fallback:s;}
    private void showPreview(boolean save) {
        Bitmap b=render(); if(save){
            try { ContentValues cv=new ContentValues(); cv.put(MediaStore.Images.Media.DISPLAY_NAME,"master-banner-"+System.currentTimeMillis()+".png"); cv.put(MediaStore.Images.Media.MIME_TYPE,"image/png"); cv.put(MediaStore.Images.Media.RELATIVE_PATH,Environment.DIRECTORY_PICTURES+"/XCORE"); Uri u=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv); if(u==null)throw new Exception("Não foi possível criar o arquivo"); OutputStream out=getContentResolver().openOutputStream(u); b.compress(Bitmap.CompressFormat.PNG,100,out); if(out!=null)out.close(); Toast.makeText(this,"Banner salvo em Fotos/XCORE",Toast.LENGTH_LONG).show(); } catch(Exception e){Toast.makeText(this,"Falha ao salvar: "+e.getMessage(),Toast.LENGTH_LONG).show();}
        } else { ImageView image=new ImageView(this); image.setImageBitmap(b); image.setAdjustViewBounds(true); new android.app.AlertDialog.Builder(this).setTitle("Prévia").setView(image).setPositiveButton("Fechar",null).show(); }
        if(!save)b.recycle();
    }
}
