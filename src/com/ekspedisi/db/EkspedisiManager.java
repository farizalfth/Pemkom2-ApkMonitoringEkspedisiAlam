// FILE: com/ekspedisi/db/EkspedisiManager.java
package com.ekspedisi.db;

import com.ekspedisi.model.AnggotaTim;
import com.ekspedisi.model.Ekspedisi;
import com.ekspedisi.util.GenericList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * Kelas manajer untuk semua operasi CRUD ke database.
 * Versi final ini mencakup semua fungsionalitas, termasuk query untuk kalender dan leaderboard.
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
        String sqlEkspedisi = "INSERT INTO ekspedisi (nama_tim, tujuan, jenis_pendakian, tanggal, status, latitude, longitude, catatan) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlFoto = "INSERT INTO foto (ekspedisi_id, path_foto) VALUES (?, ?)";
        String sqlAnggota = "INSERT INTO anggota_tim (ekspedisi_id, nama_anggota, jenis_kelamin, no_tlp, alamat) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psEkspedisi = conn.prepareStatement(sqlEkspedisi, Statement.RETURN_GENERATED_KEYS)) {
                    psEkspedisi.setString(1, ekspedisi.getNamaTim()); psEkspedisi.setString(2, ekspedisi.getTujuan()); psEkspedisi.setString(3, ekspedisi.getJenisPendakian()); psEkspedisi.setDate(4, ekspedisi.getTanggal()); psEkspedisi.setString(5, ekspedisi.getStatus()); psEkspedisi.setObject(6, ekspedisi.getLatitude()); psEkspedisi.setObject(7, ekspedisi.getLongitude()); psEkspedisi.setString(8, ekspedisi.getCatatan());
                    psEkspedisi.executeUpdate();
                    try (ResultSet generatedKeys = psEkspedisi.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            ekspedisi.setId(generatedKeys.getInt(1));
                        } else {
                            throw new SQLException("Gagal membuat ekspedisi, tidak ada ID yang diperoleh.");
                        }
                    }
                }
                if (ekspedisi.getPathFoto() != null && !ekspedisi.getPathFoto().isEmpty()) {
                    try (PreparedStatement psFoto = conn.prepareStatement(sqlFoto)) {
                        psFoto.setInt(1, ekspedisi.getId()); psFoto.setString(2, ekspedisi.getPathFoto());
                        psFoto.executeUpdate();
                    }
                }
                if (!ekspedisi.getAnggota().isEmpty()) {
                    try (PreparedStatement psAnggota = conn.prepareStatement(sqlAnggota)) {
                        for (AnggotaTim anggota : ekspedisi.getAnggota()) {
                            psAnggota.setInt(1, ekspedisi.getId()); psAnggota.setString(2, anggota.getNama()); psAnggota.setString(3, anggota.getJenisKelamin()); psAnggota.setString(4, anggota.getNoTlp()); psAnggota.setString(5, anggota.getAlamat());
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
        String sqlEkspedisi = "UPDATE ekspedisi SET nama_tim=?, tujuan=?, jenis_pendakian=?, tanggal=?, status=?, latitude=?, longitude=?, catatan=? WHERE id=?";
        String sqlFotoUpdate = "UPDATE foto SET path_foto=? WHERE ekspedisi_id=?";
        String sqlFotoInsert = "INSERT INTO foto (ekspedisi_id, path_foto) VALUES (?, ?)";
        String sqlCheckFoto = "SELECT id FROM foto WHERE ekspedisi_id=?";
        String sqlDeleteAnggota = "DELETE FROM anggota_tim WHERE ekspedisi_id = ?";
        String sqlInsertAnggota = "INSERT INTO anggota_tim (ekspedisi_id, nama_anggota, jenis_kelamin, no_tlp, alamat) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psEkspedisi = conn.prepareStatement(sqlEkspedisi)) {
                    psEkspedisi.setString(1, ekspedisi.getNamaTim()); psEkspedisi.setString(2, ekspedisi.getTujuan()); psEkspedisi.setString(3, ekspedisi.getJenisPendakian()); psEkspedisi.setDate(4, ekspedisi.getTanggal()); psEkspedisi.setString(5, ekspedisi.getStatus()); psEkspedisi.setObject(6, ekspedisi.getLatitude()); psEkspedisi.setObject(7, ekspedisi.getLongitude()); psEkspedisi.setString(8, ekspedisi.getCatatan()); psEkspedisi.setInt(9, ekspedisi.getId());
                    psEkspedisi.executeUpdate();
                }
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
                try (PreparedStatement psDelete = conn.prepareStatement(sqlDeleteAnggota)) {
                    psDelete.setInt(1, ekspedisi.getId());
                    psDelete.executeUpdate();
                }
                if (!ekspedisi.getAnggota().isEmpty()) {
                    try (PreparedStatement psInsert = conn.prepareStatement(sqlInsertAnggota)) {
                        for (AnggotaTim anggota : ekspedisi.getAnggota()) {
                            psInsert.setInt(1, ekspedisi.getId()); psInsert.setString(2, anggota.getNama());
                            psInsert.setString(3, anggota.getJenisKelamin()); psInsert.setString(4, anggota.getNoTlp());
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
        String sql = "SELECT * FROM ekspedisi e LEFT JOIN foto f ON e.id = f.ekspedisi_id WHERE e.id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                e = new Ekspedisi();
                e.setId(rs.getInt("id")); e.setNamaTim(rs.getString("nama_tim")); e.setTujuan(rs.getString("tujuan"));
                e.setJenisPendakian(rs.getString("jenis_pendakian")); e.setTanggal(rs.getDate("tanggal"));
                e.setStatus(rs.getString("status")); e.setLatitude(rs.getObject("latitude", Double.class));
                e.setLongitude(rs.getObject("longitude", Double.class)); e.setCatatan(rs.getString("catatan"));
                e.setPathFoto(rs.getString("path_foto"));
                
                String sqlAnggota = "SELECT * FROM anggota_tim WHERE ekspedisi_id = ?";
                try (PreparedStatement psAnggota = conn.prepareStatement(sqlAnggota)) {
                    psAnggota.setInt(1, id);
                    ResultSet rsAnggota = psAnggota.executeQuery();
                    List<AnggotaTim> daftarAnggota = new ArrayList<>();
                    while(rsAnggota.next()) {
                        AnggotaTim anggota = new AnggotaTim();
                        anggota.setId(rsAnggota.getInt("id")); anggota.setEkspedisiId(rsAnggota.getInt("ekspedisi_id")); 
                        anggota.setNama(rsAnggota.getString("nama_anggota")); 
                        anggota.setJenisKelamin(rsAnggota.getString("jenis_kelamin")); 
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
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            String searchKeyword = "%" + keyword + "%";
            ps.setString(1, searchKeyword); ps.setString(2, searchKeyword); ps.setString(3, searchKeyword);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Ekspedisi e = new Ekspedisi();
                e.setId(rs.getInt("id")); e.setNamaTim(rs.getString("nama_tim")); e.setTujuan(rs.getString("tujuan"));
                e.setJenisPendakian(rs.getString("jenis_pendakian")); e.setTanggal(rs.getDate("tanggal"));
                e.setStatus(rs.getString("status")); e.setLatitude(rs.getObject("latitude", Double.class));
                e.setLongitude(rs.getObject("longitude", Double.class)); e.setCatatan(rs.getString("catatan"));
                e.setPathFoto(rs.getString("path_foto"));
                list.add(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }
    
    public boolean hapusEkspedisi(int id, String namaTim) {
        String sql = "DELETE FROM ekspedisi WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean success = ps.executeUpdate() > 0;
            if (success) logActivity("Menghapus ekspedisi ID: " + id + " (" + namaTim + ")");
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public int[] getStatistik() {
        int[] stats = new int[5];
        String sql = "SELECT (SELECT COUNT(*) FROM ekspedisi) as total, SUM(CASE WHEN status = 'Aktif' THEN 1 ELSE 0 END) as aktif, SUM(CASE WHEN status = 'Kembali' THEN 1 ELSE 0 END) as kembali, SUM(CASE WHEN status = 'Tertunda' THEN 1 ELSE 0 END) as tertunda, SUM(CASE WHEN status = 'Dibatalkan' THEN 1 ELSE 0 END) as dibatalkan FROM ekspedisi";
        try (Connection conn = DatabaseConnection.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) {
                stats[0] = rs.getInt("total"); stats[1] = rs.getInt("aktif"); stats[2] = rs.getInt("kembali"); stats[3] = rs.getInt("tertunda"); stats[4] = rs.getInt("dibatalkan");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * [METODE BARU] Mengambil semua ekspedisi dalam bulan dan tahun tertentu untuk kalender.
     */
    public Map<Integer, List<String>> getEkspedisiByMonth(int month, int year) {
        Map<Integer, List<String>> schedule = new HashMap<>();
        String sql = "SELECT DAY(tanggal) as hari, nama_tim FROM ekspedisi WHERE MONTH(tanggal) = ? AND YEAR(tanggal) = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int hari = rs.getInt("hari");
                String namaTim = rs.getString("nama_tim");
                schedule.computeIfAbsent(hari, k -> new ArrayList<>()).add(namaTim);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedule;
    }

    /**
     * [LEADERBOARD] Mengambil data tim paling aktif (jumlah ekspedisi terbanyak).
     */
    public Map<String, Integer> getTimTeraktif(int limit) {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT nama_tim, COUNT(*) as jumlah FROM ekspedisi GROUP BY nama_tim ORDER BY jumlah DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.put(rs.getString("nama_tim"), rs.getInt("jumlah"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * [LEADERBOARD] Mengambil data gunung paling populer (paling sering didaki).
     */
    public Map<String, Integer> getGunungTerpopuler(int limit) {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT SUBSTRING_INDEX(tujuan, ',', 1) as gunung, COUNT(*) as jumlah FROM ekspedisi GROUP BY gunung ORDER BY jumlah DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.put(rs.getString("gunung"), rs.getInt("jumlah"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * [LEADERBOARD] Mengambil data tim dengan jumlah total anggota terbanyak.
     */
    public Map<String, Integer> getTimDenganAnggotaTerbanyak(int limit) {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT e.nama_tim, COUNT(a.id) as jumlah_anggota FROM ekspedisi e JOIN anggota_tim a ON e.id = a.ekspedisi_id GROUP BY e.nama_tim ORDER BY jumlah_anggota DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.put(rs.getString("nama_tim"), rs.getInt("jumlah_anggota"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
}