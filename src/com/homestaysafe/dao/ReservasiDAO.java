package com.homestaysafe.dao;

import com.homestaysafe.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReservasiDAO {

    public static class RiwayatItem {
        private int idTransaksi;
        private String jenisPesanan;
        private String detail;
        private double total;
        private String status;

        public RiwayatItem(int idTransaksi, String jenisPesanan, String detail, double total, String status) {
            this.idTransaksi = idTransaksi;
            this.jenisPesanan = jenisPesanan;
            this.detail = detail;
            this.total = total;
            this.status = status;
        }

        public int getIdTransaksi() { return idTransaksi; }
        public String getJenisPesanan() { return jenisPesanan; }
        public String getDetail() { return detail; }
        public double getTotal() { return total; }
        public String getStatus() { return status; }
    }

    public List<RiwayatItem> getRiwayatByUsername(String username) {
        List<RiwayatItem> list = new ArrayList<>();

        String sqlReservasi = "SELECT v.id_reservasi, COALESCE(v.no_kamar, '-') AS no_kamar, " +
                "COALESCE(v.tipe_kamar, '-') AS tipe_kamar, v.tgl_checkin, v.tgl_checkout_rencana, " +
                "v.total_biaya_kamar, v.status_reservasi " +
                "FROM v_laporan_pemasukan v " +
                "JOIN reservasi r ON v.id_reservasi = r.id_reservasi " +
                "JOIN users u ON r.id_user = u.id_user " +
                "WHERE LOWER(u.username) = LOWER(?) " +
                "ORDER BY v.id_reservasi DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlReservasi)) {

            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String noKamar = rs.getString("no_kamar");
                String tipeKamar = rs.getString("tipe_kamar");
                String infoKamar = (noKamar == null || noKamar.equals("-") || noKamar.trim().isEmpty())
                        ? "Tipe " + tipeKamar
                        : "Kamar No. " + noKamar + " (" + tipeKamar + ")";

                String detailKamar = infoKamar + " | " +
                        rs.getString("tgl_checkin") + " s/d " + rs.getString("tgl_checkout_rencana");

                list.add(new RiwayatItem(
                        rs.getInt("id_reservasi"),
                        "Reservasi Kamar",
                        detailKamar,
                        rs.getDouble("total_biaya_kamar"),
                        rs.getString("status_reservasi")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sqlFB = "SELECT pm.id_pesanan, m.nama_menu, pm.jumlah, pm.subtotal AS total_harga, pm.status_pesanan " +
                "FROM pesanan_makanan pm " +
                "JOIN menu m ON pm.id_menu = m.id_menu " +
                "JOIN reservasi r ON pm.id_reservasi = r.id_reservasi " +
                "JOIN users u ON r.id_user = u.id_user " +
                "WHERE LOWER(u.username) = LOWER(?) " +
                "ORDER BY pm.id_pesanan DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtFB = conn.prepareStatement(sqlFB)) {

            stmtFB.setString(1, username.trim());
            ResultSet rsFB = stmtFB.executeQuery();

            while (rsFB.next()) {
                String detailMenu = rsFB.getString("nama_menu") + " (x" + rsFB.getInt("jumlah") + ")";
                list.add(new RiwayatItem(
                        rsFB.getInt("id_pesanan"),
                        "Food & Beverage",
                        detailMenu,
                        rsFB.getDouble("total_harga"),
                        rsFB.getString("status_pesanan")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean batalkanReservasi(int idReservasi) {
        String sql = "UPDATE reservasi SET status_reservasi = 'Dibatalkan' WHERE id_reservasi = ? AND status_reservasi ILIKE 'Pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idReservasi);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}