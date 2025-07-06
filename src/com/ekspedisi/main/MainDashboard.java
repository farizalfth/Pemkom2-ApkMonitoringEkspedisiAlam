// FILE: com/ekspedisi/main/MainDashboard.java
package com.ekspedisi.main;

// Import dari package proyek
import com.ekspedisi.db.EkspedisiManager;
import com.ekspedisi.model.AnggotaTim;
import com.ekspedisi.model.Ekspedisi;
import com.ekspedisi.util.GenericList;
import com.ekspedisi.util.I18n;

// Import dari library eksternal
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDayChooser;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

// Import dari library Java
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Frame utama aplikasi yang berfungsi sebagai dasbor analitik dengan beberapa tab fungsionalitas.
 * Versi final ini mencakup semua fitur yang telah dikembangkan dan diperbaiki.
 */
public class MainDashboard extends JFrame {

    private final EkspedisiManager ekspedisiManager;
    private JTable tableEkspedisi;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JLabel lblTotal, lblAktif, lblKembali, lblTertunda, lblDibatalkan, clockLabel, searchLabel;
    private JCalendar calendar;
    private DefaultListModel<String> scheduleListModel;
    private Map<Integer, List<String>> monthlySchedule;

    public MainDashboard() {
        this.ekspedisiManager = new EkspedisiManager();
        initUI();
    }

    private void initUI() {
        setTitle(I18n.getString("app.title"));
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 768));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setupMenuBar();
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        
        tabbedPane.addTab("   " + I18n.getString("Monitoring") + "   ", createMonitoringPanel());
        tabbedPane.addTab("   " + I18n.getString("Statistics") + "   ", createStatisticsPanel());
        tabbedPane.addTab("   " + I18n.getString("Calendar") + "   ", createCalendarPanel());
        tabbedPane.addTab("   " + I18n.getString("Leaderboard") + "   ", createLeaderboardPanel());
        
        add(tabbedPane);
    }
    
    // =================================================================================
    // BAGIAN 1: PEMBUATAN PANEL UNTUK SETIAP TAB
    // =================================================================================
    
    private JPanel createMonitoringPanel() {
        ImagePanel backgroundPanel = new ImagePanel("/images/Baground.png");
        backgroundPanel.setLayout(new BorderLayout(10, 10));
        backgroundPanel.add(createTopPanel(), BorderLayout.NORTH);
        backgroundPanel.add(createCenterPanel(), BorderLayout.CENTER);
        backgroundPanel.add(createBottomPanel(), BorderLayout.SOUTH);
        loadDataAndStats();
        startAutoRefresh();
        return backgroundPanel;
    }
    
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel topStatsPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        topStatsPanel.add(createStatCard(I18n.getString("stats.card.active_today"), "7", new Color(220, 240, 255)));
        topStatsPanel.add(createStatCard(I18n.getString("stats.card.popular_mountain"), "Prau", new Color(255, 230, 220)));
        topStatsPanel.add(createStatCard(I18n.getString("stats.card.avg_duration"), "2.5 hari", new Color(220, 255, 230)));
        panel.add(topStatsPanel, BorderLayout.NORTH);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(5, "Ekspedisi", "Jan"); dataset.addValue(8, "Ekspedisi", "Feb"); dataset.addValue(12, "Ekspedisi", "Mar");
        JFreeChart lineChart = ChartFactory.createLineChart(
            I18n.getString("stats.chart.activity_title"), I18n.getString("stats.chart.month_axis"), I18n.getString("stats.chart.count_axis"),
            dataset, PlotOrientation.VERTICAL, false, true, false);
        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setBorder(BorderFactory.createEtchedBorder());
        panel.add(chartPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCalendarPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        clockLabel = new JLabel("Memuat jam...", SwingConstants.CENTER);
        clockLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 20));
        clockLabel.setBorder(BorderFactory.createEtchedBorder());
        startRealTimeClock();
        mainPanel.add(clockLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        calendar = new JCalendar();
        
        com.toedter.calendar.JMonthChooser monthChooser = calendar.getMonthChooser();
        com.toedter.calendar.JYearChooser yearChooser = calendar.getYearChooser();
        com.toedter.calendar.JDayChooser dayChooser = calendar.getDayChooser();
        java.awt.Font headerFont = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18);
        monthChooser.setFont(headerFont);
        if (yearChooser.getSpinner() instanceof JSpinner) {
            JSpinner spinner = (JSpinner) yearChooser.getSpinner();
            if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setFont(headerFont);
            }
        }
        monthChooser.setPreferredSize(new Dimension(150, 40));
        yearChooser.setPreferredSize(new Dimension(100, 40));
        dayChooser.setWeekdayForeground(new Color(0, 51, 102));
        dayChooser.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        dayChooser.setSundayForeground(Color.RED);
        
        JPanel schedulePanel = new JPanel(new BorderLayout());
        schedulePanel.setBorder(BorderFactory.createTitledBorder(I18n.getString("calendar.schedule.title")));
        scheduleListModel = new DefaultListModel<>();
        JList<String> scheduleList = new JList<>(scheduleListModel);
        scheduleList.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        schedulePanel.add(new JScrollPane(scheduleList), BorderLayout.CENTER);
        schedulePanel.setPreferredSize(new Dimension(350, 0));
        
        calendar.getDayChooser().addPropertyChangeListener("day", this::calendarDayChanged);
        calendar.addPropertyChangeListener("calendar", this::calendarMonthChanged);
        
        centerPanel.add(calendar, BorderLayout.CENTER);
        centerPanel.add(schedulePanel, BorderLayout.EAST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        SwingUtilities.invokeLater(this::updateCalendarUI);
        return mainPanel;
    }
    
    private JPanel createLeaderboardPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        Map<String, Integer> dataTimAktif = ekspedisiManager.getTimTeraktif(5);
        DefaultCategoryDataset datasetTim = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : dataTimAktif.entrySet()) { datasetTim.setValue(entry.getValue(), I18n.getString("leaderboard.active_team.y"), entry.getKey()); }
        JFreeChart chartTim = createBarChart(datasetTim, I18n.getString("leaderboard.active_team.title"), I18n.getString("leaderboard.active_team.x"), I18n.getString("leaderboard.active_team.y"));
        panel.add(new ChartPanel(chartTim));
        
        Map<String, Integer> dataGunungPopuler = ekspedisiManager.getGunungTerpopuler(5);
        DefaultCategoryDataset datasetGunung = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : dataGunungPopuler.entrySet()) { datasetGunung.setValue(entry.getValue(), I18n.getString("leaderboard.popular_mountain.y"), entry.getKey()); }
        JFreeChart chartGunung = createBarChart(datasetGunung, I18n.getString("leaderboard.popular_mountain.title"), I18n.getString("leaderboard.popular_mountain.x"), I18n.getString("leaderboard.popular_mountain.y"));
        panel.add(new ChartPanel(chartGunung));
        
        Map<String, Integer> dataPeserta = ekspedisiManager.getTimDenganAnggotaTerbanyak(5);
        DefaultCategoryDataset datasetPeserta = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : dataPeserta.entrySet()) { datasetPeserta.setValue(entry.getValue(), I18n.getString("leaderboard.most_members.y"), entry.getKey()); }
        JFreeChart chartPeserta = createBarChart(datasetPeserta, I18n.getString("leaderboard.most_members.title"), I18n.getString("leaderboard.most_members.x"), I18n.getString("leaderboard.most_members.y"));
        panel.add(new ChartPanel(chartPeserta));
        
        return panel;
    }
    
    // ================== BAGIAN KOMPONEN UI TAB MONITORING ==================
    
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(I18n.getString("File"));
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
        searchLabel = new JLabel(I18n.getString("dashboard.search"));
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        txtSearch = new JTextField();
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadDataAndStats(); }
            @Override public void removeUpdate(DocumentEvent e) { loadDataAndStats(); }
            @Override public void changedUpdate(DocumentEvent e) {}
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
            "ID", I18n.getString("table.header.team_name"), I18n.getString("table.header.destination"), 
            I18n.getString("table.header.date"), I18n.getString("table.header.status")
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableEkspedisi = new JTable(tableModel);
        tableEkspedisi.setOpaque(false);
        tableEkspedisi.getTableHeader().setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        tableEkspedisi.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        tableEkspedisi.setRowHeight(25);
        tableEkspedisi.setGridColor(Color.DARK_GRAY);
        tableEkspedisi.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JComponent) ((JComponent) c).setOpaque(true);
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground()); c.setForeground(table.getSelectionForeground());
                } else {
                    c.setBackground(new Color(255, 255, 255, 120)); c.setForeground(Color.BLACK);
                }
                return c;
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
    
    // =================================================================================
    // BAGIAN 4: METODE LOGIKA UTAMA & HELPER
    // =================================================================================
    
    /**
     * Memuat data dari database ke tabel dan memperbarui label statistik.
     */
    public void loadDataAndStats() {
        // Simpan baris yang sedang dipilih agar tidak hilang setelah refresh
        int selectedRow = getSelectedRow();
        
        tableModel.setRowCount(0); // Kosongkan tabel
        GenericList<Ekspedisi> list = ekspedisiManager.semuaEkspedisi(txtSearch.getText());
        for (Ekspedisi e : list.getList()) {
            tableModel.addRow(new Object[]{e.getId(), e.getNamaTim(), e.getTujuan(), e.getTanggal(), e.getStatus()});
        }
        
        // Kembalikan seleksi ke baris yang sama jika memungkinkan
        if (selectedRow != -1 && selectedRow < tableModel.getRowCount()) {
            tableEkspedisi.setRowSelectionInterval(selectedRow, selectedRow);
        }

        // Perbarui statistik
        int[] stats = ekspedisiManager.getStatistik();
        lblTotal.setText(I18n.getString("dashboard.stats.total") + ": " + stats[0]);
        lblAktif.setText(I18n.getString("dashboard.stats.active") + ": " + stats[1]);
        lblKembali.setText(I18n.getString("dashboard.stats.returned") + ": " + stats[2]);
        lblTertunda.setText(I18n.getString("dashboard.stats.pending") + ": " + stats[3]);
        lblDibatalkan.setText(I18n.getString("dashboard.stats.canceled") + ": " + stats[4]);
    }

    /**
     * Membersihkan field pencarian dan memuat ulang semua data di tabel.
     * Dipanggil setelah operasi Tambah, Ubah, atau Hapus.
     */
    public void refreshDataAndClearSearch() {
        txtSearch.setText("");
        loadDataAndStats();
    }
    
    /**
     * Membuka dialog untuk menambah atau mengedit data ekspedisi.
     */
    private void openFormDialog(Ekspedisi ekspedisi) {
        new FormEkspedisiDialog(this, ekspedisi).setVisible(true);
    }
    
    /**
     * Menangani logika untuk tombol "Ubah Data".
     */
    private void openEditDialog() {
        int row = getSelectedRow();
        if (row != -1) {
            int id = (int) tableModel.getValueAt(row, 0);
            Ekspedisi ekspedisi = ekspedisiManager.getEkspedisiById(id);
            if (ekspedisi != null) {
                openFormDialog(ekspedisi);
            }
        } else {
            showWarning(I18n.getString("msg.select.row.edit"));
        }
    }
    
    /**
     * Menangani logika untuk tombol "Lihat Detail".
     */
    private void openDetailDialog() {
        int row = getSelectedRow();
        if (row != -1) {
            int id = (int) tableModel.getValueAt(row, 0);
            Ekspedisi ekspedisi = ekspedisiManager.getEkspedisiById(id);
            if (ekspedisi != null) {
                new DetailEkspedisiDialog(this, ekspedisi).setVisible(true);
            }
        } else {
            showWarning(I18n.getString("msg.select.row.detail"));
        }
    }
    
    /**
     * Menangani logika untuk tombol "Hapus Data".
     */
    private void deleteEkspedisi() {
        int row = getSelectedRow();
        if (row != -1) {
            int id = (int) tableModel.getValueAt(row, 0);
            String namaTim = (String) tableModel.getValueAt(row, 1);
            int response = JOptionPane.showConfirmDialog(this,
                    String.format(I18n.getString("msg.delete.confirm"), namaTim),
                    I18n.getString("msg.confirm.title"),
                    JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION && ekspedisiManager.hapusEkspedisi(id, namaTim)) {
                JOptionPane.showMessageDialog(this, I18n.getString("msg.delete.success"), I18n.getString("msg.info.title"), JOptionPane.INFORMATION_MESSAGE);
                refreshDataAndClearSearch();
            }
        } else {
            showWarning(I18n.getString("msg.select.row.delete"));
        }
    }
    
    /**
     * Menampilkan dialog untuk memilih jenis laporan (Ringkasan atau Detail).
     */
    private void showExportOptions(String format) {
        String title = String.format(I18n.getString("export.dialog.title"), format.toUpperCase());
        JRadioButton r1 = new JRadioButton(I18n.getString("export.dialog.summary"), true);
        JRadioButton r2 = new JRadioButton(I18n.getString("export.dialog.detail"));
        ButtonGroup grp = new ButtonGroup();
        grp.add(r1);
        grp.add(r2);
        
        JPanel pnl = new JPanel(new GridLayout(0, 1, 0, 5));
        pnl.add(new JLabel(I18n.getString("export.dialog.message")));
        pnl.add(r1);
        pnl.add(r2);
        
        int result = JOptionPane.showConfirmDialog(this, pnl, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            if (r1.isSelected()) {
                if ("csv".equals(format)) exportRingkasanToCSV(); else exportRingkasanToPDF();
            } else {
                int row = getSelectedRow();
                if (row < 0) {
                    showWarning(I18n.getString("msg.select.row.detail.report"));
                    return;
                }
                Ekspedisi e = ekspedisiManager.getEkspedisiById((int) tableModel.getValueAt(row, 0));
                if (e != null) {
                    if ("csv".equals(format)) exportDetailToCSV(e); else exportDetailToPDF(e);
                }
            }
        }
    }

    /**
     * Mengekspor laporan ringkasan ke file CSV.
     */
    private void exportRingkasanToCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Laporan Ringkasan CSV");
        fc.setSelectedFile(new File("Laporan_Ringkasan_Ekspedisi.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (CSVWriter w = new CSVWriter(new FileWriter(fc.getSelectedFile()))) {
                w.writeNext(new String[]{"Nama Tim", "Tujuan", "Tanggal", "Status"});
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    w.writeNext(new String[]{(String) tableModel.getValueAt(i, 1), (String) tableModel.getValueAt(i, 2), tableModel.getValueAt(i, 3).toString(), (String) tableModel.getValueAt(i, 4)});
                }
                JOptionPane.showMessageDialog(this, "CSV berhasil diekspor!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saat ekspor CSV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Mengekspor laporan ringkasan ke file PDF.
     */
    private void exportRingkasanToPDF() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Cetak Laporan Ringkasan PDF");
        fc.setSelectedFile(new File("Laporan_Ringkasan_Ekspedisi.pdf"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Document d = new Document();
            try {
                PdfWriter.getInstance(d, new FileOutputStream(fc.getSelectedFile()));
                d.open();
                d.add(new Paragraph(I18n.getString("report.summary.title"), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
                d.add(new Paragraph(" "));
                PdfPTable t = new PdfPTable(4);
                t.setWidthPercentage(100);
                String[] h = {I18n.getString("table.header.team_name"), I18n.getString("table.header.destination"), I18n.getString("table.header.date"), I18n.getString("table.header.status")};
                for (String s : h) {
                    t.addCell(new Phrase(s, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                }
                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    t.addCell((String) tableModel.getValueAt(r, 1));
                    t.addCell((String) tableModel.getValueAt(r, 2));
                    t.addCell(tableModel.getValueAt(r, 3).toString());
                    t.addCell((String) tableModel.getValueAt(r, 4));
                }
                d.add(t);
                JOptionPane.showMessageDialog(this, "PDF berhasil dibuat!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saat membuat PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (d.isOpen()) d.close();
            }
        }
    }
    
    /**
     * Mengekspor laporan detail ke file CSV.
     */
    private void exportDetailToCSV(Ekspedisi e) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Laporan Detail CSV");
        fc.setSelectedFile(new File("Detail_" + e.getNamaTim().replace(" ", "_") + ".csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (CSVWriter w = new CSVWriter(new FileWriter(fc.getSelectedFile()))) {
                w.writeNext(new String[]{"DETAIL EKSPEDISI"});
                w.writeNext(new String[]{"Nama Tim", e.getNamaTim()});
                w.writeNext(new String[]{"Tujuan", e.getTujuan()});
                w.writeNext(new String[]{"Jenis Pendakian", e.getJenisPendakian()});
                w.writeNext(new String[]{"Tanggal", e.getTanggal().toString()});
                w.writeNext(new String[]{"Status", e.getStatus()});
                w.writeNext(new String[]{"Catatan", e.getCatatan()});
                w.writeNext(new String[]{""});
                w.writeNext(new String[]{"DAFTAR ANGGOTA TIM"});
                w.writeNext(new String[]{"Nama Anggota", "Jenis Kelamin", "No. TLP", "Alamat"});
                for (AnggotaTim a : e.getAnggota()) {
                    w.writeNext(new String[]{a.getNama(), a.getJenisKelamin(), a.getNoTlp(), a.getAlamat()});
                }
                JOptionPane.showMessageDialog(this, "CSV Detail berhasil diekspor!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saat ekspor CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Mengekspor laporan detail ke file PDF.
     */
    private void exportDetailToPDF(Ekspedisi e) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Cetak Laporan Detail PDF");
        fc.setSelectedFile(new File("Detail_" + e.getNamaTim().replace(" ", "_") + ".pdf"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Document d = new Document();
            try {
                PdfWriter.getInstance(d, new FileOutputStream(fc.getSelectedFile()));
                d.open();
                com.itextpdf.text.Font tf = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                com.itextpdf.text.Font hf = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
                com.itextpdf.text.Font bf = FontFactory.getFont(FontFactory.HELVETICA, 11);
                d.add(new Paragraph(I18n.getString("report.detail.title"), tf));
                d.add(Chunk.NEWLINE);
                addDetailPdfRow(d, "Nama Tim:", e.getNamaTim(), hf, bf);
                addDetailPdfRow(d, "Tujuan:", e.getTujuan(), hf, bf);
                addDetailPdfRow(d, "Jenis Pendakian:", e.getJenisPendakian(), hf, bf);
                addDetailPdfRow(d, "Tanggal:", e.getTanggal().toString(), hf, bf);
                addDetailPdfRow(d, "Status:", e.getStatus(), hf, bf);
                addDetailPdfRow(d, "Catatan:", e.getCatatan(), hf, bf);
                d.add(Chunk.NEWLINE);
                d.add(new Paragraph(I18n.getString("report.detail.team_list"), hf));
                d.add(new Paragraph(" "));
                PdfPTable at = new PdfPTable(4);
                at.setWidthPercentage(100);
                at.addCell(new Phrase(I18n.getString("table.header.member_name"), hf));
                at.addCell(new Phrase(I18n.getString("table.header.gender"), hf));
                at.addCell(new Phrase(I18n.getString("table.header.phone"), hf));
                at.addCell(new Phrase(I18n.getString("table.header.address"), hf));
                for (AnggotaTim a : e.getAnggota()) {
                    at.addCell(new Phrase(a.getNama(), bf));
                    at.addCell(new Phrase(a.getJenisKelamin(), bf));
                    at.addCell(new Phrase(a.getNoTlp(), bf));
                    at.addCell(new Phrase(a.getAlamat(), bf));
                }
                d.add(at);
                JOptionPane.showMessageDialog(this, "PDF Detail berhasil dibuat!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saat membuat PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (d.isOpen()) d.close();
            }
        }
    }

    private void addDetailPdfRow(Document d, String l, String v, com.itextpdf.text.Font lf, com.itextpdf.text.Font vf) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(l + " ", lf));
        p.add(new Chunk(v != null ? v : "-", vf));
        d.add(p);
    }
    
    private void startAutoRefresh() {
        SwingWorker<Void, Void> w = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (!isCancelled()) {
                    Thread.sleep(30000);
                    SwingUtilities.invokeLater(() -> {
                        System.out.println("Auto-refreshing data...");
                        loadDataAndStats();
                    });
                }
                return null;
            }
        };
        w.execute();
    }
    
    private void switchLanguage(Locale l) {
        I18n.setLocale(l);
        this.dispose();
        SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
    }

    private void calendarMonthChanged(PropertyChangeEvent e) {
        if ("calendar".equals(e.getPropertyName())) {
            updateCalendarUI();
        }
    }

    private void calendarDayChanged(PropertyChangeEvent e) {
        updateScheduleList();
    }

    private void updateCalendarUI() {
        Calendar c = calendar.getCalendar();
        monthlySchedule = ekspedisiManager.getEkspedisiByMonth(c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
        JDayChooser dc = calendar.getDayChooser();
        // Menggunakan pendekatan manipulasi komponen langsung yang lebih stabil
        for (Component component : dc.getDayPanel().getComponents()) {
            if (component instanceof JButton) {
                JButton dayButton = (JButton) component;
                try {
                    int day = Integer.parseInt(dayButton.getText());
                    if (monthlySchedule != null && monthlySchedule.containsKey(day)) {
                        dayButton.setBackground(new Color(173, 216, 230));
                        dayButton.setToolTipText(String.format(I18n.getString("calendar.schedule.tooltip"), monthlySchedule.get(day).size()));
                    } else {
                        dayButton.setBackground(Color.WHITE);
                        dayButton.setToolTipText(null);
                    }
                } catch (NumberFormatException ex) { /* Abaikan */ }
            }
        }
        calendar.revalidate();
        calendar.repaint();
        updateScheduleList();
    }

    private void updateScheduleList() {
        if (scheduleListModel == null) return;
        scheduleListModel.clear();
        int day = calendar.getCalendar().get(Calendar.DAY_OF_MONTH);
        if (monthlySchedule != null && monthlySchedule.containsKey(day)) {
            for (String t : monthlySchedule.get(day)) {
                scheduleListModel.addElement("• " + t);
            }
        } else {
            scheduleListModel.addElement(I18n.getString("calendar.schedule.empty"));
        }
    }
    
    // --- Metode Helper Kecil ---
    private int getSelectedRow() {
        return (tableEkspedisi != null) ? tableEkspedisi.getSelectedRow() : -1;
    }

    private void showWarning(String m) {
        JOptionPane.showMessageDialog(this, m, I18n.getString("msg.warning.title"), JOptionPane.WARNING_MESSAGE);
    }

    private JSeparator createVerticalSeparator() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        s.setPreferredSize(new Dimension(1, 20));
        return s;
    }

    private JLabel createStatsLabel() {
        JLabel l = new JLabel("Status: 0");
        l.setForeground(Color.WHITE);
        l.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        return l;
    }

    private JPanel createStatCard(String t, String v, Color c) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        p.setBackground(c);
        JLabel tl = new JLabel(t, SwingConstants.CENTER);
        tl.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16));
        JLabel vl = new JLabel(v, SwingConstants.CENTER);
        vl.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 48));
        vl.setForeground(new Color(0, 51, 102));
        p.add(tl, BorderLayout.NORTH);
        p.add(vl, BorderLayout.CENTER);
        return p;
    }

    private JFreeChart createBarChart(DefaultCategoryDataset d, String t, String cl, String vl) {
        JFreeChart bc = ChartFactory.createBarChart(t, cl, vl, d, PlotOrientation.VERTICAL, false, true, false);
        bc.getTitle().setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16));
        CategoryPlot p = bc.getCategoryPlot();
        ((BarRenderer) p.getRenderer()).setSeriesPaint(0, new Color(79, 129, 189));
        p.setBackgroundPaint(Color.WHITE);
        p.setDomainGridlinePaint(Color.LIGHT_GRAY);
        p.setRangeGridlinePaint(Color.LIGHT_GRAY);
        org.jfree.chart.axis.CategoryAxis domainAxis = p.getDomainAxis();
        domainAxis.setCategoryLabelPositions(org.jfree.chart.axis.CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0));
        return bc;
    }

     private void startRealTimeClock() {
        // Menggunakan javax.swing.Timer yang aman untuk UI Swing.
        // Timer ini akan memicu event setiap 1000 milidetik (1 detik).
        Timer timer = new Timer(1000, e -> {
            // Membuat formatter untuk tanggal dan waktu.
            // Locale("in", "ID") memastikan nama hari dan bulan dalam Bahasa Indonesia.
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy | HH:mm:ss 'WIB'", new Locale("in", "ID"));
            
            // Atur teks label dengan waktu saat ini yang sudah diformat.
            // Pastikan clockLabel sudah diinisialisasi sebelum metode ini dipanggil.
            if (clockLabel != null) {
                clockLabel.setText(sdf.format(new java.util.Date()));
            }
        });
        
        // Memulai timer agar berjalan di latar belakang.
        timer.start();
    }
}