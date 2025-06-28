// FILE: com/ekspedisi/model/AnggotaTim.java
package com.ekspedisi.model;

import java.io.Serializable;

/**
 * Kelas model (POJO) untuk entitas AnggotaTim.
 * Merepresentasikan data satu anggota tim dalam sebuah ekspedisi,
 * termasuk nama, jenis kelamin, no. tlp, dan alamat.
 */
public class AnggotaTim implements Serializable {
    private static final long serialVersionUID = 2L; // Versi diperbarui
    
    private int id;
    private int ekspedisiId;
    private String nama;
    private String jenisKelamin; // Field baru
    private String noTlp;
    private String alamat;

    // Konstruktor default
    public AnggotaTim() {}

    // Konstruktor dengan parameter untuk kemudahan
    public AnggotaTim(String nama, String jenisKelamin, String noTlp, String alamat) {
        this.nama = nama;
        this.jenisKelamin = jenisKelamin;
        this.noTlp = noTlp;
        this.alamat = alamat;
    }

    // Getters dan Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEkspedisiId() { return ekspedisiId; }
    public void setEkspedisiId(int ekspedisiId) { this.ekspedisiId = ekspedisiId; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getJenisKelamin() { return jenisKelamin; }
    public void setJenisKelamin(String jenisKelamin) { this.jenisKelamin = jenisKelamin; }

    public String getNoTlp() { return noTlp; }
    public void setNoTlp(String noTlp) { this.noTlp = noTlp; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
}