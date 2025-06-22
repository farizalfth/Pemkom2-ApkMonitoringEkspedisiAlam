package com.simeks.app.client.view.auth;

import com.simeks.app.client.service.ClientService;
import com.simeks.app.client.view.admin.AdminDashboard;
import com.simeks.app.client.view.user.UserDashboard;
import com.simeks.app.shared.model.User;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.Font;

public class LoginForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginForm() {
        super("SimeksApp - Login");
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel lblTitle = new JLabel("SimeksApp", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 20, 5);
        mainPanel.add(lblTitle, gbc);
        
        // Reset insets
        gbc.insets = new Insets(5, 5, 5, 5);

        // Username
        JLabel lblUsername = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(lblUsername, gbc);

        txtUsername = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(txtUsername, gbc);

        // Password
        JLabel lblPassword = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(lblPassword, gbc);

        txtPassword = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(txtPassword, gbc);

        // Login Button
        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_END; // Align to the right
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(btnLogin, gbc);

        // Add action listener
        btnLogin.addActionListener(e -> loginAction());

        add(mainPanel);
    }

    private void loginAction() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Password wajib diisi.", "Input Tidak Lengkap", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = ClientService.getInstance().getRepository().login(username, password);
            if (user != null) {
                dispose();
                if ("ADMIN".equals(user.getRole())) {
                    new AdminDashboard(user).setVisible(true);
                } else {
                    new UserDashboard(user).setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Username atau Password salah.", "Login Gagal", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal terhubung ke server.\nPastikan server sudah berjalan.", "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}