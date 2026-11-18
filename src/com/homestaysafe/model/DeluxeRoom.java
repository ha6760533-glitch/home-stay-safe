package com.homestaysafe.model;

/** Kamar tipe Deluxe (Inheritance dari {@link Kamar}). */
public class DeluxeRoom extends Kamar {
    public DeluxeRoom(int id, String no, String tipe, double harga, String status) {
        super(id, no, tipe, harga, status);
    }

    @Override public String getFasilitas() { return "AC, Queen Bed, Wi-Fi, Water Heater"; }
    @Override public int getKapasitas() { return 2; }
}
