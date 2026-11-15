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

        String sqlReservasi = "SELECT r.id_reservasi, COALESCE(r.no_kamar, '-') AS no_kamar, " +
                "COALESCE(r.tipe_kamar, '-') AS tipe_kamar, r.tgl_checkin, r.tgl_checkout_rencana, " +
                "r.total_biaya, r.status_reservasi " +
                "FROM reservasi r " +
                "JOIN users u ON r.id_user = u.id_user " +
                "WHERE LOWER(u.username) = LOWER(?) " +
                "ORDER BY r.id_reservasi DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlReservasi)) {

            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String detailKamar = "Kamar No. " + rs.getString("no_kamar") +
                        " (" + rs.getString("tipe_kamar") + ") | " +
                        rs.getString("tgl_checkin") + " s/d " + rs.getString("tgl_checkout_rencana");

                list.add(new RiwayatItem(
                        rs.getInt("id_reservasi"),
                        "Reservasi Kamar",
                        detailKamar,
                        rs.getDouble("total_biaya"),
                        rs.getString("status_reservasi")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sqlFB = "SELECT p.id_pesanan, p.nama_menu, p.jumlah, p.total_harga, p.status_pesanan " +
                "FROM pesanan_fb p " +
                "JOIN reservasi r ON p.id_reservasi = r.id_reservasi " +
                "JOIN users u ON r.id_user = u.id_user " +
                "WHERE LOWER(u.username) = LOWER(?) " +
                "ORDER BY p.id_pesanan DESC";

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
        } catch (Exception ignored) {
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