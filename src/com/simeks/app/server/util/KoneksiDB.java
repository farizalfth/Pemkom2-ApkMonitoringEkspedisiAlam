package com.simeks.app.server.util;

import com.simeks.app.shared.model.Ekspedisi;
import com.simeks.app.shared.model.Logbook;
import com.simeks.app.shared.model.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * KoneksiDB.java
 * Kelas ini mensimulasikan database menggunakan data in-memory.
 */
public class KoneksiDB {

    private static final Map<String, User> usersByUsername = new HashMap<>();
    private static final List<Ekspedisi> ekspedisiList = new ArrayList<>();
    private static final List<Logbook> logbooks = new ArrayList<>();

    static {
        System.out.println("SERVER INFO: Menginisialisasi database in-memory...");
        
        User admin = new User(1, "admin", "admin123", "Administrator Utama", "ADMIN");
        User user1 = new User(2, "budi", "user123", "Budi Pendaki", "USER");
        User user2 = new User(3, "siti", "user123", "Siti Pelacak", "USER");

        usersByUsername.put(admin.getUsername(), admin);
        usersByUsername.put(user1.getUsername(), user1);
        usersByUsername.put(user2.getUsername(), user2);

        Ekspedisi ekspedisi1 = new Ekspedisi(101, "Ekspedisi Semeru 2024", "Ranu Pane - Kalimati", "Aktif", Arrays.asList(user1.getId(), user2.getId()));
        ekspedisiList.add(ekspedisi1);
        
        System.out.println("SERVER INFO: Inisialisasi selesai. " + usersByUsername.size() + " pengguna berhasil dimuat.");
    }

    public static User findUserByUsername(String username) {
        return usersByUsername.get(username);
    }
    
    public static List<User> findAllUsers() {
        return new ArrayList<>(usersByUsername.values());
    }
    
    public static Ekspedisi findEkspedisiByUserId(int userId) {
        return ekspedisiList.stream()
                .filter(e -> e.getPesertaIds().contains(userId))
                .findFirst()
                .orElse(null);
    }

    public static void saveLogbook(Logbook log) {
        logbooks.add(log);
        System.out.println("SERVER INFO: Logbook baru berhasil disimpan dari user ID: " + log.getUserId() + " dengan catatan: '" + log.getCatatan() + "'");
    }

    public static int countActiveEkspedisi() {
        return (int) ekspedisiList.stream().filter(e -> "Aktif".equals(e.getStatus())).count();
    }

    /**
     * Menghitung jumlah pengguna dengan role "USER".
     * @return Jumlah total peserta.
     */
    public static int countTotalUsers() {
        // ================== PERBAIKAN DI SINI (SEKITAR LINE 70) ==================
        // Kode yang benar adalah menghitung dari 'usersByUsername.values()'
        // dan memfilter berdasarkan role "USER".
        return (int) usersByUsername.values().stream()
                .filter(user -> "USER".equals(user.getRole()))
                .count();
        // =========================================================================
    }
}