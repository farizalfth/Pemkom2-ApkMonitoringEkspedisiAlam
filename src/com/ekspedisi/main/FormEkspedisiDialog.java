// FILE: com/main/FormEkspedisiDialog.java
package com.ekspedisi.main;

import com.ekspedisi.db.EkspedisiManager;
import com.ekspedisi.util.I18n;
import com.model.Ekspedisi;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Dialog modal untuk menambah atau mengedit data Ekspedisi.
 */
public class FormEkspedisiDialog extends JDialog {

    private final EkspedisiManager ekspedisiManager;
    private final Ekspedisi currentEkspedisi; // Null jika mode 'Tambah', terisi jika mode 'Edit'
    private final MainDashboard parentDashboard; // Untuk callback refresh

    // Komponen UI
    private JTextField txtNamaTim, txtTujuan, txtTanggal, txtLatitude, txtLongitude;
    private JComboBox<String> cmbStatus;
    private JTextArea txtCatatan;
    private JLabel lblFotoPath;
    private File selectedPhotoFile;

    public FormEkspedisiDialog(MainDashboard parent, Ekspedisi ekspedisi) {
        super(parent, true); // true = modal
        this.parentDashboard = parent;
        this.currentEkspedisi = ekspedisi;
        this.ekspedisiManager = new EkspedisiManager();
        initUI();
        if (currentEkspedisi != null) {
            populateForm();
        }
    }
    
    private void initUI() {
        setTitle(currentEkspedisi == null ? I18n.getString("form.add.title") : I18n.getString("form.edit.title"));
        setSize(550, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Inisialisasi semua komponen
        txtNamaTim = new JTextField(30);
        txtTujuan = new JTextField(30);
        txtTanggal = new JTextField(10);
        txtLatitude = new JTextField(15);
        txtLongitude = new JTextField(15);
        cmbStatus = new JComboBox<>(new String[]{"Aktif", "Kembali", "Tertunda", "Dibatalkan"});
        txtCatatan = new JTextArea(5, 30);
        lblFotoPath = new JLabel("Tidak ada foto dipilih.");
        
        // Tata letak komponen dengan GridBagLayout
        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel(I18n.getString("form.team_name") + "*:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(txtNamaTim, gbc);

        gbc.gridy = y; formPanel.add(new JLabel(I18n.getString("form.destination") + "*:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; formPanel.add(txtTujuan, gbc);

        gbc.gridy = y; formPanel.add(new JLabel(I18n.getString("form.date") + "*:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; formPanel.add(txtTanggal, gbc);

        gbc.gridy = y; formPanel.add(new JLabel(I18n.getString("form.status") + "*:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; formPanel.add(cmbStatus, gbc);

        gbc.gridy = y; formPanel.add(new JLabel(I18n.getString("form.latitude") + ":"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; formPanel.add(txtLatitude, gbc);
        
        gbc.gridy = y; formPanel.add(new JLabel(I18n.getString("form.longitude") + ":"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; formPanel.add(txtLongitude, gbc);

        gbc.gridy = y; gbc.anchor = GridBagConstraints.NORTHWEST; formPanel.add(new JLabel(I18n.getString("form.notes") + ":"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.anchor = GridBagConstraints.WEST; formPanel.add(new JScrollPane(txtCatatan), gbc);

        JButton btnBrowse = new JButton(I18n.getString("form.photo.browse"));
        btnBrowse.addActionListener(e -> browsePhoto());
        gbc.gridy = y; formPanel.add(new JLabel(I18n.getString("form.photo") + ":"), gbc);
        gbc.gridx = 1; gbc.gridy = y++;
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        photoPanel.add(btnBrowse);
        photoPanel.add(Box.createHorizontalStrut(10));
        photoPanel.add(lblFotoPath);
        formPanel.add(photoPanel, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    private void populateForm() {
        txtNamaTim.setText(currentEkspedisi.getNamaTim());
        txtTujuan.setText(currentEkspedisi.getTujuan());
        txtTanggal.setText(new SimpleDateFormat("yyyy-MM-dd").format(currentEkspedisi.getTanggal()));
        cmbStatus.setSelectedItem(currentEkspedisi.getStatus());
        txtLatitude.setText(currentEkspedisi.getLatitude() != null ? String.valueOf(currentEkspedisi.getLatitude()) : "");
        txtLongitude.setText(currentEkspedisi.getLongitude() != null ? String.valueOf(currentEkspedisi.getLongitude()) : "");
        txtCatatan.setText(currentEkspedisi.getCatatan());
        if (currentEkspedisi.getPathFoto() != null && !currentEkspedisi.getPathFoto().isEmpty()) {
            lblFotoPath.setText(currentEkspedisi.getPathFoto());
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSimpan = new JButton(I18n.getString("form.save.button"));
        JButton btnBatal = new JButton(I18n.getString("form.cancel.button"));
        
        btnSimpan.addActionListener(e -> save());
        btnBatal.addActionListener(e -> dispose());

        panel.add(btnSimpan);
        panel.add(btnBatal);
        return panel;
    }

    private void browsePhoto() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg", "gif");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            selectedPhotoFile = chooser.getSelectedFile();
            lblFotoPath.setText(selectedPhotoFile.getName());
        }
    }

    private void save() {
        if (!validateInput()) {
            JOptionPane.showMessageDialog(this, I18n.getString("msg.form.validation"), I18n.getString("msg.error.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        Ekspedisi ekspedisi = new Ekspedisi();
        ekspedisi.setNamaTim(txtNamaTim.getText());
        ekspedisi.setTujuan(txtTujuan.getText());
        ekspedisi.setStatus((String) cmbStatus.getSelectedItem());
        ekspedisi.setCatatan(txtCatatan.getText());
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date utilDate = sdf.parse(txtTanggal.getText());
            ekspedisi.setTanggal(new Date(utilDate.getTime()));
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Format tanggal salah. Gunakan yyyy-mm-dd.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (!txtLatitude.getText().trim().isEmpty()) ekspedisi.setLatitude(Double.parseDouble(txtLatitude.getText()));
            if (!txtLongitude.getText().trim().isEmpty()) ekspedisi.setLongitude(Double.parseDouble(txtLongitude.getText()));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Latitude/Longitude harus berupa angka.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Handle foto
        if (selectedPhotoFile != null) {
            try {
                // Pastikan direktori 'src/images' ada
                Path destDir = Paths.get("src/images");
                Files.createDirectories(destDir);
                // Salin file ke direktori tujuan
                Path destPath = destDir.resolve(selectedPhotoFile.getName());
                Files.copy(selectedPhotoFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                ekspedisi.setPathFoto(selectedPhotoFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal menyimpan file foto.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else if (currentEkspedisi != null) {
            // Jika tidak ada foto baru dipilih saat edit, pertahankan foto lama
            ekspedisi.setPathFoto(currentEkspedisi.getPathFoto());
        }

        boolean success;
        if (currentEkspedisi == null) { // Mode Tambah
            success = ekspedisiManager.tambahEkspedisi(ekspedisi);
        } else { // Mode Edit
            ekspedisi.setId(currentEkspedisi.getId());
            success = ekspedisiManager.ubahEkspedisi(ekspedisi);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, I18n.getString("msg.save.success"), I18n.getString("msg.info.title"), JOptionPane.INFORMATION_MESSAGE);
            parentDashboard.loadDataAndStats(); // Refresh dashboard
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, I18n.getString("msg.save.failed"), I18n.getString("msg.error.title"), JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateInput() {
        return !txtNamaTim.getText().trim().isEmpty() &&
               !txtTujuan.getText().trim().isEmpty() &&
               !txtTanggal.getText().trim().isEmpty() &&
               cmbStatus.getSelectedItem() != null;
    }
}