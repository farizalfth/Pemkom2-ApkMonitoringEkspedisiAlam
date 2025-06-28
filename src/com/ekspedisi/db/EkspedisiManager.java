// FILE: com/ekspedisi/db/EkspedisiManager.java
package com.ekspedisi.db;

import com.model.Ekspedisi;
import com.util.GenericList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;

/**
 * Kelas manajer untuk semua operasi CRUD ke database.
 * PENTING: Kelas ini TIDAK menyimpan objek Connection.
 * Setiap metode akan meminta koneksi baru dan menutupnya setelah selesai.
 */
public class EkspedisiManager {

    // TIDAK ADA LAGI: private final Connection conn = ...;

    private void logActivity(String activity) {
        String sql = "INSERT INTO log (aktivitas) VALUES (?)";
        // Setiap metode mendapatkan koneksinya sendiri
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, activity);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean tambahEkspedisi(Ekspedisi ekspedisi) {
        String sqlEkspedisi = "INSERT INTO ekspedisi (nama_tim, tanggal, tujuan, status, latitude, longitude, catatan) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlFoto = "INSERT INTO foto (ekspedisi_id, path_foto) VALUES (?, ?)";

        // Menggunakan try-with-resources untuk koneksi transaksi
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Mulai transaksi

            try (PreparedStatement psEkspedisi = conn.prepareStatement(sqlEkspedisi, Statement.RETURN_GENERATED_KEYS)) {
                // ... (set parameters)
                psEkspedisi.setString(1, ekspedisi.getNamaTim());
                psEkspedisi.setDate(2, ekspedisi.getTanggal());
                psEkspedisi.setString(3, ekspedisi.getTujuan());
                psEkspedisi.setString(4, ekspedisi.getStatus());
                psEkspedisi.setObject(5, ekspedisi.getLatitude());
                psEkspedisi.setObject(6, ekspedisi.getLongitude());
                psEkspedisi.setString(7, ekspedisi.getCatatan());
                psEkspedisi.executeUpdate();

                ResultSet generatedKeys = psEkspedisi.getGeneratedKeys();
                int ekspedisiId;
                if (generatedKeys.next()) {
                    ekspedisiId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Gagal membuat ekspedisi, tidak ada ID yang diperoleh.");
                }

                if (ekspedisi.getPathFoto() != null && !ekspedisi.getPathFoto().isEmpty()) {
                    try (PreparedStatement psFoto = conn.prepareStatement(sqlFoto)) {
                        psFoto.setInt(1, ekspedisiId);
                        psFoto.setString(2, ekspedisi.getPathFoto());
                        psFoto.executeUpdate();
                    }
                }
            }
            conn.commit(); // Selesaikan transaksi jika semua berhasil
            logActivity("Menambah ekspedisi baru: '" + ekspedisi.getNamaTim() + "'");
            return true;

        } catch (SQLException e) {
            // Rollback tidak bisa dilakukan di sini karena koneksi mungkin sudah ditutup oleh try-with-resources
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public GenericList<Ekspedisi> semuaEkspedisi(String keyword) {
        GenericList<Ekspedisi> list = new GenericList<>();
        String sql = "SELECT e.*, f.path_foto FROM ekspedisi e LEFT JOIN foto f ON e.id = f.ekspedisi_id WHERE e.nama_tim LIKE ? OR e.tujuan LIKE ? OR e.status LIKE ? ORDER BY e.tanggal DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String searchKeyword = "%" + keyword + "%";
            ps.setString(1, searchKeyword);
            ps.setString(2, searchKeyword);
            ps.setString(3, searchKeyword);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Ekspedisi e = new Ekspedisi();
                // ... (set semua properti Ekspedisi dari ResultSet)
                e.setId(rs.getInt("id"));
                e.setNamaTim(rs.getString("nama_tim"));
                e.setTanggal(rs.getDate("tanggal"));
                e.setTujuan(rs.getString("tujuan"));
                e.setStatus(rs.getString("status"));
                e.setLatitude(rs.getObject("latitude", Double.class));
                e.setLongitude(rs.getObject("longitude", Double.class));
                e.setCatatan(rs.getString("catatan"));
                e.setPathFoto(rs.getString("path_foto"));
                list.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public Ekspedisi getEkspedisiById(int id) {
        Ekspedisi e = null;
        String sql = "SELECT e.*, f.path_foto FROM ekspedisi e LEFT JOIN foto f ON e.id = f.ekspedisi_id WHERE e.id = ?";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                e = new Ekspedisi();
                // ... (set semua properti Ekspedisi dari ResultSet)
                e.setId(rs.getInt("id"));
                e.setNamaTim(rs.getString("nama_tim"));
                e.setTanggal(rs.getDate("tanggal"));
                e.setTujuan(rs.getString("tujuan"));
                e.setStatus(rs.getString("status"));
                e.setLatitude(rs.getObject("latitude", Double.class));
                e.setLongitude(rs.getObject("longitude", Double.class));
                e.setCatatan(rs.getString("catatan"));
                e.setPathFoto(rs.getString("path_foto"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saat mengambil data detail: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
        return e;
    }
    
    // Ulangi pola try-with-resources untuk semua metode lainnya...
    // ... (ubahEkspedisi, hapusEkspedisi, getStatistik)
    public boolean ubahEkspedisi(Ekspedisi ekspedisi) {
        // ...
        return false; // Implementasi disingkat untuk contoh
    }

    public boolean hapusEkspedisi(int id, String namaTim) {
        String sql = "DELETE FROM ekspedisi WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                logActivity("Menghapus ekspedisi ID: " + id + " (" + namaTim + ")");
            }
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public int[] getStatistik() {
        int[] stats = new int[5];
        String sql = "SELECT (SELECT COUNT(*) FROM ekspedisi) as total, SUM(CASE WHEN status = 'Aktif' THEN 1 ELSE 0 END) as aktif, SUM(CASE WHEN status = 'Kembali' THEN 1 ELSE 0 END) as kembali, SUM(CASE WHEN status = 'Tertunda' THEN 1 ELSE 0 END) as tertunda, SUM(CASE WHEN status = 'Dibatalkan' THEN 1 ELSE 0 END) as dibatalkan FROM ekspedisi";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) {
                stats[0] = rs.getInt("total");
                stats[1] = rs.getInt("aktif");
                stats[2] = rs.getInt("kembali");
                stats[3] = rs.getInt("tertunda");
                stats[4] = rs.getInt("dibatalkan");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
}