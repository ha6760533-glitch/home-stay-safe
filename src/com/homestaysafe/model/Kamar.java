package com.homestaysafe.model;

// ABSTRACTION: class abstract Kamar
// INHERITANCE: parent bagi StandardRoom, DeluxeRoom, ExecutiveRoom, SuiteRoom
public abstract class Kamar {

    // ENCAPSULATION
    private final int idKamar;
    private final int idTipeKamar;
    private final String noKamar;
    private final String tipeKamar;
    private final double hargaPerMalam;
    private final String statusKamar;
    private final String deskripsi;

    protected Kamar(int idKamar, int idTipeKamar, String noKamar, String tipeKamar,
                    double hargaPerMalam, String statusKamar, String deskripsi) {
        this.idKamar = idKamar;
        this.idTipeKamar = idTipeKamar;
        this.noKamar = noKamar;
        this.tipeKamar = tipeKamar;
        this.hargaPerMalam = hargaPerMalam;
        this.statusKamar = statusKamar;
        this.deskripsi = deskripsi;
    }

    public int getIdKamar() { return idKamar; }
    public int getIdTipeKamar() { return idTipeKamar; }
    public String getNoKamar() { return noKamar; }
    public String getTipeKamar() { return tipeKamar; }
    public double getHargaPerMalam() { return hargaPerMalam; }
    public String getStatusKamar() { return statusKamar; }
    public String getDeskripsi() { return deskripsi; }

    // ABSTRACTION + POLYMORPHISM: tiap tipe kamar override sendiri
    public abstract String getDefaultFasilitas();
    public abstract int getKapasitas();

    public String getFasilitas() {
        return (deskripsi != null && !deskripsi.trim().isEmpty())
                ? deskripsi.trim()
                : getDefaultFasilitas();
    }

    public double hitungBiaya(int jumlahMalam) {
        return hargaPerMalam * Math.max(jumlahMalam, 0);
    }

    public String getRingkasan() {
        return tipeKamar + " No. " + noKamar + " | " + getKapasitas() + " orang | " + getFasilitas();
    }

    // POLYMORPHISM (Factory): return subclass berbeda sesuai tipe
    public static Kamar buat(int id, String no, String tipe, double harga, String status) {
        return buat(id, 0, no, tipe, harga, status, null);
    }

    public static Kamar buat(int id, int idTipe, String no, String tipe, double harga, String status, String deskripsi) {
        String t = tipe == null ? "" : tipe.trim();
        if (t.equalsIgnoreCase("Presidential Suite") || t.equalsIgnoreCase("Junior Suite") || t.equalsIgnoreCase("Suite")) {
            return new SuiteRoom(id, idTipe, no, t, harga, status, deskripsi);
        }
        if (t.equalsIgnoreCase("Executive")) {
            return new ExecutiveRoom(id, idTipe, no, t, harga, status, deskripsi);
        }
        if (t.equalsIgnoreCase("Deluxe")) {
            return new DeluxeRoom(id, idTipe, no, t, harga, status, deskripsi);
        }
        return new StandardRoom(id, idTipe, no, t.isEmpty() ? "Standard" : t, harga, status, deskripsi);
    }
}
