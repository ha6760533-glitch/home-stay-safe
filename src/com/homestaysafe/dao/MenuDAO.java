package com.homestaysafe.dao;

import com.homestaysafe.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    public static class Menu {
        private int idMenu;
        private String namaMenu;
        private double harga;

        public Menu(int idMenu, String namaMenu, double harga) {
            this.idMenu = idMenu;
            this.namaMenu = namaMenu;
            this.harga = harga;
        }

        public int getIdMenu() { return idMenu; }
        public String getNamaMenu() { return namaMenu; }
        public double getHarga() { return harga; }
    }

    public static class PesananFBDetail {
        private int idPesanan;
        private int idReservasi;
        private String noKamar;
        private String namaMenu;
        private int jumlah;
        private double subtotal;
        private String statusPesanan;

        public PesananFBDetail(int idPesanan, int idReservasi, String noKamar, String namaMenu, int jumlah, double subtotal, String statusPesanan) {
            this.idPesanan = idPesanan;
            this.idReservasi = idReservasi;
            this.noKamar = noKamar;
            this.namaMenu = namaMenu;
            this.jumlah = jumlah;
            this.subtotal = subtotal;
            this.statusPesanan = statusPesanan;
        }

        public int getIdPesanan() { return idPesanan; }
        public int getIdReservasi() { return idReservasi; }
        public String getNoKamar() { return noKamar; }
        public String getNamaMenu() { return namaMenu; }
        public int getJumlah() { return jumlah; }
        public double getSubtotal() { return subtotal; }
        public String getStatusPesanan() { return statusPesanan; }
    }

    // ===================== READ =====================
    public List<Menu> getAllMenu() {
        List<Menu> listMenu = new ArrayList<>();
        String sql = "SELECT id_menu, nama_menu, harga FROM menu ORDER BY id_menu ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                listMenu.add(new Menu(
                        rs.getInt("id_menu"),
                        rs.getString("nama_menu"),
                        rs.getDouble("harga")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listMenu;
    }

    // ===================== CREATE =====================
    public boolean tambahMenu(String namaMenu, double harga) {
        String sql = "INSERT INTO menu (nama_menu, harga) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, namaMenu.trim());
            stmt.setDouble(2, harga);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===================== UPDATE =====================
    public boolean updateMenu(int idMenu, String namaMenu, double harga) {
        String sql = "UPDATE menu SET nama_menu = ?, harga = ? WHERE id_menu = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, namaMenu.trim());
            stmt.setDouble(2, harga);
            stmt.setInt(3, idMenu);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===================== DELETE =====================
    /**
     * Hapus menu dari database.
     * Karena FK pesanan_makanan.id_menu ON DELETE CASCADE,
     * pesanan terkait juga ikut terhapus.
     */
    public boolean hapusMenu(int idMenu) {
        String sql = "DELETE FROM menu WHERE id_menu = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMenu);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cek apakah menu sudah pernah dipesan (ada di tabel pesanan_makanan).
     * Dipakai untuk memberi peringatan sebelum hapus.
     */
    public int hitungPesananTerkait(int idMenu) {
        String sql = "SELECT COUNT(*) AS total FROM pesanan_makanan WHERE id_menu = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMenu);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ===================== METHOD LAMA (pesanan F&B) =====================
    public boolean hasActiveReservation(String username) {
        String sql = "SELECT r.id_reservasi FROM reservasi r " +
                "JOIN users u ON r.id_user = u.id_user " +
                "WHERE LOWER(u.username) = LOWER(?) " +
                "AND LOWER(r.status_reservasi) IN ('diterima', 'check-in', 'lunas (check-in)', 'disetujui', 'pending check-in')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String simpanPesananFB(String username, int idMenu, int jumlah, double totalHarga, String metodeBayar) {
        String sqlReservasi = "SELECT r.id_reservasi FROM reservasi r " +
                "JOIN users u ON r.id_user = u.id_user " +
                "WHERE LOWER(u.username) = LOWER(?) " +
                "AND LOWER(r.status_reservasi) IN ('diterima', 'check-in', 'lunas (check-in)', 'disetujui', 'pending check-in') " +
                "ORDER BY r.id_reservasi DESC LIMIT 1";
        String sqlInsert = "INSERT INTO pesanan_makanan (id_reservasi, id_menu, jumlah, subtotal, status_pesanan) VALUES (?, ?, ?, ?, 'Terkirim ke Dapur')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtRes = conn.prepareStatement(sqlReservasi)) {

            stmtRes.setString(1, username.trim());
            ResultSet rs = stmtRes.executeQuery();

            if (rs.next()) {
                int idReservasi = rs.getInt("id_reservasi");
                try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                    stmtInsert.setInt(1, idReservasi);
                    stmtInsert.setInt(2, idMenu);
                    stmtInsert.setInt(3, jumlah);
                    stmtInsert.setDouble(4, totalHarga);
                    int rows = stmtInsert.executeUpdate();
                    return rows > 0 ? "OK" : "Gagal menyimpan pesanan ke tabel pesanan_makanan.";
                }
            } else {
                return "Tidak ditemukan ID reservasi aktif untuk user '" + username + "'.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public List<PesananFBDetail> getRiwayatPesananByUsername(String username) {
        List<PesananFBDetail> list = new ArrayList<>();
        String sql = "SELECT pm.id_pesanan, pm.id_reservasi, COALESCE(r.no_kamar, '-') AS no_kamar, " +
                "m.nama_menu, pm.jumlah, pm.subtotal, " +
                "CASE WHEN LOWER(r.status_reservasi) = 'selesai' THEN 'Selesai' " +
                "ELSE COALESCE(pm.status_pesanan, 'Terkirim ke Dapur') END AS status_pesanan " +
                "FROM pesanan_makanan pm " +
                "JOIN reservasi r ON pm.id_reservasi = r.id_reservasi " +
                "JOIN users u ON r.id_user = u.id_user " +
                "JOIN menu m ON pm.id_menu = m.id_menu " +
                "WHERE LOWER(u.username) = LOWER(?) " +
                "ORDER BY pm.id_pesanan DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new PesananFBDetail(
                        rs.getInt("id_pesanan"),
                        rs.getInt("id_reservasi"),
                        rs.getString("no_kamar"),
                        rs.getString("nama_menu"),
                        rs.getInt("jumlah"),
                        rs.getDouble("subtotal"),
                        rs.getString("status_pesanan")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
