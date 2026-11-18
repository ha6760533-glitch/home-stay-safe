package com.homestaysafe.model;

/**
 * Abstract class Kamar → Abstraction + base Inheritance.
 * Setiap tipe kamar (Standard, Deluxe, Suite) menentukan sendiri fasilitas,
 * kapasitas, dan deskripsinya lewat method abstract → Polymorphism.
 *
 * Pemanggil (panel GUI) cukup memanggil {@code kamar.getFasilitas()} tanpa
 * if/else berdasarkan tipe kamar.
 */
public abstract class Kamar {

    private final int idKamar;
    private final String noKamar;
    private final String tipeKamar;
    private final double hargaPerMalam;
    private final String statusKamar;

    protected Kamar(int idKamar, String noKamar, String tipeKamar, double hargaPerMalam, String statusKamar) {
        this.idKamar = idKamar;
        this.noKamar = noKamar;
        this.tipeKamar = tipeKamar;
        this.hargaPerMalam = hargaPerMalam;
        this.statusKamar = statusKamar;
    }

    // ===== Encapsulation =====
    public int getIdKamar() { return idKamar; }
    public String getNoKamar() { return noKamar; }
    public String getTipeKamar() { return tipeKamar; }
    public double getHargaPerMalam() { return hargaPerMalam; }
    public String getStatusKamar() { return statusKamar; }

    // ===== Abstraction + Polymorphism (di-override subclass) =====
    public abstract String getFasilitas();
    public abstract int getKapasitas();

    /** Biaya sewa untuk sejumlah malam. */
    public double hitungBiaya(int jumlahMalam) {
        return hargaPerMalam * Math.max(jumlahMalam, 0);
    }

    /** Ringkasan satu baris, memanfaatkan method polymorphic subclass. */
    public String getRingkasan() {
        return tipeKamar + " No. " + noKamar + " | " + getKapasitas() + " orang | " + getFasilitas();
    }

    /**
     * Factory: membuat subclass yang sesuai dengan tipe yang tersimpan di database.
     * Tipe yang tidak dikenal diperlakukan sebagai Standard (tipe aslinya tetap disimpan).
     */
    public static Kamar buat(int id, String no, String tipe, double harga, String status) {
        String t = tipe == null ? "" : tipe.trim();
        if (t.equalsIgnoreCase("Suite"))  return new SuiteRoom(id, no, t, harga, status);
        if (t.equalsIgnoreCase("Deluxe")) return new DeluxeRoom(id, no, t, harga, status);
        return new StandardRoom(id, no, t.isEmpty() ? "Standard" : t, harga, status);
    }
}
