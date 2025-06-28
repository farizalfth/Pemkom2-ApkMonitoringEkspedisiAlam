// FILE: com/ekspedisi/main/FormEkspedisiDialog.java
package com.ekspedisi.main;

import com.ekspedisi.db.EkspedisiManager;
import com.ekspedisi.model.AnggotaTim;
import com.ekspedisi.model.Ekspedisi;

import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FormEkspedisiDialog extends JDialog {

    private final EkspedisiManager ekspedisiManager;
    private final Ekspedisi currentEkspedisi;
    private final MainDashboard parentDashboard;

    private JTextField txtNamaTim, txtTujuan, txtTanggal, txtLatitude, txtLongitude;
    private JComboBox<String> cmbStatus;
    private JTextArea txtCatatan;
    private JLabel lblFotoPath;
    private File selectedPhotoFile;
    private JTable tableAnggota;
    private DefaultTableModel tableAnggotaModel;

    public FormEkspedisiDialog(MainDashboard parent, Ekspedisi ekspedisi) {
        super(parent, true);
        this.parentDashboard = parent;
        this.currentEkspedisi = ekspedisi;
        this.ekspedisiManager = new EkspedisiManager();
        initUI();
        if (currentEkspedisi != null) {
            populateForm();
        }
    }

    private void initUI() {
        setTitle(currentEkspedisi == null ? "Tambah Ekspedisi Baru" : "Ubah Data Ekspedisi");
        setSize(600, 750);
        setMinimumSize(new Dimension(550, 650));
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));
        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        txtNamaTim = new JTextField();
        txtTujuan = new JTextField();
        txtTanggal = new JTextField();
        txtLatitude = new JTextField();
        txtLongitude = new JTextField();
        cmbStatus = new JComboBox<>(new String[]{"Aktif", "Kembali", "Tertunda", "Dibatalkan"});
        txtCatatan = new JTextArea(4, 20);
        txtCatatan.setLineWrap(true);
        txtCatatan.setWrapStyleWord(true);
        lblFotoPath = new JLabel("Tidak ada foto dipilih.");

        int y = 0;
        
        addComponent(formPanel, new JLabel("Nama Tim:"), 0, y, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, 0.5);
        addComponent(formPanel, txtNamaTim, 1, y++, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, 0.5);
        
        addComponent(formPanel, createAnggotaPanel(), 0, y++, GridBagConstraints.LINE_START, 2, GridBagConstraints.BOTH, 1.0);

        addComponent(formPanel, new JLabel("Tujuan:"), 0, y, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, 0.5);
        addComponent(formPanel, txtTujuan, 1, y++, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, 0.5);

        addComponent(formPanel, new JLabel("Tanggal (yyyy-mm-dd):"), 0, y, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, 0.5);
        addComponent(formPanel, txtTanggal, 1, y++, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, 0.5);

        addComponent(formPanel, new JLabel("Status:"), 0, y, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, 0.5);
        addComponent(formPanel, cmbStatus, 1, y++, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, 0.5);

        addComponent(formPanel, new JLabel("Latitude:"), 0, y, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, 0.5);
        addComponent(formPanel, txtLatitude, 1, y++, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, 0.5);

        addComponent(formPanel, new JLabel("Longitude:"), 0, y, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, 0.5);
        addComponent(formPanel, txtLongitude, 1, y++, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, 0.5);

        addComponent(formPanel, new JLabel("Catatan:"), 0, y, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.BOTH, 0.5);
        addComponent(formPanel, new JScrollPane(txtCatatan), 1, y++, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, 0.5);

        addComponent(formPanel, new JLabel("Foto Tim:"), 0, y, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, 0.5);
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton btnBrowse = new JButton("Pilih Foto...");
        btnBrowse.addActionListener(e -> browsePhoto());
        photoPanel.add(btnBrowse);
        photoPanel.add(lblFotoPath);
        addComponent(formPanel, photoPanel, 1, y++, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, 0.5);

        return formPanel;
    }

    private JPanel createAnggotaPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Anggota Tim"));

        String[] columnNames = {"Nama Anggota", "Jenis Kelamin", "No. TLP", "Alamat"};
        tableAnggotaModel = new DefaultTableModel(columnNames, 0);
        tableAnggota = new JTable(tableAnggotaModel);
        JScrollPane scrollPane = new JScrollPane(tableAnggota);
        scrollPane.setPreferredSize(new Dimension(400, 120));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnTambahAnggota = new JButton("Tambah");
        JButton btnEditAnggota = new JButton("Edit");
        JButton btnHapusAnggota = new JButton("Hapus");
        btnTambahAnggota.addActionListener(e -> tambahAnggota());
        btnEditAnggota.addActionListener(e -> editAnggota());
        btnHapusAnggota.addActionListener(e -> hapusAnggota());
        buttonPanel.add(btnTambahAnggota);
        buttonPanel.add(btnEditAnggota);
        buttonPanel.add(btnHapusAnggota);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void tambahAnggota() {
        JTextField namaField = new JTextField(20);
        JRadioButton lakiLakiRadio = new JRadioButton("Laki-laki", true);
        JRadioButton perempuanRadio = new JRadioButton("Perempuan");
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(lakiLakiRadio);
        genderGroup.add(perempuanRadio);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radioPanel.add(lakiLakiRadio);
        radioPanel.add(perempuanRadio);
        JTextField tlpField = new JTextField(15);
        JTextArea alamatArea = new JTextArea(3, 20);
        alamatArea.setLineWrap(true);
        alamatArea.setWrapStyleWord(true);

        JComponent[] inputs = new JComponent[] {
            new JLabel("Nama:"), namaField,
            new JLabel("Jenis Kelamin:"), radioPanel,
            new JLabel("No. Telepon:"), tlpField,
            new JLabel("Alamat:"), new JScrollPane(alamatArea)
        };
        
        int result = JOptionPane.showConfirmDialog(this, inputs, "Tambah Anggota Baru", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION && !namaField.getText().trim().isEmpty()) {
            String jenisKelamin = lakiLakiRadio.isSelected() ? "Laki-laki" : "Perempuan";
            tableAnggotaModel.addRow(new Object[]{namaField.getText(), jenisKelamin, tlpField.getText(), alamatArea.getText()});
        }
    }

    private void editAnggota() {
        int selectedRow = tableAnggota.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih anggota yang akan diedit dari tabel.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String namaLama = (String) tableAnggotaModel.getValueAt(selectedRow, 0);
        String jenisKelaminLama = (String) tableAnggotaModel.getValueAt(selectedRow, 1);
        String tlpLama = (String) tableAnggotaModel.getValueAt(selectedRow, 2);
        String alamatLama = (String) tableAnggotaModel.getValueAt(selectedRow, 3);

        JTextField namaField = new JTextField(namaLama, 20);
        JRadioButton lakiLakiRadio = new JRadioButton("Laki-laki");
        JRadioButton perempuanRadio = new JRadioButton("Perempuan");
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(lakiLakiRadio);
        genderGroup.add(perempuanRadio);
        if ("Laki-laki".equals(jenisKelaminLama)) lakiLakiRadio.setSelected(true); else perempuanRadio.setSelected(true);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radioPanel.add(lakiLakiRadio);
        radioPanel.add(perempuanRadio);
        JTextField tlpField = new JTextField(tlpLama, 15);
        JTextArea alamatArea = new JTextArea(alamatLama, 3, 20);
        alamatArea.setLineWrap(true);
        alamatArea.setWrapStyleWord(true);

        JComponent[] inputs = new JComponent[] {
            new JLabel("Nama:"), namaField,
            new JLabel("Jenis Kelamin:"), radioPanel,
            new JLabel("No. Telepon:"), tlpField,
            new JLabel("Alamat:"), new JScrollPane(alamatArea)
        };
        
        int result = JOptionPane.showConfirmDialog(this, inputs, "Edit Data Anggota", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION && !namaField.getText().trim().isEmpty()) {
            String jenisKelaminBaru = lakiLakiRadio.isSelected() ? "Laki-laki" : "Perempuan";
            tableAnggotaModel.setValueAt(namaField.getText(), selectedRow, 0);
            tableAnggotaModel.setValueAt(jenisKelaminBaru, selectedRow, 1);
            tableAnggotaModel.setValueAt(tlpField.getText(), selectedRow, 2);
            tableAnggotaModel.setValueAt(alamatArea.getText(), selectedRow, 3);
        }
    }

    private void hapusAnggota() {
        int selectedRow = tableAnggota.getSelectedRow();
        if (selectedRow >= 0) {
            if (tableAnggota.isEditing()) tableAnggota.getCellEditor().stopCellEditing();
            tableAnggotaModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Pilih anggota yang akan dihapus.", "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
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
        
        tableAnggotaModel.setRowCount(0);
        if (currentEkspedisi.getAnggota() != null) {
            for (AnggotaTim anggota : currentEkspedisi.getAnggota()) {
                tableAnggotaModel.addRow(new Object[]{anggota.getNama(), anggota.getJenisKelamin(), anggota.getNoTlp(), anggota.getAlamat()});
            }
        }
    }

    private void save() {
        if (!validateInput()) {
            JOptionPane.showMessageDialog(this, "Harap isi semua field utama (Nama Tim, Tujuan, Tanggal).", "Validasi Gagal", JOptionPane.ERROR_MESSAGE);
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
        
        if (selectedPhotoFile != null) {
            try {
                Path destDir = Paths.get("src/images");
                if (!Files.exists(destDir)) Files.createDirectories(destDir);
                Path destPath = destDir.resolve(selectedPhotoFile.getName());
                Files.copy(selectedPhotoFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                ekspedisi.setPathFoto(selectedPhotoFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal menyimpan file foto.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else if (currentEkspedisi != null) {
            ekspedisi.setPathFoto(currentEkspedisi.getPathFoto());
        }
        
        List<AnggotaTim> daftarAnggota = new ArrayList<>();
        for (int i = 0; i < tableAnggotaModel.getRowCount(); i++) {
            daftarAnggota.add(new AnggotaTim(
                (String) tableAnggotaModel.getValueAt(i, 0), // Nama
                (String) tableAnggotaModel.getValueAt(i, 1), // Jenis Kelamin
                (String) tableAnggotaModel.getValueAt(i, 2), // No. TLP
                (String) tableAnggotaModel.getValueAt(i, 3)  // Alamat
            ));
        }
        ekspedisi.setAnggota(daftarAnggota);

        boolean success;
        if (currentEkspedisi == null) {
            success = ekspedisiManager.tambahEkspedisi(ekspedisi);
        } else {
            ekspedisi.setId(currentEkspedisi.getId());
            success = ekspedisiManager.ubahEkspedisi(ekspedisi);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Data berhasil disimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            parentDashboard.loadDataAndStats();
            dispose();
        }
    }
    
    private void browsePhoto() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg", "gif");
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedPhotoFile = chooser.getSelectedFile();
            lblFotoPath.setText(selectedPhotoFile.getName());
        }
    }

    private boolean validateInput() {
        return !txtNamaTim.getText().trim().isEmpty() && !txtTujuan.getText().trim().isEmpty() && !txtTanggal.getText().trim().isEmpty();
    }
    
    // --- Metode Helper untuk Tata Letak ---
    private void addComponent(JPanel p, JComponent c, int x, int y, int anchor, int BOTH, double par2) { addComponent(p, c, x, y, anchor, 1, GridBagConstraints.NONE, 0); }
    private void addComponent(JPanel p, JComponent c, int x, int y, int anchor, int w) { addComponent(p, c, x, y, anchor, w, GridBagConstraints.HORIZONTAL, 0); }
    private void addComponent(JPanel p, JComponent c, int x, int y, int anchor, int w, int fill, double weighty) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x; gbc.gridy = y; gbc.anchor = anchor;
        gbc.gridwidth = w; gbc.fill = fill; gbc.weighty = weighty;
        gbc.insets = new Insets(5, 5, 5, 5);
        if(x == 1 || w > 1) gbc.weightx = 1.0;
        p.add(c, gbc);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSimpan = new JButton("Simpan");
        JButton btnBatal = new JButton("Batal");
        btnSimpan.addActionListener(e -> save());
        btnBatal.addActionListener(e -> dispose());
        panel.add(btnBatal); panel.add(btnSimpan);
        return panel;
    }
}