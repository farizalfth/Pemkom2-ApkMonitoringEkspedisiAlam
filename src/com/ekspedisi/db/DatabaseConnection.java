package com.ekspedisi.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * Kelas pabrik (factory) untuk membuat koneksi BARU ke database.
 * KELAS INI TIDAK MENYIMPAN OBJEK KONEKSI (TIDAK SINGLETON).
 * Ini adalah kunci untuk mencegah error "connection closed".
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/db_ekspedisi?useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    /**
     * Membuat dan mengembalikan sebuah koneksi BARU setiap kali dipanggil.
     * @return Objek Connection yang baru, atau program akan keluar jika gagal.
     */
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Setiap panggilan akan membuat objek koneksi yang benar-benar baru.
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, "Koneksi ke database gagal: " + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0); // Keluar dari aplikasi jika database tidak bisa diakses.
            return null;
        }
    }
}