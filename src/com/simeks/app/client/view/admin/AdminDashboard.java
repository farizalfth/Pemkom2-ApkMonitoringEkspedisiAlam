package com.simeks.app.client.view.admin;

import com.simeks.app.client.view.admin.panels.*;
import com.simeks.app.shared.model.User;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class AdminDashboard extends JFrame {
    public AdminDashboard(User admin) {
        super("Admin Dashboard - " + admin.getFullName());
        initComponents(admin);
    }

    private void initComponents(User admin) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Menambahkan semua panel fitur sebagai tab
        tabbedPane.addTab("Dashboard", new PanelDashboardAdmin());
        tabbedPane.addTab("Manajemen Pengguna", new PanelManajemenPengguna());
        tabbedPane.addTab("Manajemen Ekspedisi", new PanelPlaceholder("Fitur Manajemen Ekspedisi"));
        tabbedPane.addTab("Monitoring Real-time", new PanelPlaceholder("Fitur Monitoring Real-time"));
        tabbedPane.addTab("Validasi Logbook", new PanelPlaceholder("Fitur Validasi Logbook"));
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    private static class PanelPlaceholder extends Component {

        public PanelPlaceholder(String fitur_Manajemen_Ekspedisi) {
        }
    }
}