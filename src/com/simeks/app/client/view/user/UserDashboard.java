package com.simeks.app.client.view.user;

import com.simeks.app.client.view.user.panels.PanelBerandaUser;
import com.simeks.app.client.view.user.panels.PanelJalurEkspedisi;
import com.simeks.app.client.view.user.panels.PanelKompasCuaca;
import com.simeks.app.client.view.user.panels.PanelLogbookHarian;
import com.simeks.app.client.view.user.panels.PanelNotifikasiUser;
import com.simeks.app.client.view.user.panels.PanelPanduanPendakian;
import com.simeks.app.client.view.user.panels.PanelProfilUser;
import com.simeks.app.client.view.user.panels.PanelRiwayatPerjalanan;
import com.simeks.app.client.view.user.panels.PanelUploadDokumentasi;
import com.simeks.app.shared.model.User;
import java.awt.BorderLayout;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

/**
 * UserDashboard.java
 * Frame utama untuk peserta ekspedisi (user), berisi semua panel fitur dalam bentuk tab.
 */
public class UserDashboard extends javax.swing.JFrame {

    /**
     * Membuat UserDashboard baru
     * @param user User (peserta) yang sedang login.
     */
    public UserDashboard(User user) {
        initComponents(user);

        // Konfigurasi dasar window
        setTitle("Dashboard Peserta - Selamat Datang, " + user.getFullName());
        setSize(900, 700); // Ukuran yang lebih sesuai untuk user
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * Inisialisasi semua komponen GUI untuk dashboard user.
     * @param user User (peserta) yang sedang login.
     */
    private void initComponents(User user) {
        // Menggunakan BorderLayout untuk menempatkan JTabbedPane
        setLayout(new BorderLayout());

        // Membuat container untuk semua tab fitur
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new java.awt.Font("Segoe UI", 0, 14));

        // 1. Menambahkan Panel Beranda
        tabbedPane.addTab("Beranda", new PanelBerandaUser(user));
        
        // 2. Menambahkan Panel Jalur Ekspedisi
        tabbedPane.addTab("Jalur Ekspedisi", new PanelJalurEkspedisi());
        
        // 3. Menambahkan Panel Kompas & Cuaca
        tabbedPane.addTab("Kompas & Cuaca", new PanelKompasCuaca());
        
        // 4. Menambahkan Panel Logbook Harian
        tabbedPane.addTab("Logbook Harian", new PanelLogbookHarian(user));
        
        // 5. Menambahkan Panel Riwayat Perjalanan
        tabbedPane.addTab("Riwayat Perjalanan", new PanelRiwayatPerjalanan());
        
        // 6. Menambahkan Panel Panduan Pendakian
        tabbedPane.addTab("Panduan Pendakian", new PanelPanduanPendakian());
        
        // 7. Menambahkan Panel Notifikasi
        tabbedPane.addTab("Notifikasi", new PanelNotifikasiUser());
        
        // 8. Menambahkan Panel Profil
        tabbedPane.addTab("Profil", new PanelProfilUser(user));

        // 9. Menambahkan Panel Upload Dokumentasi
        tabbedPane.addTab("Upload Dokumentasi", new PanelUploadDokumentasi());

        // Menambahkan JTabbedPane yang sudah terisi ke dalam frame utama
        add(tabbedPane, BorderLayout.CENTER);
    }
}