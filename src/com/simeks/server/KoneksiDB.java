package com.simeks.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class KoneksiDB {
    
    // URL koneksi ke file database SQLite
    public static final String URL = "jdbc:sqlite:simeks.db";

    /**
     * Membuat dan mengembalikan koneksi BARU ke database.
     * @return sebuah objek Connection yang baru
     * @throws SQLException jika koneksi gagal
     */
    public static Connection createConnection() throws SQLException {
        try {
            // Memastikan driver JDBC untuk SQLite sudah ter-load
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found.", e);
        }
        return DriverManager.getConnection(URL);
    }
    
    /**
     * Metode ini dipanggil sekali saat server start untuk memastikan
     * database dan tabel sudah siap.
     */
    public static void initDatabase() {
        // Gunakan try-with-resources untuk memastikan koneksi dan statement
        // ditutup secara otomatis setelah digunakan.
        try (Connection conn = createConnection();
             Statement stmt = conn.createStatement()) {
            
            // Query untuk membuat tabel jika belum ada.
            // Ini adalah struktur tabel final dengan kolom 'basecamp'.
            String sql = "CREATE TABLE IF NOT EXISTS lokasi_log (\n"
                    + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                    + " id_tim TEXT NOT NULL,\n"
                    + " nama_gunung TEXT NOT NULL,\n"
                    + " jalur_pendakian TEXT NOT NULL,\n"
                    + " basecamp TEXT NOT NULL,\n"
                    + " timestamp TEXT NOT NULL\n"
                    + ");";
            
            stmt.execute(sql);
            System.out.println("Database and table initialized successfully.");
            
        } catch (SQLException e) {
            // Tangani error jika gagal membuat database atau tabel
            System.err.println("Failed to initialize database: " + e.getMessage());
            // Anda bisa melempar exception di sini jika ingin aplikasi berhenti total
            // throw new RuntimeException("Database initialization failed", e);
        }
    }
}