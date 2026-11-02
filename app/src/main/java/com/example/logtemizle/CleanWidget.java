package com.example.logtemizle;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Ana ekran widget'ı: tek dokunuşla temizlik.
 *
 * Kendi kendine çalışmaz — widget_info.xml içinde updatePeriodMillis="0",
 * yani zamanlayıcı yok. Sadece kullanıcı dokununca tetiklenir.
 */
public class CleanWidget extends AppWidgetProvider {

    private static final String ACTION_TEMIZLE = "com.example.logtemizle.ACTION_TEMIZLE";

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) {
            mgr.updateAppWidget(id, goruntu(ctx, "Dokun"));
        }
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        super.onReceive(ctx, intent);
        if (!ACTION_TEMIZLE.equals(intent.getAction())) return;

        hepsiniGuncelle(ctx, "Temizleniyor…");

        // Yayın alıcısı kısa ömürlüdür; goAsync ile işi bitirene kadar süre alıyoruz.
        final PendingResult sonuc = goAsync();
        new Thread(() -> {
            String mesaj;
            try {
                if (!Temizleyici.rootVarMi()) {
                    mesaj = "Root izni yok";
                } else {
                    int kod = Temizleyici.temizle();
                    if (kod == 0) {
                        String saat = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                        mesaj = "✓ Temizlendi " + saat;
                    } else {
                        mesaj = "Hata (kod " + kod + ")";
                    }
                }
            } catch (Exception e) {
                mesaj = "Hata: root izni?";
            }
            hepsiniGuncelle(ctx, mesaj);
            sonuc.finish();
        }).start();
    }

    /** Ekrandaki tüm widget kopyalarının durum yazısını günceller. */
    private void hepsiniGuncelle(Context ctx, String durum) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, CleanWidget.class));
        for (int id : ids) {
            mgr.updateAppWidget(id, goruntu(ctx, durum));
        }
    }

    /** Widget görünümünü kurar ve dokunma olayını bağlar. */
    private RemoteViews goruntu(Context ctx, String durum) {
        RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.widget_clean);
        v.setTextViewText(R.id.widgetStatus, durum);

        Intent i = new Intent(ctx, CleanWidget.class);
        i.setAction(ACTION_TEMIZLE);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, i, flags);
        v.setOnClickPendingIntent(R.id.widgetRoot, pi);

        return v;
    }
}
