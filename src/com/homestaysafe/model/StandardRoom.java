package com.homestaysafe.model;

/** Kamar tipe Standard (Inheritance dari {@link Kamar}). */
public class StandardRoom extends Kamar {
    public StandardRoom(int id, String no, String tipe, double harga, String status) {
        super(id, no, tipe, harga, status);
    }

    @Override public String getFasilitas() { return "AC, Single Bed, Wi-Fi"; }
    @Override public int getKapasitas() { return 1; }
}
