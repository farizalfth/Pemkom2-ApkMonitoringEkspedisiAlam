package com.ekspedisi.main;

// Import dari package proyek yang benar
import com.ekspedisi.db.DatabaseConnection;
import com.ekspedisi.util.I18n;
import com.ekspedisi.util.PasswordUtil;

// Import untuk komponen UI dan event
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

// Import untuk koneksi database
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Frame untuk login admin.
 * Kelas ini bertanggung jawab untuk menampilkan form login, menerima input,
 * dan memverifikasi kredensial pengguna dengan data di database.
 * Ini adalah gerbang masuk (entry point) ke dalam fungsionalitas utama aplikasi.
 */
public class LoginForm extends JFrame {

    // Deklarasi komponen UI sebagai field instance agar bisa diakses di seluruh kelas.
    private final JTextField txtUsername;
    private final JPasswordField txtPassword;

    /**
     * Konstruktor utama untuk LoginForm.
     * Memanggil metode inisialisasi UI.
     */
    public LoginForm() {
        // Inisialisasi komponen UI sebelum memanggil metode lain.
        this.txtUsername = new JTextField(20);
        this.txtPassword = new JPasswordField();
        
        // Membangun dan menampilkan frame.
        initUI();
    }

    /**
     * Menginisialisasi, menata, dan menampilkan semua komponen antarmuka pengguna (UI).
     */
    private void initUI() {
        // --- Pengaturan Properti Frame ---
        setTitle(I18n.getString("Login Ke Aplikasi Monitoring Ekspedisi Alam"));
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Menampilkan jendela di tengah layar
        setResizable(false);

        // --- Penataan Komponen dengan GridBagLayout ---
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Memberi margin antar komponen
        gbc.fill = GridBagConstraints.HORIZONTAL; // Komponen akan mengisi ruang horizontal

        // Baris 1: Label dan Field Username
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel(I18n.getString("Username")), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(txtUsername, gbc);

        // Baris 2: Label dan Field Password
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel(I18n.getString("Password")), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(txtPassword, gbc);

        // Baris 3: Tombol Login
        JButton btnLogin = new JButton(I18n.getString("Masuk"));
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END; // Membuat tombol rata kanan
        gbc.fill = GridBagConstraints.NONE; // Ukuran tombol tidak meregang
        panel.add(btnLogin, gbc);

        // Menambahkan panel utama ke frame
        add(panel);

        // --- Menambahkan Action Listener ---
        // Menggunakan method reference untuk kode yang lebih ringkas dan modern.
        btnLogin.addActionListener(this::performLogin);
        txtPassword.addActionListener(this::performLogin); // Memungkinkan login dengan menekan Enter
    }

    /**
     * Metode ini dieksekusi saat tombol "Masuk" diklik atau Enter ditekan.
     * Ini berisi validasi input, hashing password, dan query ke database.
     * @param evt Objek ActionEvent yang memicu metode ini (tidak digunakan secara langsung).
     */
    private void performLogin(ActionEvent evt) {
        String inputUsername = txtUsername.getText().trim();
        String inputPassword = new String(txtPassword.getPassword());

        // Validasi input dasar untuk memastikan field tidak kosong.
        if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // [IMPLEMENTASI] Cryptography: Hash password yang diinput pengguna.
        String generatedHash = PasswordUtil.hashPassword(inputPassword);
        
        // Query SQL yang aman menggunakan PreparedStatement untuk mencegah SQL Injection.
        String sql = "SELECT id FROM users WHERE username = ? AND password_hash = ?";

        // Menggunakan try-with-resources untuk memastikan koneksi dan statement ditutup secara otomatis.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Mengatur parameter query.
            ps.setString(1, inputUsername);
            ps.setString(2, generatedHash);

            // Mengeksekusi query dan mendapatkan hasilnya.
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { // Jika query menemukan satu baris yang cocok...
                    // Login berhasil.
                    new MainDashboard().setVisible(true); // Buka dashboard utama.
                    this.dispose(); // Tutup frame login ini.
                } else { // Jika tidak ada baris yang cocok...
                    // Login gagal.
                    JOptionPane.showMessageDialog(this, "Username atau password salah.", "Login Gagal", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            // Menangani error yang mungkin terjadi pada level database.
            JOptionPane.showMessageDialog(this, "Terjadi error pada database: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Cetak stack trace ke konsol untuk debugging oleh developer.
        }
    }
}