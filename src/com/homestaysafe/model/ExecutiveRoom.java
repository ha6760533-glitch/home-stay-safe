package com.homestaysafe.model;

// INHERITANCE: ExecutiveRoom extends Kamar
public class ExecutiveRoom extends Kamar {
    public ExecutiveRoom(int id, String no, String tipe, double harga, String status) {
        this(id, 3, no, tipe, harga, status, null);
    }

    public ExecutiveRoom(int id, int idTipe, String no, String tipe, double harga, String status, String deskripsi) {
        super(id, idTipe, no, tipe, harga, status, deskripsi);
    }

    // POLYMORPHISM
    @Override
    public String getDefaultFasilitas() {
        return "Kamar bisnis eksklusif dengan meja kerja ergonomis dan akses lounge";
    }

    @Override
    public int getKapasitas() { return 2; }
}
