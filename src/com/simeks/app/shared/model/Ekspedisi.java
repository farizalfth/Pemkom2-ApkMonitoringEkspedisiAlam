package com.simeks.app.shared.model;

import java.io.Serializable;
import java.util.List;

public class Ekspedisi implements Serializable {
    private int id;
    private String nama, jalur, status;
    private List<Integer> pesertaIds;

    public Ekspedisi(int id, String nama, String jalur, String status, List<Integer> pesertaIds) {
        this.id = id; this.nama = nama; this.jalur = jalur; this.status = status; this.pesertaIds = pesertaIds;
    }
    // Getters...
    public String getNama() { return nama; }
    public String getJalur() { return jalur; }
    public String getStatus() { return status; }
    public List<Integer> getPesertaIds() { return pesertaIds; }
}