package com.simeks.app.client.view.admin.panels;

import java.awt.Font;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PanelPlaceholder extends JPanel {
    public PanelPlaceholder(String featureName) {
        setLayout(new GridBagLayout());
        JLabel label = new JLabel(featureName + " - Fitur ini sedang dikembangkan.");
        label.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        add(label);
    }
}