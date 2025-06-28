// FILE: com/ekspedisi/util/I18n.java
package com.ekspedisi.util; // Pastikan package-nya ini!

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Kelas helper untuk Internasionalisasi.
 * [IMPLEMENTASI] Internasionalisasi
 */
public class I18n {
    private static ResourceBundle bundle;

    // Blok static initializer ini akan berjalan SEKALI saat kelas I18n pertama kali digunakan.
    // Ini adalah tempat error Anda terjadi.
    static {
        // Bahasa default saat aplikasi pertama kali dijalankan adalah Bahasa Indonesia.
        setLocale(new Locale("in", "ID"));
    }

    /**
     * Mengatur bahasa yang akan digunakan oleh aplikasi.
     * @param locale Objek Locale yang merepresentasikan bahasa (misal: new Locale("en", "US")).
     */
    public static void setLocale(Locale locale) {
        // Baris ini adalah sumber error Anda.
        // Java akan mencari file berdasarkan base name dan locale.
        // Base name: "com.ekspedisi.resources.messages"
        // Locale: in_ID -> akan mencari file "messages_in_ID.properties"
        // di dalam folder "com/ekspedisi/resources/"
        bundle = ResourceBundle.getBundle("com.ekspedisi.resources.messages", locale);
    }

    /**
     * Mengambil string terjemahan berdasarkan key.
     * @param key Kunci dari string yang diinginkan (misal: "login.title").
     * @return String terjemahan, atau "!key!" jika tidak ditemukan.
     */
    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            // Mengembalikan key jika terjemahan tidak ditemukan, agar mudah di-debug.
            return "!" + key + "!";
        }
    }
}