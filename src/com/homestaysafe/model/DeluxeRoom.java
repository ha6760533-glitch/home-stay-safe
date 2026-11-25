package com.homestaysafe.model;

/** Kamar tipe Deluxe (Inheritance dari {@link Kamar}). */
public class DeluxeRoom extends Kamar {
    public DeluxeRoom(int id, String no, String tipe, double harga, String status) {
        this(id, 2, no, tipe, harga, status, null);
    }

    public DeluxeRoom(int id, int idTipe, String no, String tipe, double harga, String status, String deskripsi) {
        super(id, idTipe, no, tipe, harga, status, deskripsi);
    }

    @Override public String getDefaultFasilitas() { return "Kamar luas dengan balkon kota, minibar, dan sarapan 2 orang"; }
    @Override public int getKapasitas() { return 2; }
}