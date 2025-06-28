// FILE: com/main/Main.java
package com.ekspedisi.main;

import com.ekspedisi.main.LoginForm;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Kelas utama (entry point) untuk menjalankan aplikasi.
 * Tugasnya sederhana: mengatur Look and Feel dan menjalankan LoginForm di thread yang aman.
 */
public class Main {
    public static void main(String[] args) {
        // Mengatur Look and Feel agar sesuai dengan sistem operasi pengguna.
        // Ini membuat tampilan aplikasi (tombol, jendela, dll.) terasa lebih 'native'.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Jika gagal, aplikasi tetap berjalan dengan Look and Feel default Java.
            e.printStackTrace();
        }

        // Menjalankan GUI di Event Dispatch Thread (EDT) adalah praktik terbaik di Swing.
        // Ini mencegah masalah konkurensi dan memastikan semua pembaruan UI berjalan lancar.
        SwingUtilities.invokeLater(() -> {
            // Membuat instance LoginForm dan menampilkannya.
            new LoginForm().setVisible(true);
        });
    }
}