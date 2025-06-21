package com.simeks.server;

import com.simeks.shared.Lokasi;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LokasiDAO implements Repository<Lokasi, Integer> {

    @Override
    public void save(Lokasi entity) throws SQLException {
        // Query dengan kolom basecamp
        String sql = "INSERT INTO lokasi_log(id_tim, nama_gunung, jalur_pendakian, basecamp, timestamp) VALUES(?,?,?,?,?)";
        
        try (Connection conn = KoneksiDB.createConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, entity.getIdTim());
            pstmt.setString(2, entity.getNamaGunung());
            pstmt.setString(3, entity.getJalurPendakian());
            pstmt.setString(4, entity.getBasecamp()); // <-- SET PARAMETER BARU
            pstmt.setString(5, entity.getTimestamp().toString());
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<Lokasi> findAll() throws SQLException {
        List<Lokasi> daftarLog = new ArrayList<>();
        // Query dengan kolom basecamp
        String sql = "SELECT id_tim, nama_gunung, jalur_pendakian, basecamp, timestamp FROM lokasi_log ORDER BY id";
        
        try (Connection conn = KoneksiDB.createConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            
            while (rs.next()) {
                String idTim = rs.getString("id_tim");
                String namaGunung = rs.getString("nama_gunung");
                String jalur = rs.getString("jalur_pendakian");
                String basecamp = rs.getString("basecamp"); // <-- AMBIL DATA BARU
                LocalDateTime timestamp = LocalDateTime.parse(rs.getString("timestamp"));

                // Rekonstruksi objek dengan konstruktor baru
                Lokasi log = new Lokasi(idTim, namaGunung, jalur, basecamp) {
                    @Override public LocalDateTime getTimestamp() { return timestamp; }
                };
                daftarLog.add(log);
            }
        }
        return daftarLog;
    }
}