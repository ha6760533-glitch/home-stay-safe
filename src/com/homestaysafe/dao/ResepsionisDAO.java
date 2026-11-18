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
                "COALESCE(r.no_kamar, '-') AS no_kamar, COALESCE(r.tipe_kamar, '-') AS tipe_kamar, " +
                "r.tgl_checkin, r.tgl_checkout_rencana, r.total_biaya, r.status_reservasi " +
                "FROM reservasi r " +
                "JOIN users u ON r.id_user = u.id_user " +
                "WHERE u.username ILIKE ? OR u.nama ILIKE ? OR COALESCE(u.no_identitas, '') ILIKE ? OR COALESCE(r.no_kamar, '') ILIKE ? " +
                "ORDER BY r.id_reservasi DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String kw = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            stmt.setString(1, kw);
            stmt.setString(2, kw);
            stmt.setString(3, kw);
            stmt.setString(4, kw);

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
                        rs.getDouble("total_biaya"),
                        rs.getString("status_reservasi")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * @return {username, password} tamu, atau null bila penyimpanan gagal karena error database
     * @throws JadwalBentrokException jika kamar sudah dipesan pada rentang tanggal tersebut
     */
    public String[] simpanPemesananOffline(String namaTamu, String noIdentitas, int idKamar, String checkIn, String checkOut, double totalHarga)
            throws JadwalBentrokException {
        KamarDAO kamarDAO = new KamarDAO();
        if (!kamarDAO.isKamarTersediaUntukTanggal(idKamar, checkIn, checkOut)) {
            throw new JadwalBentrokException(kamarDAO.getNoKamarById(idKamar), checkIn, checkOut);
        }

        String sqlCheckUser = "SELECT id_user, username FROM users WHERE no_identitas = ?";
        String sqlCheckUsernameExist = "SELECT 1 FROM users WHERE LOWER(username) = LOWER(?)";
        String sqlInsertUser = "INSERT INTO users (nama, username, password_hash, role, no_identitas) VALUES (?, ?, ?, 'Tamu', ?) RETURNING id_user";
        String sqlKamarInfo = "SELECT no_kamar, tipe_kamar FROM kamar WHERE id_kamar = ?";
        String sqlReservasi = "INSERT INTO reservasi (id_user, no_kamar, tipe_kamar, tgl_checkin, tgl_checkout_rencana, total_biaya, denda, status_reservasi) " +
                "VALUES (?, ?, ?, ?, ?, ?, 0, 'Check-in')";
        String sqlUpdateKamar = "UPDATE kamar SET status_kamar = 'Terisi' WHERE id_kamar = ?";

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
                        psUser.setString(3, PasswordUtil.hash(passwordTamu)); // simpan hash; password asli hanya ditampilkan ke resepsionis
                        psUser.setString(4, noIdentitas.trim());
                        ResultSet rsNew = psUser.executeQuery();
                        if (rsNew.next()) {
                            idUser = rsNew.getInt(1);
                        }
                    }
                }

                String noKamar = "";
                String tipeKamar = "";
                try (PreparedStatement psInfo = conn.prepareStatement(sqlKamarInfo)) {
                    psInfo.setInt(1, idKamar);
                    ResultSet rsK = psInfo.executeQuery();
                    if (rsK.next()) {
                        noKamar = rsK.getString("no_kamar");
                        tipeKamar = rsK.getString("tipe_kamar");
                    }
                }

                try (PreparedStatement psRes = conn.prepareStatement(sqlReservasi);
                     PreparedStatement psKam = conn.prepareStatement(sqlUpdateKamar)) {
                    psRes.setInt(1, idUser);
                    psRes.setString(2, noKamar);
                    psRes.setString(3, tipeKamar);
                    psRes.setDate(4, java.sql.Date.valueOf(checkIn));
                    psRes.setDate(5, java.sql.Date.valueOf(checkOut));
                    psRes.setDouble(6, totalHarga);
                    psRes.executeUpdate();

                    psKam.setInt(1, idKamar);
                    psKam.executeUpdate();
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
        String sqlKamarTerisi = "UPDATE kamar SET status_kamar = 'Terisi' WHERE TRIM(no_kamar) = (SELECT TRIM(no_kamar) FROM reservasi WHERE id_reservasi = ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtRes = conn.prepareStatement(sqlRes)) {
                stmtRes.setString(1, statusBaru);
                stmtRes.setInt(2, idReservasi);
                int updated = stmtRes.executeUpdate();

                if (statusBaru.toLowerCase().contains("check-in")) {
                    try (PreparedStatement stmtKam = conn.prepareStatement(sqlKamarTerisi)) {
                        stmtKam.setInt(1, idReservasi);
                        stmtKam.executeUpdate();
                    }
                }

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
        String sql = "SELECT COALESCE(SUM(subtotal), 0) AS total_fb FROM pesanan_makanan WHERE id_reservasi = ?";
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
                "WHERE pm.id_reservasi = ? " +
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

    public boolean prosesCheckOut(int idReservasi, double grandTotal) {
        String sqlRes = "UPDATE reservasi SET status_reservasi = 'Selesai', total_biaya = ?, tgl_checkout_aktual = CURRENT_TIMESTAMP WHERE id_reservasi = ?";
        String sqlKamar = "UPDATE kamar SET status_kamar = 'Pemeliharaan' WHERE TRIM(no_kamar) = (SELECT TRIM(no_kamar) FROM reservasi WHERE id_reservasi = ?)";
        String sqlFB = "UPDATE pesanan_makanan SET status_pesanan = 'Selesai' WHERE id_reservasi = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psRes = conn.prepareStatement(sqlRes);
                 PreparedStatement psKam = conn.prepareStatement(sqlKamar);
                 PreparedStatement psFB = conn.prepareStatement(sqlFB)) {

                psRes.setDouble(1, grandTotal);
                psRes.setInt(2, idReservasi);
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
