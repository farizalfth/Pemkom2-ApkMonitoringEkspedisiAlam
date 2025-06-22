package com.simeks.app.client.service;

import com.simeks.app.shared.SimeksRepository;
import java.rmi.Naming;
import javax.swing.JOptionPane;

public class ClientService {
    private static ClientService instance;
    private SimeksRepository repository;

    private ClientService() {
        try {
            repository = (SimeksRepository) Naming.lookup("rmi://localhost/SimeksRepo");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Tidak dapat terhubung ke server.\nPastikan server sudah berjalan.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    public static synchronized ClientService getInstance() {
        if (instance == null) {
            instance = new ClientService();
        }
        return instance;
    }

    public SimeksRepository getRepository() {
        return repository;
    }
}