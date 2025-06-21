package com.simeks.server;

import com.simeks.shared.Lokasi;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class LogReportGenerator {
    private final LokasiDAO lokasiDAO = new LokasiDAO();

    public void generateHtmlFile(ServerDashboard dashboard) {
        try {
            List<Lokasi> semuaLog = lokasiDAO.findAll();
            
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang=\"id\">\n");
            html.append("<head>\n");
            html.append("    <meta charset=\"UTF-8\">\n");
            html.append("    <title>Laporan Log Ekspedisi</title>\n");
            html.append("    <style>\n");
            html.append("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 2em; background-color: #f9f9f9; color: #333; }\n");
            html.append("        h1 { color: #0056b3; border-bottom: 2px solid #0056b3; padding-bottom: 10px; }\n");
            html.append("        table { width: 100%; border-collapse: collapse; margin-top: 20px; box-shadow: 0 2px 15px rgba(0,0,0,0.1); }\n");
            html.append("        th, td { border: 1px solid #ddd; padding: 12px 15px; text-align: left; }\n");
            html.append("        th { background-color: #007bff; color: white; font-weight: bold; }\n");
            html.append("        tr:nth-child(even) { background-color: #f2f2f2; }\n");
            html.append("        tr:hover { background-color: #e2e6ea; }\n");
            html.append("    </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            
            html.append("    <h1>Laporan Log Ekspedisi</h1>\n");
            
            if (semuaLog.isEmpty()) {
                html.append("    <p>Tidak ada data log yang tersimpan di database.</p>\n");
            } else {
                html.append("    <table>\n");
                html.append("        <tr>\n");
                html.append("            <th>ID Tim</th>\n");
                html.append("            <th>Nama Gunung</th>\n");
                html.append("            <th>Jalur Pendakian</th>\n");
                html.append("            <th>Basecamp</th>\n");
                html.append("            <th>Timestamp</th>\n");
                html.append("        </tr>\n");

                // Pemanggilan escapeHtml() telah dihapus dari sini
                for (Lokasi log : semuaLog) {
                    html.append("        <tr>\n");
                    html.append("            <td>").append(log.getIdTim()).append("</td>\n");
                    html.append("            <td>").append(log.getNamaGunung()).append("</td>\n");
                    html.append("            <td>").append(log.getJalurPendakian()).append("</td>\n");
                    html.append("            <td>").append(log.getBasecamp()).append("</td>\n");
                    html.append("            <td>").append(log.getTimestamp().toString()).append("</td>\n");
                    html.append("        </tr>\n");
                }
                html.append("    </table>\n");
            }

            html.append("</body>\n</html>");
            
            try (FileWriter writer = new FileWriter("laporan_ekspedisi.html")) {
                writer.write(html.toString());
                dashboard.addLog("REPORT: SUKSES! File 'laporan_ekspedisi.html' telah diperbarui.");
            } catch (IOException e) {
                dashboard.addLog("REPORT: Gagal menulis file HTML: " + e.getMessage());
            }
        } catch (SQLException e) {
            dashboard.addLog("REPORT: Gagal mengambil data dari DB: " + e.getMessage());
        }
    }
    
    // Metode escapeHtml() telah dihapus dari sini
}