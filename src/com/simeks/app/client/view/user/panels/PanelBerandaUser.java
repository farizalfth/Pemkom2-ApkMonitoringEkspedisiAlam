package com.simeks.app.client.view.user.panels;

import com.simeks.app.client.service.ClientService;
import com.simeks.app.shared.model.*;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class PanelBerandaUser extends JPanel {
    public PanelBerandaUser(User user) {
        setLayout(new GridLayout(5, 1, 10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        add(new JLabel("Selamat datang, " + user.getFullName() + "!")).setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        try {
            Ekspedisi ekspedisi = ClientService.getInstance().getRepository().getActiveEkspedisiForUser(user.getId());
            if (ekspedisi != null) {
                add(new JLabel("Ekspedisi Aktif: " + ekspedisi.getNama())).setFont(new Font("Segoe UI", Font.PLAIN, 16));
                add(new JLabel("Jalur: " + ekspedisi.getJalur())).setFont(new Font("Segoe UI", Font.PLAIN, 16));
                add(new JLabel("Status: " + ekspedisi.getStatus())).setFont(new Font("Segoe UI", Font.PLAIN, 16));
            } else {
                add(new JLabel("Anda tidak sedang dalam ekspedisi aktif.")).setFont(new Font("Segoe UI", Font.ITALIC, 16));
            }
        } catch (Exception e) {
            add(new JLabel("Gagal memuat informasi ekspedisi.")).setFont(new Font("Segoe UI", Font.ITALIC, 16));
        }
    }
}