// File: src/com/simeks/client/KlienDashboard.java
package com.simeks.client;

import com.simeks.shared.CryptoUtils;
import com.simeks.shared.Lokasi;
import javax.swing.*;
import java.awt.*;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.crypto.SealedObject;

public class KlienDashboard extends JFrame {

    // Konstanta untuk koneksi ke server
    public static final String SERVER_IP = "127.0.0.1";
    public static final int SERVER_PORT = 9999;
    
    // Deklarasi semua komponen GUI
    private JTextField txtIdTim, txtNamaGunung, txtJalur, txtBasecamp;
    private JButton btnKirim;
    private JTextArea txtLogKlien;

    public KlienDashboard() {
        super("SIMEKS Klien Dashboard");
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 450); // Sedikit lebih tinggi untuk field baru
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Baris 0: ID Tim
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_END;
        mainPanel.add(new JLabel("ID Tim:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.LINE_START;
        txtIdTim = new JTextField("Tim Macan Kumbang", 20);
        mainPanel.add(txtIdTim, gbc);

        // Baris 1: Nama Gunung
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_END;
        mainPanel.add(new JLabel("Nama Gunung:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.LINE_START;
        txtNamaGunung = new JTextField("Gunung Rinjani", 20);
        mainPanel.add(txtNamaGunung, gbc);

        // Baris 2: Jalur Pendakian
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_END;
        mainPanel.add(new JLabel("Jalur Pendakian:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.LINE_START;
        txtJalur = new JTextField("Via Sembalun", 20);
        mainPanel.add(txtJalur, gbc);
        
        // Baris 3: Basecamp (BARU)
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.LINE_END;
        mainPanel.add(new JLabel("Basecamp:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.LINE_START;
        txtBasecamp = new JTextField("Desa Sembalun Lawang", 20);
        mainPanel.add(txtBasecamp, gbc);

        // Baris 4: Tombol Kirim
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.ipady = 10;
        btnKirim = new JButton("Kirim Data");
        btnKirim.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainPanel.add(btnKirim, gbc);

        // Baris 5: Area Log
        gbc.gridy = 5; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        txtLogKlien = new JTextArea(5, 20);
        txtLogKlien.setEditable(false);
        txtLogKlien.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScrollPane = new JScrollPane(txtLogKlien);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log Klien"));
        mainPanel.add(logScrollPane, gbc);

        this.add(mainPanel);
        btnKirim.addActionListener(e -> performSendAction());
    }

    private void performSendAction() {
        String idTim = txtIdTim.getText().trim();
        String namaGunung = txtNamaGunung.getText().trim();
        String jalur = txtJalur.getText().trim();
        String basecamp = txtBasecamp.getText().trim();

        if (idTim.isEmpty() || namaGunung.isEmpty() || jalur.isEmpty() || basecamp.isEmpty()) {
            addLog("ERROR: Semua field harus diisi!");
            return;
        }

        Lokasi logBaru = new Lokasi(idTim, namaGunung, jalur, basecamp);
        
        btnKirim.setEnabled(false);
        btnKirim.setText("Mengirim...");
        kirimData(logBaru);
    }
    
    // Metode untuk menambahkan log ke JTextArea secara thread-safe
    private void addLog(String message) {
        SwingUtilities.invokeLater(() -> {
            txtLogKlien.append(message + "\n");
            txtLogKlien.setCaretPosition(txtLogKlien.getDocument().getLength());
        });
    }

    // Metode untuk mengirim data ke server di background thread
    private void kirimData(Lokasi lokasi) {
        new Thread(() -> {
            try {
                addLog("Menyiapkan data untuk dikirim -> " + lokasi);
                addLog("Mengenkripsi data...");
                SealedObject sealedObject = CryptoUtils.encrypt(lokasi);
                
                addLog("Menghubungkan ke server...");
                try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                     ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
                    
                    oos.writeObject(sealedObject);
                    oos.flush();
                    addLog("SUKSES: Data berhasil dikirim.\n");
                }
            } catch (Exception e) {
                addLog("GAGAL: Tidak dapat mengirim data. " + e.getMessage() + "\n");
            } finally {
                // Kembalikan tombol ke keadaan normal, apapun hasilnya
                SwingUtilities.invokeLater(() -> {
                    btnKirim.setEnabled(true);
                    btnKirim.setText("Kirim Data");
                });
            }
        }).start();
    }
    
    // Metode main untuk menjalankan aplikasi Klien
    public static void main(String[] args) {
        // Atur Look and Feel agar lebih modern
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("Failed to set system look and feel.");
        }

        // Jalankan GUI di Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new KlienDashboard().setVisible(true);
        });
    }
}