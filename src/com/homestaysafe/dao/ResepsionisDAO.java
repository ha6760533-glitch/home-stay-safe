package com.homestaysafe.dao;

import com.homestaysafe.exception.JadwalBentrokException;
import com.homestaysafe.util.DatabaseConnection;
import com.homestaysafe.util.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResepsionisDAO {

    public static class ReservasiDetail {
        private int idReservasi;
        private String username;
        private String namaTamu;
        private String noIdentitas;
        private String noKamar;
        private String tipeKamar;
        private String tglCheckin;
        private String tglCheckout;
        private double totalHarga;
        private String statusReservasi;

        public ReservasiDetail(int idReservasi, String username, String namaTamu, String noIdentitas, String noKamar, String tipeKamar, String tglCheckin, String tglCheckout, double totalHarga, String statusReservasi) {
            this.idReservasi = idReservasi;
            this.username = username;
            this.namaTamu = namaTamu;
            this.noIdentitas = noIdentitas;
            this.noKamar = noKamar;
            this.tipeKamar = tipeKamar;
            this.tglCheckin = tglCheckin;
            this.tglCheckout = tglCheckout;
            this.totalHarga = totalHarga;
            this.statusReservasi = statusReservasi;
        }

        public int getIdReservasi() { return idReservasi; }
        public String getUsername() { return username; }
        public String getNamaTamu() { return namaTamu; }
        public String getNoIdentitas() { return noIdentitas; }
        public String getNoKamar() { return noKamar; }
        public String getTipeKamar() { return tipeKamar; }
        public String getTglCheckin() { return tglCheckin; }
        public String getTglCheckout() { return tglCheckout; }
        public double getTotalHarga() { return totalHarga; }
        public String getStatusReservasi() { return statusReservasi; }
    }

    public static class RincianFBItem {
        private String namaMenu;
        private int jumlah;
        private double subtotal;

        public RincianFBItem(String namaMenu, int jumlah, double subtotal) {
            this.namaMenu = namaMenu;
            this.jumlah = jumlah;
            this.subtotal = subtotal;
        }

        public String getNamaMenu() { return namaMenu; }
        public int getJumlah() { return jumlah; }
        public double getSubtotal() { return subtotal; }
    }

    public List<ReservasiDetail> getDaftarReservasi(String keyword) {
        List<ReservasiDetail> list = new ArrayList<>();
        String sql = "SELECT r.id_reservasi, u.username, u.nama, COALESCE(u.no_identitas, '-') AS no_identitas, " +
                "COALESCE(k.no_kamar, '-') AS no_kamar, tk.nama_tipe AS tipe_kamar, " +
                "r.tgl_checkin, r.tgl_checkout_rencana, " +
                "(GREATEST(1, r.tgl_checkout_rencana - r.tgl_checkin) * r.harga_kamar_deal) AS total_biaya_kamar, " +
                "r.status_reservasi " +
                "FROM reservasi r " +
                "JOIN users u ON r.id_user = u.id_user " +
                "JOIN tipe_kamar tk ON r.id_tipe_kamar = tk.id_tipe_kamar " +
                "LEFT JOIN kamar k ON r.id_kamar = k.id_kamar " +
                "WHERE ('RSV-' || CAST(r.id_reservasi AS TEXT)) ILIKE ? " +
                "OR CAST(r.id_reservasi AS TEXT) ILIKE ? " +
                "OR TRIM(u.username) ILIKE ? " +
                "OR TRIM(u.nama) ILIKE ? " +
                "OR COALESCE(TRIM(u.no_identitas), '') ILIKE ? " +
                "OR COALESCE(TRIM(k.no_kamar), '') ILIKE ? " +
                "OR TRIM(tk.nama_tipe) ILIKE ? " +
                "ORDER BY r.id_reservasi DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String cleanKw = (keyword == null ? "" : keyword.trim());
            String numericKw = cleanKw.replaceAll("(?i)^rsv-?", "").trim();
            if (numericKw.isEmpty()) numericKw = cleanKw;

            String kwFull = "%" + cleanKw + "%";
            String kwNum = "%" + numericKw + "%";

            stmt.setString(1, kwFull);
            stmt.setString(2, kwNum);
            stmt.setString(3, kwFull);
            stmt.setString(4, kwFull);
            stmt.setString(5, kwFull);
            stmt.setString(6, kwFull);
            stmt.setString(7, kwFull);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new ReservasiDetail(
                        rs.getInt("id_reservasi"),
                        rs.getString("username"),
                        rs.getString("nama"),
                        rs.getString("no_identitas"),
                        rs.getString("no_kamar"),
                        rs.getString("tipe_kamar"),
                        rs.getString("tgl_checkin"),
                        rs.getString("tgl_checkout_rencana"),
                        rs.getDouble("total_biaya_kamar"),
                        rs.getString("status_reservasi")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Mengambil nomor kamar yang tersedia & sesuai tipe kamar yang dipesan tamu */
    public List<String> getNomorKamarKosongByTipe(String tipeKamar, String checkIn, String checkOut) {
        List<String> listNoKamar = new ArrayList<>();
        String sql = "SELECT k.no_kamar FROM kamar k " +
                "JOIN tipe_kamar tk ON k.id_tipe_kamar = tk.id_tipe_kamar " +
                "WHERE TRIM(tk.nama_tipe) ILIKE TRIM(?) " +
                "AND TRIM(k.status_kamar) ILIKE 'Tersedia' " +
                "AND NOT EXISTS (" +
                "    SELECT 1 FROM reservasi r " +
                "    WHERE r.id_kamar = k.id_kamar " +
                "    AND COALESCE(r.status_reservasi, '') NOT ILIKE '%Selesai%' " +
                "    AND COALESCE(r.status_reservasi, '') NOT ILIKE '%Ditolak%' " +
                "    AND COALESCE(r.status_reservasi, '') NOT ILIKE '%Dibatalkan%' " +
                "    AND r.tgl_checkin < ?::date " +
                "    AND r.tgl_checkout_rencana > ?::date" +
                ") " +
                "ORDER BY k.no_kamar ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tipeKamar);
            stmt.setString(2, checkOut);
            stmt.setString(3, checkIn);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                listNoKamar.add(rs.getString("no_kamar"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listNoKamar;
    }

    /** Proses Check-In: menetapkan id_kamar pilihan Resepsionis & ubah status ke 'Check-in' */
    public boolean checkInDanTetapkanKamar(int idReservasi, String noKamarTerpilih) {
        String sqlGetKamar = "SELECT id_kamar FROM kamar WHERE TRIM(no_kamar) = TRIM(?)";
        String sqlRes = "UPDATE reservasi SET id_kamar = ?, status_reservasi = 'Check-in' WHERE id_reservasi = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psGet = conn.prepareStatement(sqlGetKamar)) {
            psGet.setString(1, noKamarTerpilih.trim());
            ResultSet rs = psGet.executeQuery();
            if (!rs.next()) return false;
            int idKamar = rs.getInt("id_kamar");

            conn.setAutoCommit(false);
            try (PreparedStatement psRes = conn.prepareStatement(sqlRes)) {
                psRes.setInt(1, idKamar);
                psRes.setInt(2, idReservasi);
                int updated = psRes.executeUpdate();
                conn.commit();
                return updated > 0;
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String[] simpanPemesananOffline(String namaTamu, String noIdentitas, int idKamar, String checkIn, String checkOut, double totalHarga)
            throws JadwalBentrokException {
        KamarDAO kamarDAO = new KamarDAO();
        if (!kamarDAO.isKamarTersediaUntukTanggal(idKamar, checkIn, checkOut)) {
            throw new JadwalBentrokException(kamarDAO.getNoKamarById(idKamar), checkIn, checkOut);
        }

        String sqlCheckUser = "SELECT id_user, username FROM users WHERE TRIM(no_identitas) = ?";
        String sqlCheckUsernameExist = "SELECT 1 FROM users WHERE LOWER(username) = LOWER(?)";
        String sqlInsertUser = "INSERT INTO users (nama, username, password_hash, role, no_identitas) VALUES (?, ?, ?, 'Tamu', ?) RETURNING id_user";
        String sqlKamarInfo = "SELECT k.id_tipe_kamar, tk.harga_per_malam FROM kamar k JOIN tipe_kamar tk ON k.id_tipe_kamar = tk.id_tipe_kamar WHERE k.id_kamar = ?";
        String sqlReservasi = "INSERT INTO reservasi (id_user, id_tipe_kamar, id_kamar, sumber_booking, tgl_checkin, tgl_checkout_rencana, harga_kamar_deal, status_reservasi) " +
                "VALUES (?, ?, ?, 'Walk-in', ?, ?, ?, 'Check-in')";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idUser = -1;
                String usernameTamu = "";
                String passwordTamu = "";

                try (PreparedStatement psCheck = conn.prepareStatement(sqlCheckUser)) {
                    psCheck.setString(1, noIdentitas.trim());
                    ResultSet rs = psCheck.executeQuery();
                    if (rs.next()) {
                        idUser = rs.getInt("id_user");
                        usernameTamu = rs.getString("username");
                        passwordTamu = "(Password akun terdaftar sebelumnya)";
                    }
                }

                if (idUser == -1) {
                    String namaBersih = namaTamu.trim().replaceAll("[^a-zA-Z0-9\\s]", "");
                    String kataPertama = namaBersih.isEmpty() ? "tamu" : namaBersih.split("\\s+")[0].toLowerCase();

                    usernameTamu = kataPertama;
                    passwordTamu = kataPertama + "123";

                    int counter = 2;
                    try (PreparedStatement psExist = conn.prepareStatement(sqlCheckUsernameExist)) {
                        while (true) {
                            psExist.setString(1, usernameTamu);
                            try (ResultSet rsEx = psExist.executeQuery()) {
                                if (!rsEx.next()) break;
                            }
                            usernameTamu = kataPertama + counter;
                            counter++;
                        }
                    }

                    try (PreparedStatement psUser = conn.prepareStatement(sqlInsertUser)) {
                        psUser.setString(1, namaTamu.trim());
                        psUser.setString(2, usernameTamu);
                        psUser.setString(3, PasswordUtil.hash(passwordTamu));
                        psUser.setString(4, noIdentitas.trim());
                        ResultSet rsNew = psUser.executeQuery();
                        if (rsNew.next()) {
                            idUser = rsNew.getInt(1);
                        }
                    }
                }

                int idTipeKamar = 1;
                double hargaDeal = 0;
                try (PreparedStatement psInfo = conn.prepareStatement(sqlKamarInfo)) {
                    psInfo.setInt(1, idKamar);
                    ResultSet rsK = psInfo.executeQuery();
                    if (rsK.next()) {
                        idTipeKamar = rsK.getInt("id_tipe_kamar");
                        hargaDeal = rsK.getDouble("harga_per_malam");
                    }
                }

                try (PreparedStatement psRes = conn.prepareStatement(sqlReservasi)) {
                    psRes.setInt(1, idUser);
                    psRes.setInt(2, idTipeKamar);
                    psRes.setInt(3, idKamar);
                    psRes.setDate(4, java.sql.Date.valueOf(checkIn));
                    psRes.setDate(5, java.sql.Date.valueOf(checkOut));
                    psRes.setDouble(6, hargaDeal);
                    psRes.executeUpdate();
                }

                conn.commit();
                return new String[]{usernameTamu, passwordTamu};
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStatusReservasi(int idReservasi, String statusBaru) {
        String sqlRes = "UPDATE reservasi SET status_reservasi = ? WHERE id_reservasi = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtRes = conn.prepareStatement(sqlRes)) {
                stmtRes.setString(1, statusBaru);
                stmtRes.setInt(2, idReservasi);
                int updated = stmtRes.executeUpdate();
                conn.commit();
                return updated > 0;
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public double getTagihanMakanan(int idReservasi) {
        String sql = "SELECT COALESCE(SUM(subtotal), 0) AS total_fb FROM pesanan_makanan WHERE id_reservasi = ? AND status_pesanan <> 'Dibatalkan'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idReservasi);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_fb");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<RincianFBItem> getRincianMakanan(int idReservasi) {
        List<RincianFBItem> list = new ArrayList<>();
        String sql = "SELECT m.nama_menu, pm.jumlah, pm.subtotal " +
                "FROM pesanan_makanan pm " +
                "JOIN menu m ON pm.id_menu = m.id_menu " +
                "WHERE pm.id_reservasi = ? AND pm.status_pesanan <> 'Dibatalkan' " +
                "ORDER BY pm.id_pesanan ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idReservasi);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new RincianFBItem(
                        rs.getString("nama_menu"),
                        rs.getInt("jumlah"),
                        rs.getDouble("subtotal")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Simpan denda keterlambatan/kerusakan ke tabel denda_reservasi sesuai skema baru */
    public void simpanDenda(int idReservasi, double dendaTelat, double dendaKerusakan) {
        String sql = "INSERT INTO denda_reservasi (id_reservasi, jenis_denda, keterangan, nominal) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (dendaTelat > 0) {
                ps.setInt(1, idReservasi);
                ps.setString(2, "Keterlambatan");
                ps.setString(3, "Denda keterlambatan check-out");
                ps.setDouble(4, dendaTelat);
                ps.executeUpdate();
            }
            if (dendaKerusakan > 0) {
                ps.setInt(1, idReservasi);
                ps.setString(2, "Kerusakan");
                ps.setString(3, "Denda kerusakan fasilitas kamar");
                ps.setDouble(4, dendaKerusakan);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean prosesCheckOut(int idReservasi, double grandTotal) {
        String sqlRes = "UPDATE reservasi SET status_reservasi = 'Selesai', tgl_checkout_aktual = CURRENT_TIMESTAMP WHERE id_reservasi = ?";
        String sqlKamar = "UPDATE kamar SET status_kamar = 'Pemeliharaan' WHERE id_kamar = (SELECT id_kamar FROM reservasi WHERE id_reservasi = ?)";
        String sqlFB = "UPDATE pesanan_makanan SET status_pesanan = 'Selesai' WHERE id_reservasi = ? AND status_pesanan <> 'Dibatalkan'";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psRes = conn.prepareStatement(sqlRes);
                 PreparedStatement psKam = conn.prepareStatement(sqlKamar);
                 PreparedStatement psFB = conn.prepareStatement(sqlFB)) {

                psRes.setInt(1, idReservasi);
                psRes.executeUpdate();

                psKam.setInt(1, idReservasi);
                psKam.executeUpdate();

                psFB.setInt(1, idReservasi);
                psFB.executeUpdate();

                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}