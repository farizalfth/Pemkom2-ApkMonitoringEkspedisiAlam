package com.simeks.app.client.view.admin.panels;

import com.simeks.app.client.service.ClientService;
import com.simeks.app.shared.model.User;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class PanelManajemenPengguna extends JPanel {
    private JTable table;
    private DefaultTableModel model;

    public PanelManajemenPengguna() {
        initComponents();
        // Langsung panggil loadData() saat panel dibuat
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        String[] columnNames = {"ID", "Username", "Nama Lengkap", "Role"};
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);
        
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel();
        JButton btnRefresh = new JButton("Refresh Data");
        btnRefresh.addActionListener(e -> loadData()); // Aksi untuk me-refresh
        bottomPanel.add(btnRefresh);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void loadData() {
        try {
            // Memanggil service untuk mendapatkan semua pengguna dari server
            List<User> users = ClientService.getInstance().getRepository().getAllUsers();
            
            model.setRowCount(0); // Kosongkan tabel sebelum diisi data baru
            
            // Isi tabel dengan data yang diterima dari server
            for (User u : users) {
                model.addRow(new Object[]{u.getId(), u.getUsername(), u.getFullName(), u.getRole()});
            }
        } catch (Exception e) {
            // Tampilkan pesan error jika terjadi masalah
            JOptionPane.showMessageDialog(this, "Gagal memuat data pengguna.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Cetak detail error ke console untuk debugging
        }
    }
}