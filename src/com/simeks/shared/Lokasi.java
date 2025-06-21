package com.simeks.shared;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Lokasi implements Serializable {
    private static final long serialVersionUID = 4L; // Naikkan UID karena struktur berubah

    private String idTim;
    private String namaGunung;
    private String jalurPendakian;
    private String basecamp; // <-- FIELD BARU
    private LocalDateTime timestamp;

    public Lokasi(String idTim, String namaGunung, String jalurPendakian, String basecamp) {
        this.idTim = idTim;
        this.namaGunung = namaGunung;
        this.jalurPendakian = jalurPendakian;
        this.basecamp = basecamp; // <-- TAMBAHKAN DI KONSTRUKTOR
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getIdTim() { return idTim; }
    public String getNamaGunung() { return namaGunung; }
    public String getJalurPendakian() { return jalurPendakian; }
    public String getBasecamp() { return basecamp; } // <-- GETTER BARU
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "Log [Tim=" + idTim + ", Gunung=" + namaGunung + ", Jalur=" + jalurPendakian + ", Basecamp=" + basecamp + ", Waktu=" + timestamp.format(formatter) + "]";
    }
}