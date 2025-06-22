package com.simeks.app.shared.model;

import java.io.Serializable;

/**
 * User.java
 * Kelas model (POJO - Plain Old Java Object) yang merepresentasikan data seorang pengguna.
 * Kelas ini 'Serializable' agar objeknya bisa dikirim melalui jaringan (RMI).
 */
public class User implements Serializable {

    // Versi unik untuk serialisasi, penting untuk RMI
    private static final long serialVersionUID = 1L;

    // Properti/Atribut dari seorang User
    private int id;
    private String username;
    private String password; // Di aplikasi nyata, ini seharusnya di-hash
    private String fullName;
    private String role;     // Contoh: "ADMIN" atau "USER"

    /**
     * Constructor untuk membuat objek User baru.
     * @param id ID unik pengguna
     * @param username Username untuk login
     * @param password Password untuk login
     * @param fullName Nama lengkap pengguna
     * @param role Peran pengguna (ADMIN/USER)
     */
    public User(int id, String username, String password, String fullName, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    // --- GETTER METHODS ---
    // Method-method ini digunakan untuk mendapatkan nilai dari properti privat.

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }
}