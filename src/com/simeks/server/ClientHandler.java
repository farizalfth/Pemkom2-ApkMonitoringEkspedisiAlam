package com.simeks.server;

import com.simeks.shared.CryptoUtils;
import com.simeks.shared.Lokasi;
import java.io.ObjectInputStream;
import java.net.Socket;
import javax.crypto.SealedObject;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final LokasiDAO lokasiDAO;
    private final ServerDashboard dashboard;

    public ClientHandler(Socket socket, ServerDashboard dashboard) {
        this.clientSocket = socket;
        this.dashboard = dashboard;
        this.lokasiDAO = new LokasiDAO();
    }
    @Override
    public void run() {
        try (ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {
            SealedObject sealedObject = (SealedObject) ois.readObject();
            dashboard.addLog("INFO: Received encrypted object from " + clientSocket.getInetAddress().getHostAddress());
            Object decryptedObject = CryptoUtils.decrypt(sealedObject);
            dashboard.addLog("INFO: Object successfully decrypted.");
            if (decryptedObject instanceof Lokasi) {
                Lokasi lokasi = (Lokasi) decryptedObject;
                dashboard.addLog("DATA: " + lokasi);
                lokasiDAO.save(lokasi);
                dashboard.addLog("DB: Log data for team " + lokasi.getIdTim() + " saved.");
            }
        } catch (Exception e) {
            dashboard.addLog("ERROR in ClientHandler: " + e.getMessage());
        }
    }
}