package com.simeks.app.shared.model;

import java.io.Serializable;
import java.util.Date;

public class Logbook implements Serializable {
    private int userId;
    private String catatan;
    private Date timestamp;

    public Logbook(int userId, String catatan) {
        this.userId = userId; this.catatan = catatan; this.timestamp = new Date();
    }
    // Getters...
    public String getCatatan() { return catatan; }
    public Date getTimestamp() { return timestamp; }

    public String getUserId() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}