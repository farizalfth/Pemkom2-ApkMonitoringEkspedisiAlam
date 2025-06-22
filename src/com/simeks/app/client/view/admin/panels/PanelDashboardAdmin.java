package com.simeks.app.client.view.admin.panels;

import com.simeks.app.client.service.ClientService;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class PanelDashboardAdmin extends JPanel {
    private JLabel lblEkspedisi, lblPeserta;

    public PanelDashboardAdmin() {
        initComponents();
        loadData();
    }
    
    private void initComponents(){
        setLayout(new GridLayout(4, 1, 10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("Ringkasan Sistem", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));

        lblEkspedisi = new JLabel("Jumlah Ekspedisi Aktif: Memuat...", SwingConstants.CENTER);
        lblEkspedisi.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        
        lblPeserta = new JLabel("Jumlah Peserta Terdaftar: Memuat...", SwingConstants.CENTER);
        lblPeserta.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        add(title);
        add(lblEkspedisi);
        add(lblPeserta);
    }

    private void loadData() {
        try {
            Map<String, Integer> stats = ClientService.getInstance().getRepository().getAdminDashboardStats();
            lblEkspedisi.setText("Jumlah Ekspedisi Aktif: " + stats.get("ekspedisiAktif"));
            lblPeserta.setText("Jumlah Peserta Terdaftar: " + stats.get("totalPeserta"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat statistik.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}