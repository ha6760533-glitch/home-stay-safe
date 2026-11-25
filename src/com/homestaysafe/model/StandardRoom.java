package com.homestaysafe.model;

/** Kamar tipe Standard (Inheritance dari {@link Kamar}). */
public class StandardRoom extends Kamar {
    public StandardRoom(int id, String no, String tipe, double harga, String status) {
        this(id, 1, no, tipe, harga, status, null);
    }

    public StandardRoom(int id, int idTipe, String no, String tipe, double harga, String status, String deskripsi) {
        super(id, idTipe, no, tipe, harga, status, deskripsi);
    }

    @Override public String getDefaultFasilitas() { return "Kamar nyaman dengan fasilitas standar, AC, Smart TV, dan Wi-Fi"; }
    @Override public int getKapasitas() { return 2; }
}