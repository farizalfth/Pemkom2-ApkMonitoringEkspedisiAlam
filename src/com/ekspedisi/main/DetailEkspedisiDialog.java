// FILE: com/main/DetailEkspedisiDialog.java
package com.ekspedisi.main;

import com.ekspedisi.model.Ekspedisi;
import com.ekspedisi.util.I18n;
import java.awt.*;
import java.net.URI;
import java.util.Locale;
import javax.swing.*;

/**
 * Dialog read-only untuk menampilkan detail lengkap sebuah Ekspedisi.
 * Termasuk menampilkan foto dan tombol untuk membuka lokasi di Google Maps.
 */
public class DetailEkspedisiDialog extends JDialog {

    private final Ekspedisi ekspedisi;

    public DetailEkspedisiDialog(Frame parent, Ekspedisi ekspedisi) {
        super(parent, true); // Modal
        this.ekspedisi = ekspedisi;
        initUI();
    }

    private void initUI() {
        setTitle(I18n.getString("detail.title") + " - " + ekspedisi.getNamaTim());
        setSize(500, 650);
        setResizable(false);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        // Panel Foto
        JPanel photoPanel = new JPanel(new BorderLayout());
        photoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel lblFoto = new JLabel();
        lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblFoto.setPreferredSize(new Dimension(250, 250));
        lblFoto.setBorder(BorderFactory.createEtchedBorder());
        
        // [IMPLEMENTASI] Multimedia: Muat gambar dari folder 'src/images'
        if (ekspedisi.getPathFoto() != null && !ekspedisi.getPathFoto().isEmpty()) {
            try {
                // Menggunakan getResource untuk path yang bekerja baik di dalam JAR maupun di IDE
                java.net.URL imgUrl = getClass().getResource("/images/" + ekspedisi.getPathFoto());
                if (imgUrl != null) {
                    ImageIcon icon = new ImageIcon(imgUrl);
                    Image image = icon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
                    lblFoto.setIcon(new ImageIcon(image));
                } else {
                     lblFoto.setText("Foto tidak ditemukan");
                }
            } catch (Exception e) {
                lblFoto.setText("Gagal memuat foto");
                e.printStackTrace();
            }
        } else {
            lblFoto.setText("Tidak ada foto");
        }
        photoPanel.add(lblFoto, BorderLayout.CENTER);
        add(photoPanel, BorderLayout.NORTH);

        // Panel Detail (GridBagLayout untuk penataan rapi)
        JPanel detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Menambahkan baris detail
        int y = 0;
        addDetailRow(detailPanel, gbc, y++, I18n.getString("form.team_name"), ekspedisi.getNamaTim());
        addDetailRow(detailPanel, gbc, y++, I18n.getString("form.destination"), ekspedisi.getTujuan());
        addDetailRow(detailPanel, gbc, y++, I18n.getString("form.date"), ekspedisi.getTanggal().toString());
        addDetailRow(detailPanel, gbc, y++, I18n.getString("form.status"), ekspedisi.getStatus());
        addDetailRow(detailPanel, gbc, y++, I18n.getString("form.latitude"), String.valueOf(ekspedisi.getLatitude()));
        addDetailRow(detailPanel, gbc, y++, I18n.getString("form.longitude"), String.valueOf(ekspedisi.getLongitude()));
        addDetailRow(detailPanel, gbc, y++, I18n.getString("form.notes"), "<html><p style='width:300px'>" + (ekspedisi.getCatatan() != null ? ekspedisi.getCatatan() : "") + "</p></html>");

        add(detailPanel, BorderLayout.CENTER);

        // Panel Tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnMap = new JButton(I18n.getString("detail.map.button"));
        JButton btnClose = new JButton(I18n.getString("detail.close.button"));
        
        buttonPanel.add(btnMap);
        buttonPanel.add(btnClose);
        add(buttonPanel, BorderLayout.SOUTH);

        // Listeners
        btnClose.addActionListener(e -> dispose());
        btnMap.addActionListener(e -> openMap());
    }
    
    // Metode helper untuk menambahkan baris label:nilai
    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int y, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0.2; // Alokasi lebar untuk label
        JLabel lbl = new JLabel("<html><b>" + label + ":</b></html>");
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8; // Alokasi lebar untuk nilai
        panel.add(new JLabel(value), gbc);
    }

    private void openMap() {
        if (ekspedisi.getLatitude() != null && ekspedisi.getLongitude() != null) {
            // Memeriksa apakah Desktop API didukung
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    String url = String.format(Locale.US, "https://www.google.com/maps/search/?api=1&query=%f,%f",
                            ekspedisi.getLatitude(), ekspedisi.getLongitude());
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal membuka browser.", I18n.getString("msg.error.title"), JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Data koordinat tidak tersedia.", I18n.getString("msg.info.title"), JOptionPane.INFORMATION_MESSAGE);
        }
    }
}