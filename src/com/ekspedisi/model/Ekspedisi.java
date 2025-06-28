// FILE: com/model/Ekspedisi.java
package com.model;

import java.io.Serializable;
import java.sql.Date;

/**
 * Kelas model (POJO) untuk entitas Ekspedisi.
 * Merepresentasikan struktur data dari tabel 'ekspedisi' di database.
 * Mengimplementasikan Serializable untuk memenuhi salah satu kriteria teknis.
 * [IMPLEMENTASI] Serializable
 */
public class Ekspedisi implements Serializable {
    // serialVersionUID diperlukan untuk proses serialisasi yang konsisten.
    private static final long serialVersionUID = 1L;

    private int id;
    private String namaTim;
    private Date tanggal;
    private String tujuan;
    private String status;
    private Double latitude;
    private Double longitude;
    private String catatan;
    private String pathFoto; // Hanya menyimpan nama file, bukan path lengkap

    // Constructors (tidak wajib, tapi baik untuk dimiliki)
    public Ekspedisi() {}

    // Getters and Setters (Dihasilkan otomatis, digunakan untuk mengakses dan mengatur properti)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNamaTim() { return namaTim; }
    public void setNamaTim(String namaTim) { this.namaTim = namaTim; }

    public Date getTanggal() { return tanggal; }
    public void setTanggal(Date tanggal) { this.tanggal = tanggal; }

    public String getTujuan() { return tujuan; }
    public void setTujuan(String tujuan) { this.tujuan = tujuan; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }

    public String getPathFoto() { return pathFoto; }
    public void setPathFoto(String pathFoto) { this.pathFoto = pathFoto; }
}