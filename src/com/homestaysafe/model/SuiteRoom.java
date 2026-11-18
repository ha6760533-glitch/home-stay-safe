package com.homestaysafe.model;

/** Kamar tipe Suite (Inheritance dari {@link Kamar}). */
public class SuiteRoom extends Kamar {
    public SuiteRoom(int id, String no, String tipe, double harga, String status) {
        super(id, no, tipe, harga, status);
    }

    @Override public String getFasilitas() { return "AC, King Bed, Smart TV, Bathup, Breakfast"; }
    @Override public int getKapasitas() { return 4; }

    /** Suite mendapat sarapan gratis; override agar ringkasannya menyebutkannya. */
    @Override
    public String getRingkasan() {
        return super.getRingkasan() + " (termasuk sarapan)";
    }
}
