package com.simeks.app.server;

import com.simeks.app.server.util.KoneksiDB;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Server.java
 * Ini adalah titik masuk (entry point) untuk aplikasi sisi server.
 * Tugas utamanya adalah menyalakan RMI Registry dan mendaftarkan layanan (repository)
 * agar bisa diakses oleh client.
 */
public class Server {

    public static void main(String[] args) {
        try {
            // ================== LANGKAH KRUSIAL ==================
            // Baris ini "memaksa" Java Virtual Machine (JVM) untuk me-load kelas KoneksiDB.
            // Saat kelas di-load, 'static block' di dalamnya akan otomatis dijalankan.
            // Ini memastikan database in-memory kita sudah siap SEBELUM ada permintaan dari client.
            Class.forName(KoneksiDB.class.getName());
            // ====================================================

            // 1. Membuat RMI Registry di port 1099.
            // Port ini adalah 'pintu gerbang' tempat client akan mencari layanan.
            LocateRegistry.createRegistry(1099);
            
            // 2. Membuat instance dari implementasi repository kita.
            // Inilah objek yang berisi semua logika bisnis server (login, get data, dll).
            SimeksRepositoryImpl repository = new SimeksRepositoryImpl();
            
            // 3. Mendaftarkan (rebind) instance repository ke RMI Registry.
            // Kita memberinya nama "SimeksRepo". Client akan menggunakan nama ini untuk menemukannya.
            // Formatnya adalah: rmi://[host]/[nama_layanan]
            Naming.rebind("rmi://localhost/SimeksRepo", repository);
            
            // Tampilkan pesan bahwa server sudah berhasil berjalan.
            System.out.println("\n=========================================");
            System.out.println("      SERVER SIMEKSAPP TELAH BERJALAN     ");
            System.out.println("=========================================");
            System.out.println("Server siap menerima koneksi di port 1099...");

        } catch (Exception e) {
            // Menangani error jika terjadi masalah saat menyalakan server (misalnya port sudah dipakai).
            System.err.println("SERVER FAILED TO START. Exception: " + e.toString());
            e.printStackTrace();
        }
    }
}