package com.homestaysafe.dao;

import com.homestaysafe.exception.JadwalBentrokException;
import com.homestaysafe.model.Kamar;
import com.homestaysafe.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KamarDAO {

    public static class TipeKamarSummary {
        private final int idTipeKamar;
        private final String tipeKamar;
        private final double hargaPerMalam;
        private final String deskripsi;
        private final int totalUnit;
        private final int unitTersedia;

        public TipeKamarSummary(int idTipeKamar, String tipeKamar, double hargaPerMalam,
                                String deskripsi, int totalUnit, int unitTersedia) {
            this.idTipeKamar = idTipeKamar;
            this.tipeKamar = tipeKamar;
            this.hargaPerMalam = hargaPerMalam;
            this.deskripsi = deskripsi;
            this.totalUnit = totalUnit;
            this.unitTersedia = unitTersedia;
        }

        public int getIdTipeKamar() { return idTipeKamar; }
        public String getTipeKamar() { return tipeKamar; }
        public double getHargaPerMalam() { return hargaPerMalam; }
        public String getDeskripsi() { return deskripsi; }
        public int getTotalUnit() { return totalUnit; }
        public int getUnitTersedia() { return unitTersedia; }
    }

    public List<Kamar> getAllKamar() {
        List<Kamar> listKamar = new ArrayList<>();

        String sqlUpdateReservasiSelesai = "UPDATE reservasi SET status_reservasi = 'Selesai' " +
                "WHERE tgl_checkout_rencana <= CURRENT_DATE " +
                "AND COALESCE(status_reservasi, '') ILIKE '%Check-in%'";

        String sqlSelect = "SELECT k.id_kamar, k.id_tipe_kamar, k.no_kamar, tk.nama_tipe, " +
                "tk.harga_per_malam, k.status_kamar, tk.deskripsi " +
                "FROM kamar k " +
                "JOIN tipe_kamar tk ON k.id_tipe_kamar = tk.id_tipe_kamar " +
                "ORDER BY k.id_kamar ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            // Trigger trg_sync_status_kamar di DB otomatis mengubah status kamar menjadi Tersedia
            stmt.executeUpdate(sqlUpdateReservasiSelesai);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlSelect)) {

            while (rs.next()) {
                listKamar.add(Kamar.buat(
                        rs.getInt("id_kamar"),
                        rs.getInt("id_tipe_kamar"),
                        rs.getString("no_kamar"),
                        rs.getString("nama_tipe"),
                        rs.getDouble("harga_per_malam"),
                        rs.getString("status_kamar"),
                        rs.getString("deskripsi")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listKamar;
    }

    /** Ringkasan per Tipe Kamar (Standard, Deluxe, Executive, Junior Suite, Presidential Suite) */
    public List<TipeKamarSummary> getRingkasanTipeKamar() {
        getAllKamar();
        List<TipeKamarSummary> list = new ArrayList<>();

        String sql = "SELECT tk.id_tipe_kamar, tk.nama_tipe, tk.harga_per_malam, COALESCE(tk.deskripsi, '') AS deskripsi, " +
                "COUNT(k.id_kamar) AS total_unit, " +
                "COUNT(k.id_kamar) FILTER (WHERE TRIM(k.status_kamar) ILIKE 'Tersedia') AS unit_tersedia " +
                "FROM tipe_kamar tk " +
                "LEFT JOIN kamar k ON tk.id_tipe_kamar = k.id_tipe_kamar " +
                "GROUP BY tk.id_tipe_kamar, tk.nama_tipe, tk.harga_per_malam, tk.deskripsi " +
                "ORDER BY tk.harga_per_malam ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new TipeKamarSummary(
                        rs.getInt("id_tipe_kamar"),
                        rs.getString("nama_tipe"),
                        rs.getDouble("harga_per_malam"),
                        rs.getString("deskripsi"),
                        rs.getInt("total_unit"),
                        rs.getInt("unit_tersedia")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Mengecek ketersediaan unit pada tipe kamar tertentu untuk rentang tanggal */
    public boolean isTipeKamarTersediaUntukTanggal(String tipeKamar, String checkIn, String checkOut) {
        String sqlTotalAktif = "SELECT COUNT(*) FROM kamar k " +
                "JOIN tipe_kamar tk ON k.id_tipe_kamar = tk.id_tipe_kamar " +
                "WHERE TRIM(tk.nama_tipe) ILIKE TRIM(?) " +
                "AND COALESCE(k.status_kamar, '') NOT ILIKE 'Pemeliharaan'";

        String sqlTerpakaiTanggal = "SELECT COUNT(*) FROM reservasi r " +
                "JOIN tipe_kamar tk ON r.id_tipe_kamar = tk.id_tipe_kamar " +
                "WHERE TRIM(tk.nama_tipe) ILIKE TRIM(?) " +
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

    /** Menyimpan booking online berdasarkan Tipe Kamar (id_kamar = NULL, dipilih saat Check-In) */
    public int simpanReservasiByTipe(String username, String tipeKamar, String checkIn, String checkOut, double totalHarga)
            throws JadwalBentrokException, SQLException {

        if (!isTipeKamarTersediaUntukTanggal(tipeKamar, checkIn, checkOut)) {
            throw new JadwalBentrokException("Tipe " + tipeKamar, checkIn, checkOut);
        }

        String sqlUser = "SELECT id_user FROM users WHERE LOWER(username) = LOWER(?) OR LOWER(nama) = LOWER(?)";
        String sqlTipe = "SELECT id_tipe_kamar, harga_per_malam FROM tipe_kamar WHERE LOWER(TRIM(nama_tipe)) = LOWER(TRIM(?))";
        String sqlReservasi = "INSERT INTO reservasi (id_user, id_tipe_kamar, id_kamar, sumber_booking, tgl_checkin, tgl_checkout_rencana, harga_kamar_deal, status_reservasi) " +
                "VALUES (?, ?, NULL, 'Online', ?, ?, ?, 'Pending') RETURNING id_reservasi";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtUser = conn.prepareStatement(sqlUser);
             PreparedStatement stmtTipe = conn.prepareStatement(sqlTipe)) {

            stmtUser.setString(1, username.trim());
            stmtUser.setString(2, username.trim());
            ResultSet rsUser = stmtUser.executeQuery();
            if (!rsUser.next()) {
                throw new SQLException("Username '" + username + "' tidak ditemukan di tabel users.");
            }
            int idUser = rsUser.getInt("id_user");

            stmtTipe.setString(1, tipeKamar);
            ResultSet rsTipe = stmtTipe.executeQuery();
            if (!rsTipe.next()) {
                throw new SQLException("Tipe kamar '" + tipeKamar + "' tidak ditemukan.");
            }
            int idTipeKamar = rsTipe.getInt("id_tipe_kamar");
            double hargaDeal = rsTipe.getDouble("harga_per_malam");

            conn.setAutoCommit(false);
            try (PreparedStatement stmtRes = conn.prepareStatement(sqlReservasi)) {
                stmtRes.setInt(1, idUser);
                stmtRes.setInt(2, idTipeKamar);
                stmtRes.setDate(3, java.sql.Date.valueOf(checkIn));
                stmtRes.setDate(4, java.sql.Date.valueOf(checkOut));
                stmtRes.setDouble(5, hargaDeal);

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
        String sqlStatus = "SELECT status_kamar FROM kamar WHERE id_kamar = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlStatus)) {
            stmt.setInt(1, idKamar);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status_kamar");
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
                "WHERE r.id_kamar = ? " +
                "AND COALESCE(r.status_reservasi, '') NOT ILIKE '%Selesai%' " +
                "AND COALESCE(r.status_reservasi, '') NOT ILIKE '%Ditolak%' " +
                "AND COALESCE(r.status_reservasi, '') NOT ILIKE '%Dibatalkan%' " +
                "AND r.tgl_checkin < ?::date " +
                "AND r.tgl_checkout_rencana > ?::date";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlOverlap)) {
            stmt.setInt(1, idKamar);
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
        String sqlKamarInfo = "SELECT k.id_tipe_kamar, tk.harga_per_malam FROM kamar k JOIN tipe_kamar tk ON k.id_tipe_kamar = tk.id_tipe_kamar WHERE k.id_kamar = ?";
        String sqlReservasi = "INSERT INTO reservasi (id_user, id_tipe_kamar, id_kamar, sumber_booking, tgl_checkin, tgl_checkout_rencana, harga_kamar_deal, status_reservasi) " +
                "VALUES (?, ?, ?, 'Online', ?, ?, ?, 'Pending') RETURNING id_reservasi";

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
            int idTipeKamar = rsKamar.getInt("id_tipe_kamar");
            double hargaDeal = rsKamar.getDouble("harga_per_malam");

            conn.setAutoCommit(false);
            try (PreparedStatement stmtRes = conn.prepareStatement(sqlReservasi)) {
                stmtRes.setInt(1, idUser);
                stmtRes.setInt(2, idTipeKamar);
                stmtRes.setInt(3, idKamar);
                stmtRes.setDate(4, java.sql.Date.valueOf(checkIn));
                stmtRes.setDate(5, java.sql.Date.valueOf(checkOut));
                stmtRes.setDouble(6, hargaDeal);

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
}z