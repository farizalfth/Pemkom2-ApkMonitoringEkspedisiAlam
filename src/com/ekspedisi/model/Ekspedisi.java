// FILE: com/ekspedisi/model/Ekspedisi.java
package com.ekspedisi.model;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Kelas model (POJO) untuk entitas Ekspedisi.
 * Versi final ini mencakup properti untuk jenis pendakian.
 */
public class Ekspedisi implements Serializable {
    private static final long serialVersionUID = 3L; // Versi diperbarui

    // Properti dasar ekspedisi
    private int id;
    private String namaTim;
    private String tujuan;
    private String jenisPendakian; // Field baru
    private Date tanggal;
    private String status;
    private Double latitude;
    private Double longitude;
    private String catatan;
    private String pathFoto;

    // Properti untuk daftar anggota tim
    private List<AnggotaTim> anggota = new ArrayList<>();

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNamaTim() { return namaTim; }
    public void setNamaTim(String namaTim) { this.namaTim = namaTim; }

    public String getTujuan() { return tujuan; }
    public void setTujuan(String tujuan) { this.tujuan = tujuan; }

    public String getJenisPendakian() { return jenisPendakian; }
    public void setJenisPendakian(String jenisPendakian) { this.jenisPendakian = jenisPendakian; }
    
    public Date getTanggal() { return tanggal; }
    public void setTanggal(Date tanggal) { this.tanggal = tanggal; }

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

    public List<AnggotaTim> getAnggota() { return anggota; }
    public void setAnggota(List<AnggotaTim> anggota) { this.anggota = anggota; }
}