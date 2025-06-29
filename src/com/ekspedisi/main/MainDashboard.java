package com.ekspedisi.main;

// Import dari package proyek
import com.ekspedisi.db.EkspedisiManager;
import com.ekspedisi.model.AnggotaTim;
import com.ekspedisi.model.Ekspedisi;
import com.ekspedisi.util.GenericList;
import com.ekspedisi.util.I18n;

// Import dari library eksternal (iText dan OpenCSV)
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;

// Import dari library Java Swing, AWT, dan IO
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Frame utama aplikasi yang menampilkan dashboard monitoring.
 * Versi final ini mencakup latar belakang gambar, UI transparan,
 * internasionalisasi penuh, dan fitur ekspor canggih.
 */
public class MainDashboard extends JFrame {

    private JTable tableEkspedisi;
    private DefaultTableModel tableModel;
    private final EkspedisiManager ekspedisiManager;
    private JTextField txtSearch;
    private JLabel lblTotal, lblAktif, lblKembali, lblTertunda, lblDibatalkan;
    
    public MainDashboard() {
        this.ekspedisiManager = new EkspedisiManager();
        initUI();
        loadDataAndStats();
        startAutoRefresh();
    }

    private void initUI() {
        setTitle(I18n.getString("dashboard.title"));
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Mengganti content pane default dengan ImagePanel.
        ImagePanel backgroundPanel = new ImagePanel("/images/Backgroundpanel.jpg");
        backgroundPanel.setLayout(new BorderLayout(10, 10));
        setContentPane(backgroundPanel);
        
        setupMenuBar();
        backgroundPanel.add(createTopPanel(), BorderLayout.NORTH);
        backgroundPanel.add(createCenterPanel(), BorderLayout.CENTER);
        backgroundPanel.add(createBottomPanel(), BorderLayout.SOUTH);
    }
    
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(I18n.getString("Menu"));
        JMenu langMenu = new JMenu(I18n.getString("Language"));
        
        JMenuItem exitItem = new JMenuItem(I18n.getString("Exit"));
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        JMenuItem langID = new JMenuItem("Indonesia");
        langID.addActionListener(e -> switchLanguage(new Locale("in", "ID")));
        JMenuItem langEN = new JMenuItem("English");
        langEN.addActionListener(e -> switchLanguage(new Locale("en", "US")));
        JMenuItem langJP = new JMenuItem("日本語 (Japanese)");
        langJP.addActionListener(e -> switchLanguage(new Locale("ja", "JP")));
        
        langMenu.add(langID); langMenu.add(langEN); langMenu.add(langJP);
        menuBar.add(fileMenu); menuBar.add(langMenu);
        setJMenuBar(menuBar);
    }
    
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel searchLabel = new JLabel(I18n.getString("dashboard.search"));
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        txtSearch = new JTextField();
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadDataAndStats(); }
            @Override public void removeUpdate(DocumentEvent e) { loadDataAndStats(); }
            @Override public void changedUpdate(DocumentEvent e) { /* Not used */ }
        });

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        statsPanel.setOpaque(false);
        
        lblTotal = createStatsLabel(); lblAktif = createStatsLabel(); lblKembali = createStatsLabel();
        lblTertunda = createStatsLabel(); lblDibatalkan = createStatsLabel();
        
        statsPanel.add(lblTotal); statsPanel.add(createVerticalSeparator());
        statsPanel.add(lblAktif); statsPanel.add(lblKembali);
        statsPanel.add(lblTertunda); statsPanel.add(lblDibatalkan);

        topPanel.add(searchLabel, BorderLayout.WEST);
        topPanel.add(txtSearch, BorderLayout.CENTER);
        topPanel.add(statsPanel, BorderLayout.EAST);
        return topPanel;
    }

    private JScrollPane createCenterPanel() {
        String[] columnNames = {
            "ID", 
            I18n.getString("Nama Tim"), 
            I18n.getString("Tujuan"), 
            I18n.getString("Tanggal"), 
            I18n.getString("Status")
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableEkspedisi = new JTable(tableModel);
        
        tableEkspedisi.setOpaque(false);
        tableEkspedisi.setForeground(Color.BLACK);
        tableEkspedisi.setGridColor(Color.DARK_GRAY);
        tableEkspedisi.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tableEkspedisi.getTableHeader().setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        tableEkspedisi.setRowHeight(25);
        
        tableEkspedisi.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    setOpaque(true);
                    setForeground(table.getSelectionForeground());
                    setBackground(table.getSelectionBackground());
                } else {
                    setOpaque(false);
                    setForeground(table.getForeground());
                }
                return this;
            }
        });

        tableEkspedisi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableEkspedisi.getColumnModel().getColumn(0).setMinWidth(0);
        tableEkspedisi.getColumnModel().getColumn(0).setMaxWidth(0);
        
        JScrollPane scrollPane = new JScrollPane(tableEkspedisi);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        return scrollPane;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomPanel.setOpaque(false);
        
        JButton btnTambah = new JButton(I18n.getString("dashboard.add"));
        JButton btnEdit = new JButton(I18n.getString("dashboard.edit"));
        JButton btnHapus = new JButton(I18n.getString("dashboard.delete"));
        JButton btnDetail = new JButton(I18n.getString("dashboard.details"));
        JButton btnExportCSV = new JButton(I18n.getString("dashboard.export.csv"));
        JButton btnExportPDF = new JButton(I18n.getString("dashboard.export.pdf"));

        btnTambah.addActionListener(e -> openFormDialog(null));
        btnEdit.addActionListener(e -> openEditDialog());
        btnHapus.addActionListener(e -> deleteEkspedisi());
        btnDetail.addActionListener(e -> openDetailDialog());
        btnExportCSV.addActionListener(e -> showExportOptions("csv"));
        btnExportPDF.addActionListener(e -> showExportOptions("pdf"));

        bottomPanel.add(btnTambah); bottomPanel.add(btnEdit); bottomPanel.add(btnHapus);
        bottomPanel.add(btnDetail); bottomPanel.add(createVerticalSeparator());
        bottomPanel.add(btnExportCSV); bottomPanel.add(btnExportPDF);
        return bottomPanel;
    }

    public void loadDataAndStats() {
        tableModel.setRowCount(0);
        GenericList<Ekspedisi> list = ekspedisiManager.semuaEkspedisi(txtSearch.getText());
        for (Ekspedisi e : list.getList()) {
            tableModel.addRow(new Object[]{ e.getId(), e.getNamaTim(), e.getTujuan(), e.getTanggal(), e.getStatus() });
        }
        int[] stats = ekspedisiManager.getStatistik();
        lblTotal.setText(I18n.getString("dashboard.stats.total") + ": " + stats[0]);
        lblAktif.setText(I18n.getString("dashboard.stats.active") + ": " + stats[1]);
        lblKembali.setText(I18n.getString("dashboard.stats.returned") + ": " + stats[2]);
        lblTertunda.setText(I18n.getString("dashboard.stats.pending") + ": " + stats[3]);
        lblDibatalkan.setText(I18n.getString("dashboard.stats.canceled") + ": " + stats[4]);
    }
    
    private void openFormDialog(Ekspedisi ekspedisi) {
        new FormEkspedisiDialog(this, ekspedisi).setVisible(true);
    }
    
    private void openEditDialog() {
        int selectedRow = getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            Ekspedisi ekspedisi = ekspedisiManager.getEkspedisiById(id);
            if (ekspedisi != null) openFormDialog(ekspedisi);
        } else {
            showWarning(I18n.getString("msg.select.row.edit"));
        }
    }
    
    private void openDetailDialog() {
        int selectedRow = getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            Ekspedisi ekspedisi = ekspedisiManager.getEkspedisiById(id);
            if (ekspedisi != null) new DetailEkspedisiDialog(this, ekspedisi).setVisible(true);
        } else {
            showWarning(I18n.getString("msg.select.row.detail"));
        }
    }
    
    private void deleteEkspedisi() {
        int selectedRow = getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String namaTim = (String) tableModel.getValueAt(selectedRow, 1);
            int response = JOptionPane.showConfirmDialog(this, 
                String.format(I18n.getString("msg.delete.confirm"), namaTim), 
                I18n.getString("msg.confirm.title"), JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION && ekspedisiManager.hapusEkspedisi(id, namaTim)) {
                JOptionPane.showMessageDialog(this, I18n.getString("msg.delete.success"), I18n.getString("msg.info.title"), JOptionPane.INFORMATION_MESSAGE);
                loadDataAndStats();
            }
        } else {
            showWarning(I18n.getString("msg.select.row.delete"));
        }
    }
    
    private void showExportOptions(String format) {
        String title = "Pilih Jenis Laporan " + format.toUpperCase();
        String message = "Pilih jenis laporan yang ingin Anda buat:";
        
        JRadioButton radioRingkasan = new JRadioButton("Laporan Ringkasan (Semua Tim)", true);
        JRadioButton radioDetail = new JRadioButton("Laporan Detail (Hanya Tim yang Dipilih)");
        ButtonGroup group = new ButtonGroup();
        group.add(radioRingkasan);
        group.add(radioDetail);

        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 5));
        panel.add(new JLabel(message));
        panel.add(radioRingkasan);
        panel.add(radioDetail);

        int result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            if (radioRingkasan.isSelected()) {
                if ("csv".equals(format)) exportRingkasanToCSV(); else exportRingkasanToPDF();
            } else {
                int selectedRow = getSelectedRow();
                if (selectedRow < 0) {
                    showWarning("Harap pilih satu tim dari tabel untuk membuat laporan detail.");
                    return;
                }
                int ekspedisiId = (int) tableModel.getValueAt(selectedRow, 0);
                Ekspedisi ekspedisi = ekspedisiManager.getEkspedisiById(ekspedisiId);
                if (ekspedisi != null) {
                    if ("csv".equals(format)) exportDetailToCSV(ekspedisi); else exportDetailToPDF(ekspedisi);
                }
            }
        }
    }

    private void exportRingkasanToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan Ringkasan CSV");
        fileChooser.setSelectedFile(new File("Laporan_Ringkasan_Ekspedisi.csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (CSVWriter writer = new CSVWriter(new FileWriter(fileToSave))) {
                String[] header = {"Nama Tim", "Tujuan", "Tanggal", "Status"};
                writer.writeNext(header);
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    writer.writeNext(new String[]{
                        (String) tableModel.getValueAt(i, 1), (String) tableModel.getValueAt(i, 2),
                        tableModel.getValueAt(i, 3).toString(), (String) tableModel.getValueAt(i, 4)
                    });
                }
                JOptionPane.showMessageDialog(this, "CSV berhasil diekspor!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saat ekspor CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportRingkasanToPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Cetak Laporan Ringkasan PDF");
        fileChooser.setSelectedFile(new File("Laporan_Ringkasan_Ekspedisi.pdf"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            Document document = new Document(); 
            try {
                PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                document.open();
                document.add(new Paragraph("Laporan Ringkasan Ekspedisi"));
                document.add(new Paragraph(" "));
                PdfPTable pdfTable = new PdfPTable(4);
                pdfTable.setWidthPercentage(100);
                String[] headers = {"Nama Tim", "Tujuan", "Tanggal", "Status"};
                for (String header : headers) {
                    pdfTable.addCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                }
                for (int rows = 0; rows < tableModel.getRowCount(); rows++) {
                    pdfTable.addCell((String) tableModel.getValueAt(rows, 1));
                    pdfTable.addCell((String) tableModel.getValueAt(rows, 2));
                    pdfTable.addCell(tableModel.getValueAt(rows, 3).toString());
                    pdfTable.addCell((String) tableModel.getValueAt(rows, 4));
                }
                document.add(pdfTable);
                JOptionPane.showMessageDialog(this, "PDF berhasil dibuat!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saat membuat PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (document.isOpen()) document.close();
            }
        }
    }
    
    private void exportDetailToCSV(Ekspedisi ekspedisi) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan Detail CSV");
        fileChooser.setSelectedFile(new File("Detail_" + ekspedisi.getNamaTim().replace(" ", "_") + ".csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (CSVWriter writer = new CSVWriter(new FileWriter(fileToSave))) {
                writer.writeNext(new String[]{"DETAIL EKSPEDISI"});
                writer.writeNext(new String[]{"Nama Tim", ekspedisi.getNamaTim()});
                writer.writeNext(new String[]{"Tujuan", ekspedisi.getTujuan()});
                writer.writeNext(new String[]{"Tanggal", ekspedisi.getTanggal().toString()});
                writer.writeNext(new String[]{"Status", ekspedisi.getStatus()});
                writer.writeNext(new String[]{"Catatan", ekspedisi.getCatatan()});
                writer.writeNext(new String[]{""});
                
                writer.writeNext(new String[]{"DAFTAR ANGGOTA TIM"});
                writer.writeNext(new String[]{"Nama Anggota", "Jenis Kelamin", "No. TLP", "Alamat"});

                for (AnggotaTim anggota : ekspedisi.getAnggota()) {
                    writer.writeNext(new String[]{anggota.getNama(), anggota.getJenisKelamin(), anggota.getNoTlp(), anggota.getAlamat()});
                }
                JOptionPane.showMessageDialog(this, "CSV Detail berhasil diekspor!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saat ekspor CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportDetailToPDF(Ekspedisi ekspedisi) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Cetak Laporan Detail PDF");
        fileChooser.setSelectedFile(new File("Detail_" + ekspedisi.getNamaTim().replace(" ", "_") + ".pdf"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            Document document = new Document(); 
            try {
                PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                document.open();

                com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
                com.itextpdf.text.Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

                document.add(new Paragraph("Laporan Detail Ekspedisi", titleFont));
                document.add(Chunk.NEWLINE);

                addDetailPdfRow(document, "Nama Tim:", ekspedisi.getNamaTim(), headerFont, bodyFont);
                addDetailPdfRow(document, "Tujuan:", ekspedisi.getTujuan(), headerFont, bodyFont);
                addDetailPdfRow(document, "Tanggal:", ekspedisi.getTanggal().toString(), headerFont, bodyFont);
                addDetailPdfRow(document, "Status:", ekspedisi.getStatus(), headerFont, bodyFont);
                addDetailPdfRow(document, "Catatan:", ekspedisi.getCatatan(), headerFont, bodyFont);
                document.add(Chunk.NEWLINE);

                document.add(new Paragraph("Daftar Anggota Tim", headerFont));
                document.add(new Paragraph(" "));

                PdfPTable anggotaTable = new PdfPTable(4);
                anggotaTable.setWidthPercentage(100);
                anggotaTable.addCell(new Phrase("Nama Anggota", headerFont));
                anggotaTable.addCell(new Phrase("Jenis Kelamin", headerFont));
                anggotaTable.addCell(new Phrase("No. TLP", headerFont));
                anggotaTable.addCell(new Phrase("Alamat", headerFont));
                
                for (AnggotaTim anggota : ekspedisi.getAnggota()) {
                    anggotaTable.addCell(new Phrase(anggota.getNama(), bodyFont));
                    anggotaTable.addCell(new Phrase(anggota.getJenisKelamin(), bodyFont));
                    anggotaTable.addCell(new Phrase(anggota.getNoTlp(), bodyFont));
                    anggotaTable.addCell(new Phrase(anggota.getAlamat(), bodyFont));
                }
                document.add(anggotaTable);
                JOptionPane.showMessageDialog(this, "PDF Detail berhasil dibuat!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saat membuat PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (document.isOpen()) document.close();
            }
        }
    }
    
    private void addDetailPdfRow(Document doc, String label, String value, com.itextpdf.text.Font labelFont, com.itextpdf.text.Font valueFont) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", labelFont));
        p.add(new Chunk(value != null ? value : "-", valueFont));
        doc.add(p);
    }
    
    private void startAutoRefresh() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (!isCancelled()) {
                    Thread.sleep(30000);
                    SwingUtilities.invokeLater(this::updateDataInBackground);
                }
                return null;
            }
            private void updateDataInBackground() {
                System.out.println("Auto-refreshing data...");
                loadDataAndStats();
            }
        };
        worker.execute();
    }
    
    private JLabel createStatsLabel() {
        JLabel label = new JLabel("Status: 0");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        return label;
    }
    
    private int getSelectedRow() { return tableEkspedisi.getSelectedRow(); }
    
    private void showWarning(String message) { 
        JOptionPane.showMessageDialog(this, message, I18n.getString("msg.warning.title"), JOptionPane.WARNING_MESSAGE); 
    }
    
    private JSeparator createVerticalSeparator() { 
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL); 
        sep.setPreferredSize(new Dimension(1, 20)); 
        return sep; 
    }
    
    private void switchLanguage(Locale locale) {
        I18n.setLocale(locale);
        this.dispose();
        SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
    }
}