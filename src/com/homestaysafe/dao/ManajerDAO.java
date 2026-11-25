package com.homestaysafe.dao;

import com.homestaysafe.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManajerDAO {

    public static class PendapatanItem {
        private String periodeLabel;
        private int jumlahTransaksi;
        private double totalPendapatan;

        public PendapatanItem(String periodeLabel, int jumlahTransaksi, double totalPendapatan) {
            this.periodeLabel = periodeLabel;
            this.jumlahTransaksi = jumlahTransaksi;
            this.totalPendapatan = totalPendapatan;
        }

        public String getPeriodeLabel() { return periodeLabel; }
        public int getJumlahTransaksi() { return jumlahTransaksi; }
        public double getTotalPendapatan() { return totalPendapatan; }
    }

    public static class StatistikKamarItem {
        private int peringkat;
        private String noKamar;
        private String tipeKamar;
        private int frekuensiBooking;
        private double totalOmzet;

        public StatistikKamarItem(int peringkat, String noKamar, String tipeKamar, int frekuensiBooking, double totalOmzet) {
            this.peringkat = peringkat;
            this.noKamar = noKamar;
            this.tipeKamar = tipeKamar;
            this.frekuensiBooking = frekuensiBooking;
            this.totalOmzet = totalOmzet;
        }

        public int getPeringkat() { return peringkat; }
        public String getNoKamar() { return noKamar; }
        public String getTipeKamar() { return tipeKamar; }
        public int getFrekuensiBooking() { return frekuensiBooking; }
        public double getTotalOmzet() { return totalOmzet; }
    }

    public static class TransaksiReadOnlyItem {
        private int idReservasi;
        private String namaTamu;
        private String noIdentitas;
        private String noKamar;
        private String tipeKamar;
        private String tglCheckin;
        private String tglCheckout;
        private double denda;
        private double totalBiaya;
        private String statusReservasi;

        public TransaksiReadOnlyItem(int idReservasi, String namaTamu, String noIdentitas, String noKamar, String tipeKamar, String tglCheckin, String tglCheckout, double denda, double totalBiaya, String statusReservasi) {
            this.idReservasi = idReservasi;
            this.namaTamu = namaTamu;
            this.noIdentitas = noIdentitas;
            this.noKamar = noKamar;
            this.tipeKamar = tipeKamar;
            this.tglCheckin = tglCheckin;
            this.tglCheckout = tglCheckout;
            this.denda = denda;
            this.totalBiaya = totalBiaya;
            this.statusReservasi = statusReservasi;
        }

        public int getIdReservasi() { return idReservasi; }
        public String getNamaTamu() { return namaTamu; }
        public String getNoIdentitas() { return noIdentitas; }
        public String getNoKamar() { return noKamar; }
        public String getTipeKamar() { return tipeKamar; }
        public String getTglCheckin() { return tglCheckin; }
        public String getTglCheckout() { return tglCheckout; }
        public double getDenda() { return denda; }
        public double getTotalBiaya() { return totalBiaya; }
        public String getStatusReservasi() { return statusReservasi; }
    }

    public List<PendapatanItem> getLaporanPendapatan(String modePeriode) {
        List<PendapatanItem> list = new ArrayList<>();
        String formatSql;
        if ("Harian".equalsIgnoreCase(modePeriode)) {
            formatSql = "TO_CHAR(tgl_checkin, 'YYYY-MM-DD')";
        } else if ("Tahunan".equalsIgnoreCase(modePeriode)) {
            formatSql = "TO_CHAR(tgl_checkin, 'YYYY')";
        } else {
            formatSql = "TO_CHAR(tgl_checkin, 'YYYY-MM')";
        }

        String sql = "SELECT " + formatSql + " AS periode, " +
                "COUNT(*) AS jml_transaksi, " +
                "COALESCE(SUM(grand_total_pemasukan), 0) AS total_omzet " +
                "FROM v_laporan_pemasukan " +
                "WHERE status_reservasi IN ('Disetujui', 'Check-in', 'Selesai') " +
                "GROUP BY periode " +
                "ORDER BY periode ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new PendapatanItem(
                        rs.getString("periode"),
                        rs.getInt("jml_transaksi"),
                        rs.getDouble("total_omzet")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<StatistikKamarItem> getStatistikKamarTerfavorit(String filterWaktu) {
        List<StatistikKamarItem> list = new ArrayList<>();
        String kondisiWaktu = "";

        if ("7 Hari Terakhir".equalsIgnoreCase(filterWaktu)) {
            kondisiWaktu = " AND v.tgl_checkin >= CURRENT_DATE - INTERVAL '7 days'";
        } else if ("Bulan Ini".equalsIgnoreCase(filterWaktu)) {
            kondisiWaktu = " AND DATE_TRUNC('month', v.tgl_checkin) = DATE_TRUNC('month', CURRENT_DATE)";
        } else if ("Tahun Ini".equalsIgnoreCase(filterWaktu)) {
            kondisiWaktu = " AND DATE_TRUNC('year', v.tgl_checkin) = DATE_TRUNC('year', CURRENT_DATE)";
        }

        String sql = "SELECT k.no_kamar, tk.nama_tipe AS tipe_kamar, " +
                "COUNT(v.id_reservasi) AS frekuensi, " +
                "COALESCE(SUM(v.total_biaya_kamar), 0) AS omzet " +
                "FROM kamar k " +
                "JOIN tipe_kamar tk ON k.id_tipe_kamar = tk.id_tipe_kamar " +
                "LEFT JOIN v_laporan_pemasukan v ON TRIM(k.no_kamar) = TRIM(v.no_kamar) " +
                "AND v.status_reservasi NOT IN ('Ditolak', 'Dibatalkan') " + kondisiWaktu + " " +
                "GROUP BY k.no_kamar, tk.nama_tipe " +
                "ORDER BY frekuensi DESC, omzet DESC, k.no_kamar ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int rank = 1;
            while (rs.next()) {
                list.add(new StatistikKamarItem(
                        rank++,
                        rs.getString("no_kamar"),
                        rs.getString("tipe_kamar"),
                        rs.getInt("frekuensi"),
                        rs.getDouble("omzet")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<TransaksiReadOnlyItem> getRiwayatTransaksi(String tglDari, String tglSampai, String keyword) {
        List<TransaksiReadOnlyItem> list = new ArrayList<>();

        java.sql.Date sqlDari;
        java.sql.Date sqlSampai;
        try {
            sqlDari = java.sql.Date.valueOf(tglDari.trim());
            sqlSampai = java.sql.Date.valueOf(tglSampai.trim());
        } catch (IllegalArgumentException e) {
            System.err.println("Format tanggal salah: " + tglDari + " / " + tglSampai);
            return list;
        }

        String sql = "SELECT id_reservasi, nama_tamu, COALESCE(no_identitas, '-') AS no_identitas, " +
                "COALESCE(no_kamar, '-') AS no_kamar, COALESCE(tipe_kamar, '-') AS tipe_kamar, " +
                "tgl_checkin, tgl_checkout_rencana, COALESCE(total_denda, 0) AS denda, " +
                "COALESCE(grand_total_pemasukan, 0) AS total_biaya, status_reservasi " +
                "FROM v_laporan_pemasukan " +
                "WHERE tgl_checkin BETWEEN ? AND ? " +
                "AND (nama_tamu ILIKE ? OR COALESCE(no_identitas, '') ILIKE ? " +
                "     OR COALESCE(no_kamar, '') ILIKE ? OR COALESCE(tipe_kamar, '') ILIKE ? " +
                "     OR status_reservasi ILIKE ? OR ('RSV-' || id_reservasi) ILIKE ?) " +
                "ORDER BY id_reservasi DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, sqlDari);
            stmt.setDate(2, sqlSampai);

            String kw = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            stmt.setString(3, kw);
            stmt.setString(4, kw);
            stmt.setString(5, kw);
            stmt.setString(6, kw);
            stmt.setString(7, kw);
            stmt.setString(8, kw);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new TransaksiReadOnlyItem(
                        rs.getInt("id_reservasi"),
                        rs.getString("nama_tamu"),
                        rs.getString("no_identitas"),
                        rs.getString("no_kamar"),
                        rs.getString("tipe_kamar"),
                        rs.getString("tgl_checkin"),
                        rs.getString("tgl_checkout_rencana"),
                        rs.getDouble("denda"),
                        rs.getDouble("total_biaya"),
                        rs.getString("status_reservasi")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}