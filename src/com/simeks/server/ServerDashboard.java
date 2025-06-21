// File: src/com/simeks/server/ServerDashboard.java
package com.simeks.server;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerDashboard extends JFrame {

    public static final int PORT = 9999;
    private JToggleButton toggleServer;
    private JButton btnGenerateReport;
    private JLabel lblStatus;
    private JTextArea txtLogArea;
    private Thread serverThread;
    private ServerSocket serverSocket;

    public ServerDashboard() {
        super("SIMEKS Server Dashboard");
        initComponents();
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 500);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toggleServer = new JToggleButton("Start Server");
        toggleServer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        toggleServer.setPreferredSize(new Dimension(140, 35));

        btnGenerateReport = new JButton("Generate Laporan");
        btnGenerateReport.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnGenerateReport.setPreferredSize(new Dimension(180, 35));
        btnGenerateReport.setEnabled(false);

        lblStatus = new JLabel("Status: Stopped");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStatus.setForeground(Color.RED);

        controlPanel.add(toggleServer);
        controlPanel.add(btnGenerateReport);
        controlPanel.add(lblStatus);
        
        txtLogArea = new JTextArea();
        txtLogArea.setEditable(false);
        txtLogArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScrollPane = new JScrollPane(txtLogArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Server Log"));

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(logScrollPane, BorderLayout.CENTER);
        this.add(mainPanel);

        toggleServer.addActionListener(e -> onToggleServerActionPerformed());
        btnGenerateReport.addActionListener(e -> onGenerateReportActionPerformed());
    }
    
    private void onToggleServerActionPerformed() {
        if (toggleServer.isSelected()) {
            toggleServer.setText("Stop Server");
            lblStatus.setText("Status: Running");
            lblStatus.setForeground(new Color(0, 153, 51));
            btnGenerateReport.setEnabled(true);
            txtLogArea.setText("");
            startServer();
        } else {
            toggleServer.setText("Start Server");
            lblStatus.setText("Status: Stopped");
            lblStatus.setForeground(Color.RED);
            btnGenerateReport.setEnabled(false);
            stopServer();
        }
    }
    
    private void onGenerateReportActionPerformed() {
        addLog("REPORT: Memulai pembuatan file laporan HTML...");
        btnGenerateReport.setEnabled(false);
        btnGenerateReport.setText("Generating...");

        new Thread(() -> {
            try {
                LogReportGenerator generator = new LogReportGenerator();
                generator.generateHtmlFile(this);
            } finally {
                SwingUtilities.invokeLater(() -> {
                    btnGenerateReport.setEnabled(true);
                    btnGenerateReport.setText("Generate Laporan");
                });
            }
        }).start();
    }
    
    public void addLog(String message) {
        SwingUtilities.invokeLater(() -> {
            txtLogArea.append(message + "\n");
            txtLogArea.setCaretPosition(txtLogArea.getDocument().getLength());
        });
    }

    private void startServer() {
        serverThread = new Thread(() -> {
            KoneksiDB.initDatabase();
            addLog("SYSTEM: Server thread started.");
            try {
                serverSocket = new ServerSocket(PORT);
                addLog("SYSTEM: Server listening on port " + PORT + "...");
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        new ClientHandler(clientSocket, this).start();
                    } catch (IOException e) {
                        if (Thread.currentThread().isInterrupted()) break;
                    }
                }
            } catch (IOException e) {
                addLog("FATAL: Could not start server: " + e.getMessage());
            }
        });
        serverThread.start();
    }

    private void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            if (serverThread != null) serverThread.interrupt();
        } catch (IOException e) {
            addLog("ERROR: Failed to close server socket: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ex) { System.err.println("Failed to set system look and feel."); }
        SwingUtilities.invokeLater(() -> new ServerDashboard().setVisible(true));
    }
}