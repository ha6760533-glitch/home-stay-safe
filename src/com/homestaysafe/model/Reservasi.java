package com.homestaysafe.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Model reservasi. Mengimplementasikan dua interface sekaligus:
 *  - Dendable  → aturan perhitungan denda keterlambatan check-out
 *  - Cetakable → pembuatan nota pembayaran
 *
 * Catatan: {@code totalBiaya} adalah biaya sewa kamar saja. Tagihan F&B disimpan
 * di {@code totalMakanan}, dan denda di {@code denda} (telat) + {@code dendaKerusakan}.
 */
public class Reservasi implements Cetakable, Dendable {

    public static final double TARIF_DENDA_PER_JAM = 50000.0;
    public static final LocalTime JAM_BATAS_CHECKOUT = LocalTime.of(12, 0);

    private int idReservasi;
    private int idUser;
    private String noKamar;
    private String tipeKamar;
    private String namaTamu;
    private LocalDate tglCheckin;
    private LocalDate tglCheckoutRencana;
    private LocalDateTime tglCheckoutAktual;
    private double totalBiaya;        // biaya sewa kamar
    private double denda;             // denda keterlambatan
    private double dendaKerusakan;    // denda kerusakan fasilitas
    private double totalMakanan;      // tagihan F&B
    private String statusReservasi;

    public Reservasi() {}

    // ===== Encapsulation =====
    public int getIdReservasi() { return idReservasi; }
    public void setIdReservasi(int idReservasi) { this.idReservasi = idReservasi; }
    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
    public String getNoKamar() { return noKamar; }
    public void setNoKamar(String noKamar) { this.noKamar = noKamar; }
    public String getTipeKamar() { return tipeKamar; }
    public void setTipeKamar(String tipeKamar) { this.tipeKamar = tipeKamar; }
    public String getNamaTamu() { return namaTamu; }
    public void setNamaTamu(String namaTamu) { this.namaTamu = namaTamu; }
    public LocalDate getTglCheckin() { return tglCheckin; }
    public void setTglCheckin(LocalDate tglCheckin) { this.tglCheckin = tglCheckin; }
    public LocalDate getTglCheckoutRencana() { return tglCheckoutRencana; }
    public void setTglCheckoutRencana(LocalDate tglCheckoutRencana) { this.tglCheckoutRencana = tglCheckoutRencana; }
    public LocalDateTime getTglCheckoutAktual() { return tglCheckoutAktual; }
    public void setTglCheckoutAktual(LocalDateTime tglCheckoutAktual) { this.tglCheckoutAktual = tglCheckoutAktual; }
    public double getTotalBiaya() { return totalBiaya; }
    public void setTotalBiaya(double totalBiaya) { this.totalBiaya = totalBiaya; }
    public double getDenda() { return denda; }
    public void setDenda(double denda) { this.denda = denda; }
    public double getDendaKerusakan() { return dendaKerusakan; }
    public void setDendaKerusakan(double dendaKerusakan) { this.dendaKerusakan = Math.max(0, dendaKerusakan); }
    public double getTotalMakanan() { return totalMakanan; }
    public void setTotalMakanan(double totalMakanan) { this.totalMakanan = totalMakanan; }
    public String getStatusReservasi() { return statusReservasi; }
    public void setStatusReservasi(String statusReservasi) { this.statusReservasi = statusReservasi; }

    // ===== Logika bisnis =====

    /** Batas waktu check-out: tanggal rencana pukul 12:00. */
    public LocalDateTime getBatasCheckout() {
        return tglCheckoutRencana == null ? null : tglCheckoutRencana.atTime(JAM_BATAS_CHECKOUT);
    }

    /** Jumlah jam keterlambatan (dibulatkan ke atas, minimal 1 jam jika lewat batas). */
    public long hitungJamTelat(LocalDateTime waktuCheckoutAktual) {
        LocalDateTime batas = getBatasCheckout();
        if (batas == null || waktuCheckoutAktual == null || !waktuCheckoutAktual.isAfter(batas)) return 0;
        long menitTelat = Duration.between(batas, waktuCheckoutAktual).toMinutes();
        return Math.max(1, (long) Math.ceil(menitTelat / 60.0));
    }

    /** Polymorphism via Dendable: denda keterlambatan = jam telat × tarif per jam. */
    @Override
    public double hitungDenda(LocalDateTime waktuCheckoutAktual) {
        return hitungJamTelat(waktuCheckoutAktual) * TARIF_DENDA_PER_JAM;
    }

    public double getTotalDenda() {
        return denda + dendaKerusakan;
    }

    public double hitungGrandTotal() {
        return totalBiaya + totalMakanan + getTotalDenda();
    }

    /** Polymorphism via Cetakable. */
    @Override
    public String cetakNota() {
        return ""
                + "========================================\n"
                + "         HOMESTAYSAFE RESORT\n"
                + "       NOTA PEMBAYARAN CHECK-OUT\n"
                + "========================================\n"
                + "ID Transaksi : RSV-" + idReservasi + "\n"
                + "Nama Tamu    : " + (namaTamu != null ? namaTamu : "-") + "\n"
                + "Nomor Kamar  : " + (noKamar != null ? noKamar : "-") + "\n"
                + "Tipe Kamar   : " + (tipeKamar != null ? tipeKamar : "-") + "\n"
                + "----------------------------------------\n"
                + "Biaya Kamar  : Rp " + String.format("%,.0f", totalBiaya) + "\n"
                + "Tagihan F&B  : Rp " + String.format("%,.0f", totalMakanan) + "\n"
                + "Denda Telat  : Rp " + String.format("%,.0f", denda) + "\n"
                + "Denda Rusak  : Rp " + String.format("%,.0f", dendaKerusakan) + "\n"
                + "----------------------------------------\n"
                + "TOTAL AKHIR  : Rp " + String.format("%,.0f", hitungGrandTotal()) + "\n"
                + "========================================\n"
                + "Terima kasih atas kunjungan Anda!\n";
    }
}
