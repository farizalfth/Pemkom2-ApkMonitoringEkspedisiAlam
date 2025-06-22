package com.simeks.app.server;

import com.simeks.app.server.util.KoneksiDB;
import com.simeks.app.shared.SimeksRepository;
import com.simeks.app.shared.model.Ekspedisi;
import com.simeks.app.shared.model.Logbook;
import com.simeks.app.shared.model.User;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SimeksRepositoryImpl.java
 * Implementasi dari interface SimeksRepository. Ini adalah "otak" dari server.
 * Semua permintaan dari client akan diproses di dalam kelas ini.
 */
public class SimeksRepositoryImpl extends UnicastRemoteObject implements SimeksRepository {

    /**
     * Constructor wajib untuk kelas RMI.
     * @throws RemoteException
     */
    public SimeksRepositoryImpl() throws RemoteException {
        super();
    }

    /**
     * Memproses permintaan login dari client.
     */
    @Override
    public User login(String username, String password) throws RemoteException {
        System.out.println("SERVER INFO: Menerima permintaan login untuk user: '" + username + "'");
        
        User user = KoneksiDB.findUserByUsername(username);
        
        if (user != null && user.getPassword().equals(password)) {
            System.out.println("SERVER INFO: Login berhasil untuk user '" + username + "'.");
            return user;
        } else {
            System.out.println("SERVER WARNING: Login gagal untuk user '" + username + "'.");
            return null;
        }
    }

    /**
     * Mengambil data statistik untuk dashboard admin.
     */
    @Override
    public Map<String, Integer> getAdminDashboardStats() throws RemoteException {
        System.out.println("SERVER INFO: Menerima permintaan data statistik dashboard.");
        Map<String, Integer> stats = new HashMap<>();
        
        stats.put("ekspedisiAktif", KoneksiDB.countActiveEkspedisi());
        stats.put("totalPeserta", KoneksiDB.countTotalUsers());
        
        System.out.println("SERVER INFO: Mengirim data statistik ke client. Ekspedisi Aktif: " 
            + stats.get("ekspedisiAktif") + ", Total Peserta: " + stats.get("totalPeserta"));
        
        return stats;
    }

    /**
     * Mengambil daftar semua pengguna untuk panel manajemen pengguna admin.
     */
    @Override
    public List<User> getAllUsers() throws RemoteException {
        System.out.println("SERVER INFO: Menerima permintaan untuk mengambil semua data pengguna.");
        List<User> userList = KoneksiDB.findAllUsers();
        System.out.println("SERVER INFO: Mengirim " + userList.size() + " data pengguna ke client.");
        return userList;
    }

    /**
     * Mengambil data ekspedisi aktif untuk seorang user.
     */
    @Override
    public Ekspedisi getActiveEkspedisiForUser(int userId) throws RemoteException {
        System.out.println("SERVER INFO: Menerima permintaan data ekspedisi untuk user ID: " + userId);
        return KoneksiDB.findEkspedisiByUserId(userId);
    }

    /**
     * Menerima dan menyimpan logbook dari seorang user.
     */
    @Override
    public void submitLogbook(Logbook log) throws RemoteException {
        System.out.println("SERVER INFO: Menerima pengiriman logbook.");
        KoneksiDB.saveLogbook(log);
    }
}