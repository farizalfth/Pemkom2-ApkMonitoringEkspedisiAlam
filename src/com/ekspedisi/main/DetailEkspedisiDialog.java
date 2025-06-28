// FILE: com/ekspedisi/main/DetailEkspedisiDialog.java
package com.ekspedisi.main;

import com.ekspedisi.model.Ekspedisi;
import com.ekspedisi.util.I18n;
import com.ekspedisi.util.ImageRotator; // Import kelas helper kita

// Import untuk UI, layout, dan event
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

// Import untuk manipulasi gambar, stream, dan URL
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import javax.imageio.ImageIO;

/**
 * Dialog read-only untuk menampilkan detail lengkap sebuah Ekspedisi.
 * Versi ini sudah mendukung rotasi gambar otomatis berdasarkan metadata EXIF.
 */
public class DetailEkspedisiDialog extends JDialog {

    private final Ekspedisi ekspedisi;

    public DetailEkspedisiDialog(Frame parent, Ekspedisi ekspedisi) {
        super(parent, true);
        this.ekspedisi = ekspedisi;
        initUI();
    }

    private void initUI() {
        setTitle("Detail Ekspedisi - " + ekspedisi.getNamaTim());
        setSize(500, 650);
        setResizable(false);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        add(createPhotoPanel(), BorderLayout.NORTH);
        add(createDetailPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createPhotoPanel() {
        JPanel photoPanel = new JPanel(new BorderLayout());
        photoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel lblFoto = new JLabel();
        lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblFoto.setPreferredSize(new Dimension(250, 250));
        lblFoto.setBorder(BorderFactory.createEtchedBorder());
        
        // [IMPLEMENTASI] Multimedia dengan Rotasi EXIF
        if (ekspedisi.getPathFoto() != null && !ekspedisi.getPathFoto().isEmpty()) {
            try {
                URL imgUrl = getClass().getResource("/images/" + ekspedisi.getPathFoto());
                if (imgUrl != null) {
                    BufferedImage originalImage;
                    int orientation = 1;

                    // Langkah 1: Baca orientasi EXIF dari stream baru
                    try (InputStream exifStream = getClass().getResourceAsStream("/images/" + ekspedisi.getPathFoto())) {
                         if(exifStream != null) {
                             orientation = ImageRotator.getExifOrientation(exifStream);
                         }
                    }
                    
                    // Langkah 2: Baca file gambar dari stream baru
                    try (InputStream imageStream = getClass().getResourceAsStream("/images/" + ekspedisi.getPathFoto())) {
                        if (imageStream != null) {
                            originalImage = ImageIO.read(imageStream);
                        } else {
                            originalImage = null;
                        }
                    }

                    if (originalImage != null) {
                        // Langkah 3: Putar gambar jika perlu menggunakan helper kita
                        BufferedImage rotatedImage = ImageRotator.rotateImage(originalImage, orientation);
                        
                        // Langkah 4: Scale gambar agar pas di label
                        Image finalImage = rotatedImage.getScaledInstance(250, 250, Image.SCALE_SMOOTH);
                        lblFoto.setIcon(new ImageIcon(finalImage));
                    } else {
                        lblFoto.setText("Gagal membaca file gambar");
                    }
                } else {
                     lblFoto.setText("File gambar tidak ditemukan");
                }
            } catch (Exception e) {
                lblFoto.setText("Error saat memuat foto");
                e.printStackTrace();
            }
        } else {
            lblFoto.setText("Tidak ada foto");
        }
        
        photoPanel.add(lblFoto, BorderLayout.CENTER);
        return photoPanel;
    }
    
    private JPanel createDetailPanel() {
        JPanel detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
        
        int y = 0;
        addDetailRow(detailPanel, y++, "Nama Tim:", ekspedisi.getNamaTim());
        addDetailRow(detailPanel, y++, "Tujuan:", ekspedisi.getTujuan());
        addDetailRow(detailPanel, y++, "Tanggal:", ekspedisi.getTanggal().toString());
        addDetailRow(detailPanel, y++, "Status:", ekspedisi.getStatus());
        addDetailRow(detailPanel, y++, "Latitude:", String.valueOf(ekspedisi.getLatitude()));
        addDetailRow(detailPanel, y++, "Longitude:", String.valueOf(ekspedisi.getLongitude()));
        // Menggunakan HTML untuk word-wrapping di JLabel
        addDetailRow(detailPanel, y++, "Catatan:", "<html><p style='width:300px'>" + (ekspedisi.getCatatan() != null ? ekspedisi.getCatatan() : "") + "</p></html>");

        return detailPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnMap = new JButton("Buka di Peta");
        JButton btnClose = new JButton("Tutup");
        
        btnMap.addActionListener(e -> openMap());
        btnClose.addActionListener(e -> dispose());
        
        buttonPanel.add(btnMap);
        buttonPanel.add(btnClose);
        return buttonPanel;
    }
    
    private void addDetailRow(JPanel panel, int y, String label, String value) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = y;
        gbcLabel.anchor = GridBagConstraints.FIRST_LINE_END;
        gbcLabel.insets = new Insets(2, 2, 2, 10);
        JLabel lbl = new JLabel("<html><b>" + label + "</b></html>");
        panel.add(lbl, gbcLabel);

        GridBagConstraints gbcValue = new GridBagConstraints();
        gbcValue.gridx = 1;
        gbcValue.gridy = y;
        gbcValue.anchor = GridBagConstraints.FIRST_LINE_START;
        gbcValue.weightx = 1.0;
        gbcValue.fill = GridBagConstraints.HORIZONTAL;
        gbcValue.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel(value != null && !value.isEmpty() ? value : "-"), gbcValue);
    }

    private void openMap() {
        if (ekspedisi.getLatitude() != null && ekspedisi.getLongitude() != null) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    String url = String.format(Locale.US, "https://www.google.com/maps/search/?api=1&query=%f,%f",
                            ekspedisi.getLatitude(), ekspedisi.getLongitude());
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal membuka browser.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Data koordinat tidak tersedia.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}