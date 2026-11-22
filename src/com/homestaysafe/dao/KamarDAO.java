package com.homestaysafe.dao;

import com.homestaysafe.exception.JadwalBentrokException;
import com.homestaysafe.model.Kamar;
import com.homestaysafe.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KamarDAO {

    public static class TipeKamarSummary {
        private final String tipeKamar;
        private final double hargaPerMalam;
        private final int totalUnit;
        private final int unitTersedia;

        public TipeKamarSummary(String tipeKamar, double hargaPerMalam, int totalUnit, int unitTersedia) {
            this.tipeKamar = tipeKamar;
            this.hargaPerMalam = hargaPerMalam;
            this.totalUnit = totalUnit;
            this.unitTersedia = unitTersedia;
        }

        public String getTipeKamar() { return tipeKamar; }
        public double getHargaPerMalam() { return hargaPerMalam; }
        public int getTotalUnit() { return totalUnit; }
        public int getUnitTersedia() { return unitTersedia; }
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
                listKamar.add(Kamar.buat(
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

    /** Ringkasan per Tipe Kamar (Standard, Deluxe, Suite) untuk Booking Online Tamu */
    public List<TipeKamarSummary> getRingkasanTipeKamar() {
        getAllKamar(); // sinkronisasi status kamar terbaru terlebih dahulu
        List<TipeKamarSummary> list = new ArrayList<>();

        String sql = "SELECT tipe_kamar, MAX(harga_per_malam) AS harga, " +
                "COUNT(*) AS total_unit, " +
                "COUNT(*) FILTER (WHERE TRIM(status_kamar) ILIKE 'Tersedia') AS unit_tersedia " +
                "FROM kamar " +
                "GROUP BY tipe_kamar " +
                "ORDER BY harga ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new TipeKamarSummary(
                        rs.getString("tipe_kamar"),
                        rs.getDouble("harga"),
                        rs.getInt("total_unit"),
                        rs.getInt("unit_tersedia")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Mengecek apakah masih ada minimal 1 kamar dengan tipe tersebut yang bebas bentrok pada tanggal tertentu */
    public boolean isTipeKamarTersediaUntukTanggal(String tipeKamar, String checkIn, String checkOut) {
        String sqlTotalAktif = "SELECT COUNT(*) FROM kamar " +
                "WHERE TRIM(tipe_kamar) ILIKE TRIM(?) " +
                "AND COALESCE(status_kamar, '') NOT ILIKE 'Pemeliharaan'";

        String sqlTerpakaiTanggal = "SELECT COUNT(*) FROM reservasi r " +
                "WHERE TRIM(r.tipe_kamar) ILIKE TRIM(?) " +
                "AND COALESCE(r.status_reservasi, '') NOT ILIKE '%Selesai%' " +
                "AND COALESCE(r.status_reservasi, '') NOT ILIKE '%Ditolak%' " +
                "AND COALESCE(r.status_reservasi, '') NOT ILIKE '%Dibatalkan%' " +
                "AND r.tgl_checkin < ?::date " +
                "AND r.tgl_checkout_rencana > ?::date";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtTotal = conn.prepareStatement(sqlTotalAktif);
             PreparedStatement stmtUsed = conn.prepareStatement(sqlTerpakaiTanggal)) {

            stmtTotal.setString(1, tipeKamar);
            ResultSet rsTotal = stmtTotal.executeQuery();
            int totalUnit = rsTotal.next() ? rsTotal.getInt(1) : 0;
            if (totalUnit <= 0) return false;

            stmtUsed.setString(1, tipeKamar);
            stmtUsed.setString(2, checkOut);
            stmtUsed.setString(3, checkIn);
            ResultSet rsUsed = stmtUsed.executeQuery();
            int usedUnit = rsUsed.next() ? rsUsed.getInt(1) : 0;

            return (totalUnit - usedUnit) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Menyimpan booking online berdasarkan Tipe Kamar (no_kamar = NULL, dipilih Resepsionis saat verifikasi) */
    public int simpanReservasiByTipe(String username, String tipeKamar, String checkIn, String checkOut, double totalHarga)
            throws JadwalBentrokException, SQLException {

        if (!isTipeKamarTersediaUntukTanggal(tipeKamar, checkIn, checkOut)) {
            throw new JadwalBentrokException("Tipe " + tipeKamar, checkIn, checkOut);
        }

        String sqlUser = "SELECT id_user FROM users WHERE LOWER(username) = LOWER(?) OR LOWER(nama) = LOWER(?)";
        String sqlReservasi = "INSERT INTO reservasi (id_user, no_kamar, tipe_kamar, tgl_checkin, tgl_checkout_rencana, total_biaya, denda, status_reservasi) " +
                "VALUES (?, NULL, ?, ?, ?, ?, 0, 'Pending') RETURNING id_reservasi";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtUser = conn.prepareStatement(sqlUser)) {

            stmtUser.setString(1, username.trim());
            stmtUser.setString(2, username.trim());
            ResultSet rsUser = stmtUser.executeQuery();

            if (!rsUser.next()) {
                throw new SQLException("Username '" + username + "' tidak ditemukan di tabel users.");
            }
            int idUser = rsUser.getInt("id_user");

            conn.setAutoCommit(false);
            try (PreparedStatement stmtRes = conn.prepareStatement(sqlReservasi)) {
                stmtRes.setInt(1, idUser);
                stmtRes.setString(2, tipeKamar);
                stmtRes.setDate(3, java.sql.Date.valueOf(checkIn));
                stmtRes.setDate(4, java.sql.Date.valueOf(checkOut));
                stmtRes.setDouble(5, totalHarga);

                ResultSet rs = stmtRes.executeQuery();
                if (rs.next()) {
                    int idReservasi = rs.getInt(1);
                    conn.commit();
                    return idReservasi;
                }
                conn.rollback();
                throw new SQLException("Gagal mendapatkan ID reservasi.");
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
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

    public int simpanReservasi(String username, int idKamar, String checkIn, String checkOut, double totalHarga)
            throws JadwalBentrokException, SQLException {

        if (!isKamarTersediaUntukTanggal(idKamar, checkIn, checkOut)) {
            throw new JadwalBentrokException(getNoKamarById(idKamar), checkIn, checkOut);
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
                throw new SQLException("Username '" + username + "' tidak ditemukan di tabel users.");
            }
            int idUser = rsUser.getInt("id_user");

            stmtInfo.setInt(1, idKamar);
            ResultSet rsKamar = stmtInfo.executeQuery();
            if (!rsKamar.next()) {
                throw new SQLException("Data kamar dengan ID " + idKamar + " tidak ditemukan.");
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
                    return idReservasi;
                }
                conn.rollback();
                throw new SQLException("Gagal mendapatkan ID reservasi.");
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public String getNoKamarById(int idKamar) {
        String sql = "SELECT no_kamar FROM kamar WHERE id_kamar = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idKamar);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("no_kamar");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ID " + idKamar;
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