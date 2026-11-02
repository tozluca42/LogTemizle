package com.example.logtemizle;

import java.io.DataOutputStream;

/**
 * Temizlik işini yapan ortak sınıf.
 * Hem ana ekrandaki buton hem de widget bunu kullanır.
 * Sadece kullanıcı tetiklediğinde çalışır; arka planda kendi kendine çalışmaz.
 */
public class Temizleyici {

    // Temizlenecek paketler. Başka sürüm varsa buraya ekle.
    public static final String[] PAKETLER = {"com.mobile.legends"};

    /** Root izni var mı? */
    public static boolean rootVarMi() {
        try {
            Process p = Runtime.getRuntime().exec("su -c id");
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Önbellek, .log ve crash dosyalarını siler, logcat tamponunu boşaltır.
     * @return çıkış kodu; 0 ise başarılı
     * @throws Exception root yoksa veya komut çalışmazsa
     */
    public static int temizle() throws Exception {
        Process p = Runtime.getRuntime().exec("su");
        DataOutputStream os = new DataOutputStream(p.getOutputStream());

        for (String pkg : PAKETLER) {
            os.writeBytes("am force-stop " + pkg + " 2>/dev/null\n");
            os.writeBytes("rm -rf /data/data/" + pkg + "/cache/* /data/data/" + pkg + "/code_cache/* 2>/dev/null\n");
            os.writeBytes("rm -rf /sdcard/Android/data/" + pkg + "/cache/* 2>/dev/null\n");
            os.writeBytes("find /sdcard/Android/data/" + pkg + "/files -type f \\( -iname '*.log' -o -iname '*crash*' \\) -delete 2>/dev/null\n");
        }
        os.writeBytes("logcat -c\n");
        os.writeBytes("exit\n");
        os.flush();

        return p.waitFor();
    }
}
