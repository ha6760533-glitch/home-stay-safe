package com.homestaysafe.model;

// INHERITANCE: SuiteRoom extends Kamar
public class SuiteRoom extends Kamar {
    public SuiteRoom(int id, String no, String tipe, double harga, String status) {
        this(id, 4, no, tipe, harga, status, null);
    }

    public SuiteRoom(int id, int idTipe, String no, String tipe, double harga, String status, String deskripsi) {
        super(id, idTipe, no, tipe, harga, status, deskripsi);
    }

    // POLYMORPHISM
    @Override
    public String getDefaultFasilitas() {
        if ("Presidential Suite".equalsIgnoreCase(getTipeKamar())) {
            return "Suite termewah lantai atas dengan ruang tamu, ruang makan, dan layanan butler";
        }
        return "Suite modern dengan area duduk terpisah dan bathtub mewah";
    }

    @Override
    public int getKapasitas() {
        return "Presidential Suite".equalsIgnoreCase(getTipeKamar()) ? 4 : 3;
    }

    @Override
    public String getRingkasan() {
        return super.getRingkasan() + " (termasuk sarapan & fasilitas VIP)";
    }
}
