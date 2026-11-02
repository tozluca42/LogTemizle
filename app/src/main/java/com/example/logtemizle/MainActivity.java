package com.example.logtemizle;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

public class MainActivity extends Activity {

    TextView rootDetail, rootBadge, statusText, btnTemizle;
    TextView item1Icon, item2Icon, item3Icon, item4Icon;

    final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        rootDetail = findViewById(R.id.rootDetail);
        rootBadge = findViewById(R.id.rootBadge);
        statusText = findViewById(R.id.statusText);
        btnTemizle = findViewById(R.id.btnTemizle);
        item1Icon = findViewById(R.id.item1Icon);
        item2Icon = findViewById(R.id.item2Icon);
        item3Icon = findViewById(R.id.item3Icon);
        item4Icon = findViewById(R.id.item4Icon);

        TextView telegramLink = findViewById(R.id.telegramLink);
        telegramLink.setOnClickListener(v -> openTelegram());

        btnTemizle.setOnClickListener(v -> new Thread(this::temizle).start());

        new Thread(this::rootKontrolEt).start();
    }

    void openTelegram() {
        try {
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=Mtozluca"));
            startActivity(appIntent);
        } catch (ActivityNotFoundException e) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Mtozluca"));
            startActivity(webIntent);
        }
    }

    void rootKontrolEt() {
        boolean sonuc = Temizleyici.rootVarMi();
        handler.post(() -> {
            if (sonuc) {
                rootDetail.setText("Cihaz rootlu, tam temizlik yapılabilir");
                rootBadge.setText("VAR");
                rootBadge.setTextColor(getColor(R.color.success));
                rootBadge.setBackgroundResource(R.drawable.bg_badge_ok);
            } else {
                rootDetail.setText("Root izni bulunamadı");
                rootBadge.setText("YOK");
                rootBadge.setTextColor(getColor(R.color.danger));
                rootBadge.setBackgroundResource(R.drawable.bg_badge_no);
                btnTemizle.setAlpha(0.5f);
            }
        });
    }

    void adimIsaretle(TextView icon, boolean basarili) {
        handler.post(() -> icon.setText(basarili ? "✅" : "⚠️"));
    }

    void temizle() {
        handler.post(() -> {
            statusText.setText("Temizleniyor…");
            item1Icon.setText("⏳");
            item2Icon.setText("⏳");
            item3Icon.setText("⏳");
            item4Icon.setText("⏳");
        });

        try {
            int kod = Temizleyici.temizle();
            boolean basarili = kod == 0;

            adimIsaretle(item1Icon, basarili);
            Thread.sleep(200);
            adimIsaretle(item2Icon, basarili);
            Thread.sleep(200);
            adimIsaretle(item3Icon, basarili);
            Thread.sleep(200);
            adimIsaretle(item4Icon, basarili);

            handler.post(() -> statusText.setText(
                    basarili ? "Bitti. Önbellek ve log dosyaları temizlendi."
                             : "Bazı adımlar başarısız oldu (çıkış kodu " + kod + ")"));
        } catch (Exception e) {
            adimIsaretle(item1Icon, false);
            adimIsaretle(item2Icon, false);
            adimIsaretle(item3Icon, false);
            adimIsaretle(item4Icon, false);
            handler.post(() -> statusText.setText("Hata (root izni var mı?): " + e.getMessage()));
        }
    }
}
