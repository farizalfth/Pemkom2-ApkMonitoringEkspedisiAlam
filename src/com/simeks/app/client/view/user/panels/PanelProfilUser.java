package com.simeks.app.client.view.user.panels;
import com.simeks.app.shared.model.User;
import java.awt.*; import javax.swing.JPanel; import javax.swing.JLabel;
public class PanelProfilUser extends JPanel {
    public PanelProfilUser(User user) {
        setLayout(new GridBagLayout());
        add(new JLabel("Fitur Edit Profil untuk " + user.getFullName() + " - Sedang Dikembangkan"));
    }
}