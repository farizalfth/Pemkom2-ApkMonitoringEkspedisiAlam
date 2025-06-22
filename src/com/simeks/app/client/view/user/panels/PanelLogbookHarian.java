package com.simeks.app.client.view.user.panels;

import com.simeks.app.client.service.ClientService;
import com.simeks.app.shared.model.*;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class PanelLogbookHarian extends JPanel {
    private JTextArea txtCatatan;
    private User currentUser;

    public PanelLogbookHarian(User user) {
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Tulis catatan perjalanan harian Anda:");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        add(lblTitle, BorderLayout.NORTH);

        txtCatatan = new JTextArea();
        txtCatatan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtCatatan.setLineWrap(true);
        txtCatatan.setWrapStyleWord(true);
        add(new JScrollPane(txtCatatan), BorderLayout.CENTER);

        JButton btnSimpan = new JButton("Simpan Logbook");
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSimpan.addActionListener(e -> simpanLogbook());
        add(btnSimpan, BorderLayout.SOUTH);
    }
    
    private void simpanLogbook() {
        String catatan = txtCatatan.getText();
        if (catatan.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Catatan tidak boleh kosong.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Logbook log = new Logbook(currentUser.getId(), catatan);
            ClientService.getInstance().getRepository().submitLogbook(log);
            JOptionPane.showMessageDialog(this, "Logbook berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            txtCatatan.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mengirim logbook ke server.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}