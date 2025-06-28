// FILE: com/ekspedisi/db/EkspedisiManager.java
package com.ekspedisi.db;

import com.ekspedisi.model.AnggotaTim;
import com.ekspedisi.model.Ekspedisi;
import com.util.GenericList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Kelas manajer untuk semua operasi CRUD ke database.
 * Versi ini sudah mendukung CRUD untuk Anggota Tim.
 */
public class EkspedisiManager {

    private void logActivity(String activity) {
        String sql = "INSERT INTO log (aktivitas) VALUES (?)";
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
        String sqlAnggota = "INSERT INTO anggota_tim (ekspedisi_id, nama_anggota, jenis_kelamin, no_tlp, alamat) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert data ekspedisi utama
                try (PreparedStatement psEkspedisi = conn.prepareStatement(sqlEkspedisi, Statement.RETURN_GENERATED_KEYS)) {
                    psEkspedisi.setString(1, ekspedisi.getNamaTim());
                    psEkspedisi.setDate(2, ekspedisi.getTanggal());
                    psEkspedisi.setString(3, ekspedisi.getTujuan());
                    psEkspedisi.setString(4, ekspedisi.getStatus());
                    psEkspedisi.setObject(5, ekspedisi.getLatitude());
                    psEkspedisi.setObject(6, ekspedisi.getLongitude());
                    psEkspedisi.setString(7, ekspedisi.getCatatan());
                    psEkspedisi.executeUpdate();
                    try (ResultSet generatedKeys = psEkspedisi.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            ekspedisi.setId(generatedKeys.getInt(1));
                        } else {
                            throw new SQLException("Gagal membuat ekspedisi, tidak ada ID yang diperoleh.");
                        }
                    }
                }

                // Insert foto jika ada
                if (ekspedisi.getPathFoto() != null && !ekspedisi.getPathFoto().isEmpty()) {
                    try (PreparedStatement psFoto = conn.prepareStatement(sqlFoto)) {
                        psFoto.setInt(1, ekspedisi.getId());
                        psFoto.setString(2, ekspedisi.getPathFoto());
                        psFoto.executeUpdate();
                    }
                }

                // Insert anggota tim menggunakan batch
                if (!ekspedisi.getAnggota().isEmpty()) {
                    try (PreparedStatement psAnggota = conn.prepareStatement(sqlAnggota)) {
                        for (AnggotaTim anggota : ekspedisi.getAnggota()) {
                            psAnggota.setInt(1, ekspedisi.getId());
                            psAnggota.setString(2, anggota.getNama());
                            psAnggota.setString(3, anggota.getJenisKelamin()); // Kolom baru
                            psAnggota.setString(4, anggota.getNoTlp());
                            psAnggota.setString(5, anggota.getAlamat());
                            psAnggota.addBatch();
                        }
                        psAnggota.executeBatch();
                    }
                }

                conn.commit();
                logActivity("Menambah ekspedisi baru: '" + ekspedisi.getNamaTim() + "'");
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error database saat menambah data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean ubahEkspedisi(Ekspedisi ekspedisi) {
        String sqlEkspedisi = "UPDATE ekspedisi SET nama_tim=?, tanggal=?, tujuan=?, status=?, latitude=?, longitude=?, catatan=? WHERE id=?";
        String sqlFotoUpdate = "UPDATE foto SET path_foto=? WHERE ekspedisi_id=?";
        String sqlFotoInsert = "INSERT INTO foto (ekspedisi_id, path_foto) VALUES (?, ?)";
        String sqlCheckFoto = "SELECT id FROM foto WHERE ekspedisi_id=?";
        String sqlDeleteAnggota = "DELETE FROM anggota_tim WHERE ekspedisi_id = ?";
        String sqlInsertAnggota = "INSERT INTO anggota_tim (ekspedisi_id, nama_anggota, jenis_kelamin, no_tlp, alamat) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update tabel ekspedisi
                try (PreparedStatement psEkspedisi = conn.prepareStatement(sqlEkspedisi)) {
                    psEkspedisi.setString(1, ekspedisi.getNamaTim()); psEkspedisi.setDate(2, ekspedisi.getTanggal()); psEkspedisi.setString(3, ekspedisi.getTujuan()); psEkspedisi.setString(4, ekspedisi.getStatus()); psEkspedisi.setObject(5, ekspedisi.getLatitude()); psEkspedisi.setObject(6, ekspedisi.getLongitude()); psEkspedisi.setString(7, ekspedisi.getCatatan()); psEkspedisi.setInt(8, ekspedisi.getId());
                    psEkspedisi.executeUpdate();
                }

                // Handle logika foto
                if (ekspedisi.getPathFoto() != null && !ekspedisi.getPathFoto().isEmpty()) {
                    boolean fotoExists = false;
                    try (PreparedStatement psCheck = conn.prepareStatement(sqlCheckFoto)) {
                        psCheck.setInt(1, ekspedisi.getId());
                        try (ResultSet rs = psCheck.executeQuery()) {
                            if (rs.next()) fotoExists = true;
                        }
                    }
                    if (fotoExists) {
                        try (PreparedStatement psFoto = conn.prepareStatement(sqlFotoUpdate)) {
                            psFoto.setString(1, ekspedisi.getPathFoto()); psFoto.setInt(2, ekspedisi.getId());
                            psFoto.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement psFoto = conn.prepareStatement(sqlFotoInsert)) {
                            psFoto.setInt(1, ekspedisi.getId()); psFoto.setString(2, ekspedisi.getPathFoto());
                            psFoto.executeUpdate();
                        }
                    }
                }
                
                // Sinkronisasi anggota tim: Hapus semua yang lama...
                try (PreparedStatement psDelete = conn.prepareStatement(sqlDeleteAnggota)) {
                    psDelete.setInt(1, ekspedisi.getId());
                    psDelete.executeUpdate();
                }

                // ...lalu masukkan semua anggota tim yang baru dari form
                if (!ekspedisi.getAnggota().isEmpty()) {
                    try (PreparedStatement psInsert = conn.prepareStatement(sqlInsertAnggota)) {
                        for (AnggotaTim anggota : ekspedisi.getAnggota()) {
                            psInsert.setInt(1, ekspedisi.getId());
                            psInsert.setString(2, anggota.getNama());
                            psInsert.setString(3, anggota.getJenisKelamin()); // Kolom baru
                            psInsert.setString(4, anggota.getNoTlp());
                            psInsert.setString(5, anggota.getAlamat());
                            psInsert.addBatch();
                        }
                        psInsert.executeBatch();
                    }
                }

                conn.commit();
                logActivity("Mengubah data ekspedisi ID: " + ekspedisi.getId() + " (" + ekspedisi.getNamaTim() + ")");
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error database saat mengubah data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
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
                e.setId(rs.getInt("id")); e.setNamaTim(rs.getString("nama_tim")); e.setTanggal(rs.getDate("tanggal")); e.setTujuan(rs.getString("tujuan")); e.setStatus(rs.getString("status")); e.setLatitude(rs.getObject("latitude", Double.class)); e.setLongitude(rs.getObject("longitude", Double.class)); e.setCatatan(rs.getString("catatan")); e.setPathFoto(rs.getString("path_foto"));
                
                // Jika ekspedisi ditemukan, ambil data anggotanya
                String sqlAnggota = "SELECT * FROM anggota_tim WHERE ekspedisi_id = ?";
                try (PreparedStatement psAnggota = conn.prepareStatement(sqlAnggota)) {
                    psAnggota.setInt(1, id);
                    ResultSet rsAnggota = psAnggota.executeQuery();
                    List<AnggotaTim> daftarAnggota = new ArrayList<>();
                    while(rsAnggota.next()) {
                        AnggotaTim anggota = new AnggotaTim();
                        anggota.setId(rsAnggota.getInt("id")); 
                        anggota.setEkspedisiId(rsAnggota.getInt("ekspedisi_id")); 
                        anggota.setNama(rsAnggota.getString("nama_anggota")); 
                        anggota.setJenisKelamin(rsAnggota.getString("jenis_kelamin")); // Kolom baru
                        anggota.setNoTlp(rsAnggota.getString("no_tlp")); 
                        anggota.setAlamat(rsAnggota.getString("alamat"));
                        daftarAnggota.add(anggota);
                    }
                    e.setAnggota(daftarAnggota);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return e;
    }

    public GenericList<Ekspedisi> semuaEkspedisi(String keyword) {
        GenericList<Ekspedisi> list = new GenericList<>();
        String sql = "SELECT e.*, f.path_foto FROM ekspedisi e LEFT JOIN foto f ON e.id = f.ekspedisi_id WHERE e.nama_tim LIKE ? OR e.tujuan LIKE ? OR e.status LIKE ? ORDER BY e.tanggal DESC, e.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String searchKeyword = "%" + keyword + "%";
            ps.setString(1, searchKeyword);
            ps.setString(2, searchKeyword);
            ps.setString(3, searchKeyword);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Ekspedisi e = new Ekspedisi();
                e.setId(rs.getInt("id")); e.setNamaTim(rs.getString("nama_tim")); e.setTanggal(rs.getDate("tanggal")); e.setTujuan(rs.getString("tujuan")); e.setStatus(rs.getString("status")); e.setLatitude(rs.getObject("latitude", Double.class)); e.setLongitude(rs.getObject("longitude", Double.class)); e.setCatatan(rs.getString("catatan")); e.setPathFoto(rs.getString("path_foto"));
                list.add(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
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
                stats[0] = rs.getInt("total"); stats[1] = rs.getInt("aktif"); stats[2] = rs.getInt("kembali"); stats[3] = rs.getInt("tertunda"); stats[4] = rs.getInt("dibatalkan");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
}