package com.homestaysafe.dao;

import com.homestaysafe.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class KamarDAO {

    public static class Kamar {
        private int idKamar;
        private String noKamar;
        private String tipeKamar;
        private double hargaPerMalam;
        private String statusKamar;

        public Kamar(int idKamar, String noKamar, String tipeKamar, double hargaPerMalam, String statusKamar) {
            this.idKamar = idKamar;
            this.noKamar = noKamar;
            this.tipeKamar = tipeKamar;
            this.hargaPerMalam = hargaPerMalam;
            this.statusKamar = statusKamar;
        }

        public int getIdKamar() { return idKamar; }
        public String getNoKamar() { return noKamar; }
        public String getTipeKamar() { return tipeKamar; }
        public double getHargaPerMalam() { return hargaPerMalam; }
        public String getStatusKamar() { return statusKamar; }
    }

    public List<Kamar> getAllKamar() {
        List<Kamar> listKamar = new ArrayList<>();

        String sqlUpdateReservasiSelesai = "UPDATE reservasi SET status_reservasi = 'Selesai' " +
                "WHERE tgl_checkout_rencana <= CURRENT_DATE " +
                "AND COALESCE(status_reservasi, '') ILIKE '%Check-in%'";

        String sqlAutoUpdate = "UPDATE kamar k SET status_kamar = 'Tersedia' " +
                "WHERE TRIM(k.status_kamar) ILIKE 'Terisi' " +
                "AND NOT EXISTS (" +
                "    SELECT 1 FROM reservasi r " +
                "    WHERE TRIM(r.no_kamar) = TRIM(k.no_kamar) " +
                "    AND r.tgl_checkout_rencana > CURRENT_DATE " +
                "    AND COALESCE(r.status_reservasi, '') NOT ILIKE '%Selesai%' " +
                "    AND COALESCE(r.status_reservasi, '') NOT ILIKE '%Ditolak%' " +
                "    AND COALESCE(r.status_reservasi, '') NOT ILIKE '%Dibatalkan%'" +
                ")";

        String sqlSelect = "SELECT id_kamar, no_kamar, tipe_kamar, harga_per_malam, status_kamar FROM kamar ORDER BY id_kamar ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlUpdateReservasiSelesai);
            stmt.executeUpdate(sqlAutoUpdate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlSelect)) {

            while (rs.next()) {
                listKamar.add(new Kamar(
                        rs.getInt("id_kamar"),
                        rs.getString("no_kamar"),
                        rs.getString("tipe_kamar"),
                        rs.getDouble("harga_per_malam"),
                        rs.getString("status_kamar")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listKamar;
    }

    public boolean isKamarTersediaUntukTanggal(int idKamar, String checkIn, String checkOut) {

        String sqlStatus = "SELECT status_kamar, no_kamar FROM kamar WHERE id_kamar = ?";
        String noKamar = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlStatus)) {
            stmt.setInt(1, idKamar);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status_kamar");
                noKamar = rs.getString("no_kamar");
                if (status != null && status.equalsIgnoreCase("Pemeliharaan")) {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        String sqlOverlap = "SELECT COUNT(*) AS jumlah " +
                "FROM reservasi r " +
                "WHERE TRIM(r.no_kamar) = TRIM(?) " +
                "AND COALESCE(r.status_reservasi, '') NOT ILIKE '%Selesai%' " +
                "AND COALESCE(r.status_reservasi, '') NOT ILIKE '%Ditolak%' " +
                "AND COALESCE(r.status_reservasi, '') NOT ILIKE '%Dibatalkan%' " +
                "AND r.tgl_checkin < ?::date " +
                "AND r.tgl_checkout_rencana > ?::date";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlOverlap)) {
            stmt.setString(1, noKamar);
            stmt.setString(2, checkOut);
            stmt.setString(3, checkIn);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("jumlah") == 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String simpanReservasi(String username, int idKamar, String checkIn, String checkOut, double totalHarga) {

        if (!isKamarTersediaUntukTanggal(idKamar, checkIn, checkOut)) {
            return "BENTROK";
        }

        String sqlUser = "SELECT id_user FROM users WHERE LOWER(username) = LOWER(?) OR LOWER(nama) = LOWER(?)";
        String sqlKamarInfo = "SELECT no_kamar, tipe_kamar FROM kamar WHERE id_kamar = ?";

        String sqlReservasi = "INSERT INTO reservasi (id_user, no_kamar, tipe_kamar, tgl_checkin, tgl_checkout_rencana, total_biaya, denda, status_reservasi) " +
                "VALUES (?, ?, ?, ?, ?, ?, 0, 'Pending') RETURNING id_reservasi";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtUser = conn.prepareStatement(sqlUser);
             PreparedStatement stmtInfo = conn.prepareStatement(sqlKamarInfo)) {

            stmtUser.setString(1, username.trim());
            stmtUser.setString(2, username.trim());
            ResultSet rsUser = stmtUser.executeQuery();

            if (!rsUser.next()) {
                return "Username '" + username + "' tidak ditemukan di tabel users.";
            }
            int idUser = rsUser.getInt("id_user");

            stmtInfo.setInt(1, idKamar);
            ResultSet rsKamar = stmtInfo.executeQuery();
            if (!rsKamar.next()) {
                return "Data kamar dengan ID " + idKamar + " tidak ditemukan.";
            }
            String noKamar = rsKamar.getString("no_kamar");
            String tipeKamar = rsKamar.getString("tipe_kamar");

            conn.setAutoCommit(false);

            try (PreparedStatement stmtRes = conn.prepareStatement(sqlReservasi)) {
                stmtRes.setInt(1, idUser);
                stmtRes.setString(2, noKamar);
                stmtRes.setString(3, tipeKamar);
                stmtRes.setDate(4, java.sql.Date.valueOf(checkIn));
                stmtRes.setDate(5, java.sql.Date.valueOf(checkOut));
                stmtRes.setDouble(6, totalHarga);

                ResultSet rs = stmtRes.executeQuery();
                if (rs.next()) {
                    int idReservasi = rs.getInt(1);
                    conn.commit();
                    return "OK|" + idReservasi;   // kirim ID ke UI untuk dijadikan kode booking
                } else {
                    conn.rollback();
                    return "Gagal mendapatkan ID reservasi.";
                }
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
                return ex.getMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public boolean updateStatusKamar(int idKamar, String statusBaru) {
        String sql = "UPDATE kamar SET status_kamar = ? WHERE id_kamar = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statusBaru);
            stmt.setInt(2, idKamar);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}