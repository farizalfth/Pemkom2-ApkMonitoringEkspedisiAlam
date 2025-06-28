// FILE: com/ekspedisi/main/MainDashboard.java
package com.ekspedisi.main;

// Import untuk kelas-kelas dari proyek ini
import com.ekspedisi.db.EkspedisiManager;
import com.ekspedisi.util.I18n;

// Import untuk library eksternal (pastikan sudah ditambahkan ke proyek)
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.model.Ekspedisi;
import com.opencsv.CSVWriter;
import com.util.GenericList;

// Import untuk komponen Swing dan AWT
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import javax.swing.table.DefaultTableModel;

/**
 * Frame utama aplikasi yang menampilkan dashboard monitoring.
 * Ini adalah pusat kendali untuk semua operasi CRUD dan fitur lainnya.
 */
public class MainDashboard extends JFrame {

    // Komponen UI utama
    private JTable tableEkspedisi;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JLabel lblTotal, lblAktif, lblKembali, lblTertunda, lblDibatalkan;

    // Manajer untuk interaksi dengan database
    private final EkspedisiManager ekspedisiManager;

    /**
     * Konstruktor utama untuk MainDashboard.
     */
    public MainDashboard() {
        this.ekspedisiManager = new EkspedisiManager();
        initUI(); // Inisialisasi semua komponen UI
        loadDataAndStats(); // Muat data awal saat frame dibuka
        startAutoRefresh(); // Mulai thread untuk refresh data otomatis
    }

    /**
     * Metode utama untuk menginisialisasi dan menata seluruh antarmuka pengguna (UI).
     */
    private void initUI() {
        setTitle(I18n.getString("dashboard.title"));
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Tampilkan di tengah layar
        setLayout(new BorderLayout(10, 10)); // Layout utama frame

        // Membangun dan menambahkan setiap bagian UI ke frame
        setupMenuBar();
        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    /**
     * Mengatur JMenuBar untuk menu File dan Bahasa.
     */
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu langMenu = new JMenu("Bahasa");
        
        JMenuItem exitItem = new JMenuItem("Keluar");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // [IMPLEMENTASI] Internasionalisasi
        JMenuItem langID = new JMenuItem("Indonesia");
        langID.addActionListener(e -> switchLanguage(new Locale("in", "ID")));
        JMenuItem langEN = new JMenuItem("English");
        langEN.addActionListener(e -> switchLanguage(new Locale("en", "US")));
        JMenuItem langJP = new JMenuItem("日本語 (Japanese)");
        langJP.addActionListener(e -> switchLanguage(new Locale("ja", "JP")));
        
        langMenu.add(langID);
        langMenu.add(langEN);
        langMenu.add(langJP);
        
        menuBar.add(fileMenu);
        menuBar.add(langMenu);
        setJMenuBar(menuBar);
    }

    /**
     * Membuat panel bagian atas yang berisi field pencarian dan label statistik.
     * @return JPanel untuk bagian atas.
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Field pencarian dengan listener untuk live search
        txtSearch = new JTextField();
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadDataAndStats(); }
            @Override public void removeUpdate(DocumentEvent e) { loadDataAndStats(); }
            @Override public void changedUpdate(DocumentEvent e) { /* Tidak digunakan untuk JTextField */ }
        });

        // Panel untuk statistik
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        lblTotal = new JLabel();
        lblAktif = new JLabel();
        lblKembali = new JLabel();
        lblTertunda = new JLabel();
        lblDibatalkan = new JLabel();
        
        statsPanel.add(lblTotal);
        statsPanel.add(createVerticalSeparator());
        statsPanel.add(lblAktif);
        statsPanel.add(lblKembali);
        statsPanel.add(lblTertunda);
        statsPanel.add(lblDibatalkan);

        topPanel.add(new JLabel(I18n.getString("dashboard.search")), BorderLayout.WEST);
        topPanel.add(txtSearch, BorderLayout.CENTER);
        topPanel.add(statsPanel, BorderLayout.EAST);
        return topPanel;
    }

    /**
     * Membuat panel bagian tengah yang berisi tabel data.
     * @return JScrollPane yang membungkus JTable.
     */
    private JScrollPane createCenterPanel() {
        String[] columnNames = {"ID", I18n.getString("form.team_name"), I18n.getString("form.destination"), I18n.getString("form.date"), I18n.getString("form.status")};
        
        // Membuat table model dengan sel yang tidak bisa diedit
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableEkspedisi = new JTable(tableModel);
        tableEkspedisi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableEkspedisi.setRowHeight(25);
        tableEkspedisi.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        
        // Menyembunyikan kolom ID dari tampilan, tapi datanya tetap ada di model
        tableEkspedisi.getColumnModel().getColumn(0).setMinWidth(0);
        tableEkspedisi.getColumnModel().getColumn(0).setMaxWidth(0);
        tableEkspedisi.getColumnModel().getColumn(0).setWidth(0);
        
        // Tabel harus dibungkus JScrollPane agar bisa di-scroll
        return new JScrollPane(tableEkspedisi);
    }

    /**
     * Membuat panel bagian bawah yang berisi tombol-tombol aksi.
     * @return JPanel untuk bagian bawah.
     */
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton btnTambah = new JButton(I18n.getString("dashboard.add"));
        JButton btnEdit = new JButton(I18n.getString("dashboard.edit"));
        JButton btnHapus = new JButton(I18n.getString("dashboard.delete"));
        JButton btnDetail = new JButton(I18n.getString("dashboard.details"));
        JButton btnExportCSV = new JButton(I18n.getString("dashboard.export.csv"));
        JButton btnExportPDF = new JButton(I18n.getString("dashboard.export.pdf"));

        // Menambahkan action listener ke setiap tombol
        btnTambah.addActionListener(e -> openFormDialog(null)); // null berarti mode 'Tambah'
        btnEdit.addActionListener(e -> openEditDialog());
        btnHapus.addActionListener(e -> deleteEkspedisi());
        btnDetail.addActionListener(e -> openDetailDialog());
        btnExportCSV.addActionListener(e -> exportToCSV());
        btnExportPDF.addActionListener(e -> exportToPDF());

        // Menambahkan komponen ke panel
        bottomPanel.add(btnTambah);
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnHapus);
        bottomPanel.add(btnDetail);
        bottomPanel.add(createVerticalSeparator());
        bottomPanel.add(btnExportCSV);
        bottomPanel.add(btnExportPDF);
        return bottomPanel;
    }

    /**
     * Metode utama untuk memuat data dari database ke tabel dan memperbarui label statistik.
     */
    public void loadDataAndStats() {
        // 1. Kosongkan tabel sebelum memuat data baru
        tableModel.setRowCount(0);

        // 2. Ambil data dari database berdasarkan keyword pencarian
        GenericList<Ekspedisi> list = ekspedisiManager.semuaEkspedisi(txtSearch.getText());
        
        // 3. Isi tabel dengan data yang didapat
        for (int i = 0; i < list.size(); i++) {
            Ekspedisi e = list.get(i);
            tableModel.addRow(new Object[]{ e.getId(), e.getNamaTim(), e.getTujuan(), e.getTanggal(), e.getStatus() });
        }
        
        // 4. Ambil dan perbarui statistik
        int[] stats = ekspedisiManager.getStatistik();
        lblTotal.setText(String.format("%s: %d", I18n.getString("dashboard.stats.total"), stats[0]));
        lblAktif.setText(String.format("%s: %d", I18n.getString("dashboard.stats.active"), stats[1]));
        lblKembali.setText(String.format("%s: %d", I18n.getString("dashboard.stats.returned"), stats[2]));
        lblTertunda.setText(String.format("%s: %d", I18n.getString("dashboard.stats.pending"), stats[3]));
        lblDibatalkan.setText(String.format("%s: %d", I18n.getString("dashboard.stats.canceled"), stats[4]));
    }
    
    /**
     * Membuka FormEkspedisiDialog untuk menambah (jika ekspedisi null) atau mengedit.
     * @param ekspedisi Objek Ekspedisi yang akan diedit, atau null untuk menambah data baru.
     */
    private void openFormDialog(Ekspedisi ekspedisi) {
        // Dialog ini modal, jadi eksekusi akan berhenti di sini sampai dialog ditutup.
        // Callback untuk refresh data dilakukan dari dalam dialog itu sendiri.
        new FormEkspedisiDialog(this, ekspedisi).setVisible(true);
    }
    
    /**
     * Menangani logika untuk tombol Edit.
     */
    private void openEditDialog() {
        int selectedRow = getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            Ekspedisi ekspedisi = ekspedisiManager.getEkspedisiById(id);
            if (ekspedisi != null) {
                openFormDialog(ekspedisi);
            } else {
                JOptionPane.showMessageDialog(this, "Data tidak ditemukan di database.", I18n.getString("msg.error.title"), JOptionPane.ERROR_MESSAGE);
            }
        } else {
            showWarning(I18n.getString("msg.select.row.edit"));
        }
    }

    /**
     * Menangani logika untuk tombol Detail.
     */
    private void openDetailDialog() {
        int selectedRow = getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            Ekspedisi ekspedisi = ekspedisiManager.getEkspedisiById(id);
            if (ekspedisi != null) {
                new DetailEkspedisiDialog(this, ekspedisi).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Data tidak ditemukan di database.", I18n.getString("msg.error.title"), JOptionPane.ERROR_MESSAGE);
            }
        } else {
            showWarning(I18n.getString("msg.select.row.detail"));
        }
    }
    
    /**
     * Menghapus data ekspedisi yang dipilih setelah konfirmasi.
     */
    private void deleteEkspedisi() {
        int selectedRow = getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String namaTim = (String) tableModel.getValueAt(selectedRow, 1);
            
            // Tampilkan dialog konfirmasi sebelum menghapus
            int response = JOptionPane.showConfirmDialog(this, 
                String.format(I18n.getString("msg.delete.confirm"), namaTim), 
                I18n.getString("msg.confirm.title"), JOptionPane.YES_NO_OPTION);
                
            if (response == JOptionPane.YES_OPTION) {
                if (ekspedisiManager.hapusEkspedisi(id, namaTim)) {
                    JOptionPane.showMessageDialog(this, I18n.getString("msg.delete.success"), I18n.getString("msg.info.title"), JOptionPane.INFORMATION_MESSAGE);
                    loadDataAndStats(); // Refresh tabel setelah hapus
                } else {
                     JOptionPane.showMessageDialog(this, I18n.getString("msg.delete.failed"), I18n.getString("msg.error.title"), JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            showWarning(I18n.getString("msg.select.row.delete"));
        }
    }
    
    /**
     * Mengekspor semua data ekspedisi ke dalam format file .csv.
     * [IMPLEMENTASI] Simpan & Muat ke CSV
     */
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan CSV");
        fileChooser.setSelectedFile(new File("Laporan_Ekspedisi.csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Menggunakan try-with-resources untuk memastikan writer ditutup secara otomatis
            try (FileWriter fileWriter = new FileWriter(fileToSave);
                 CSVWriter writer = new CSVWriter(fileWriter)) {

                String[] header = { "ID", "NAMA TIM", "TUJUAN", "TANGGAL", "STATUS", "LATITUDE", "LONGITUDE", "CATATAN" };
                writer.writeNext(header);

                // Mengambil semua data (tanpa filter pencarian) untuk laporan lengkap
                GenericList<Ekspedisi> allData = ekspedisiManager.semuaEkspedisi("");
                for (int i = 0; i < allData.size(); i++) {
                    Ekspedisi e = allData.get(i);
                    writer.writeNext(new String[]{
                        String.valueOf(e.getId()),
                        e.getNamaTim(), e.getTujuan(), e.getTanggal().toString(), e.getStatus(),
                        e.getLatitude() != null ? String.valueOf(e.getLatitude()) : "",
                        e.getLongitude() != null ? String.valueOf(e.getLongitude()) : "",
                        e.getCatatan() != null ? e.getCatatan() : ""
                    });
                }
                JOptionPane.showMessageDialog(this, "CSV berhasil diekspor!", I18n.getString("msg.info.title"), JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saat ekspor CSV: " + ex.getMessage(), I18n.getString("msg.error.title"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Mengekspor data yang ditampilkan di tabel ke dalam format file .pdf.
     * [IMPLEMENTASI] Export ke PDF
     */
    private void exportToPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Cetak Laporan PDF");
        fileChooser.setSelectedFile(new File("Laporan_Ekspedisi.pdf"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            Document document = new Document(); 
            try {
                PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                document.open();
                document.add(new Paragraph(I18n.getString("dashboard.title")));
                document.add(new Paragraph(" ")); // Spasi

                // Membuat tabel PDF dengan jumlah kolom yang sama dengan tabel UI (tanpa kolom ID)
                PdfPTable pdfTable = new PdfPTable(tableModel.getColumnCount() - 1);
                
                // Menambahkan header ke tabel PDF
                for (int i = 1; i < tableModel.getColumnCount(); i++) {
                    pdfTable.addCell(tableModel.getColumnName(i));
                }
                // Menambahkan data baris ke tabel PDF
                for (int rows = 0; rows < tableModel.getRowCount(); rows++) {
                    for (int cols = 1; cols < tableModel.getColumnCount(); cols++) {
                        pdfTable.addCell(tableModel.getValueAt(rows, cols).toString());
                    }
                }
                document.add((Element) pdfTable);
                JOptionPane.showMessageDialog(this, "PDF berhasil dibuat!", I18n.getString("msg.info.title"), JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saat membuat PDF: " + ex.getMessage(), I18n.getString("msg.error.title"), JOptionPane.ERROR_MESSAGE);
            } finally {
                // Sangat penting untuk selalu menutup dokumen untuk menyimpan file dengan benar
                if (document.isOpen()) {
                    document.close();
                }
            }
        }
    }

    /**
     * Memulai thread background untuk me-refresh data secara periodik.
     * [IMPLEMENTASI] Thread
     */
    private void startAutoRefresh() {
        // Menggunakan SwingWorker untuk tugas background agar UI tidak freeze
        // Tipe generik <Void, Void> harus dideklarasikan secara eksplisit untuk kompatibilitas Java 8
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (!isCancelled()) {
                    Thread.sleep(30000); // Tunggu 30 detik
                    // Pembaruan UI harus dilakukan di Event Dispatch Thread (EDT)
                    SwingUtilities.invokeLater(() -> {
                        System.out.println("Auto-refreshing data...");
                        loadDataAndStats();
                    });
                }
                return null;
            }
        };
        worker.execute(); // Jalankan thread
    }
    
    // --- Metode-metode Helper Kecil ---
    
    private int getSelectedRow() { return tableEkspedisi.getSelectedRow(); }
    
    private void showWarning(String message) { JOptionPane.showMessageDialog(this, message, I18n.getString("msg.warning.title"), JOptionPane.WARNING_MESSAGE); }
    
    private JSeparator createVerticalSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 20));
        return sep;
    }
    
    private void switchLanguage(Locale locale) {
        I18n.setLocale(locale);
        this.dispose(); // Tutup frame saat ini
        // Buka frame baru agar semua komponen diinisialisasi ulang dengan bahasa yang baru
        SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
    }

    private static class PdfPTable {

        public PdfPTable(int i) {
        }

        private void addCell(String columnName) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }
}