package com.simeks.app.client;

import com.simeks.app.client.view.auth.LoginForm;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ClientApp {
    public static void main(String[] args) {
        // Atur tampilan agar sesuai dengan sistem operasi untuk tampilan yang lebih native
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Jalankan GUI di Event Dispatch Thread (EDT) untuk keamanan thread Swing
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}