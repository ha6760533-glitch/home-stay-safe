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
        private String kategori;
        private String namaMenu;
        private double harga;
        private boolean available;

        public Menu(int idMenu, String namaMenu, double harga) {
            this(idMenu, "Makanan Utama", namaMenu, harga, true);
        }

        public Menu(int idMenu, String kategori, String namaMenu, double harga, boolean available) {
            this.idMenu = idMenu;
            this.kategori = kategori;
            this.namaMenu = namaMenu;
            this.harga = harga;
            this.available = available;
        }

        public int getIdMenu() { return idMenu; }
        public String getKategori() { return kategori; }
        public String getNamaMenu() { return namaMenu; }
        public double getHarga() { return harga; }
        public boolean isAvailable() { return available; }
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
        String sql = "SELECT id_menu, kategori, nama_menu, harga, is_available FROM menu ORDER BY id_menu ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                listMenu.add(new Menu(
                        rs.getInt("id_menu"),
                        rs.getString("kategori"),
                        rs.getString("nama_menu"),
                        rs.getDouble("harga"),
                        rs.getBoolean("is_available")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listMenu;
    }

    // ===================== CREATE =====================
    public boolean tambahMenu(String namaMenu, double harga) {
        return tambahMenu("Makanan Utama", namaMenu, harga);
    }

    public boolean tambahMenu(String kategori, String namaMenu, double harga) {
        String sql = "INSERT INTO menu (kategori, nama_menu, harga, is_available) VALUES (?, ?, ?, TRUE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, (kategori == null || kategori.trim().isEmpty()) ? "Makanan Utama" : kategori.trim());
            stmt.setString(2, namaMenu.trim());
            stmt.setDouble(3, harga);
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

    public boolean updateMenu(int idMenu, String kategori, String namaMenu, double harga) {
        String sql = "UPDATE menu SET kategori = ?, nama_menu = ?, harga = ? WHERE id_menu = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, kategori.trim());
            stmt.setString(2, namaMenu.trim());
            stmt.setDouble(3, harga);
            stmt.setInt(4, idMenu);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===================== DELETE =====================
    public boolean hapusMenu(int idMenu) {
        String sqlDeletePesanan = "DELETE FROM pesanan_makanan WHERE id_menu = ?";
        String sqlDeleteMenu = "DELETE FROM menu WHERE id_menu = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlDeletePesanan);
                 PreparedStatement ps2 = conn.prepareStatement(sqlDeleteMenu)) {
                ps1.setInt(1, idMenu);
                ps1.executeUpdate();

                ps2.setInt(1, idMenu);
                int rows = ps2.executeUpdate();
                conn.commit();
                return rows > 0;
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

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

    // ===================== VALIDASI & SIMPAN PESANAN F&B =====================
    public boolean hasActiveReservation(String username) {
        String sql = "SELECT r.id_reservasi FROM reservasi r " +
                "JOIN users u ON r.id_user = u.id_user " +
                "WHERE LOWER(u.username) = LOWER(?) " +
                "AND LOWER(TRIM(r.status_reservasi)) = 'check-in' " +
                "AND r.id_kamar IS NOT NULL";

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
                "AND LOWER(TRIM(r.status_reservasi)) = 'check-in' " +
                "AND r.id_kamar IS NOT NULL " +
                "ORDER BY r.id_reservasi DESC LIMIT 1";
        String sqlHargaMenu = "SELECT harga FROM menu WHERE id_menu = ?";
        String sqlInsert = "INSERT INTO pesanan_makanan (id_reservasi, id_menu, jumlah, harga_satuan_deal, status_pesanan) " +
                "VALUES (?, ?, ?, ?, 'Terkirim ke Dapur')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtRes = conn.prepareStatement(sqlReservasi);
             PreparedStatement stmtMenu = conn.prepareStatement(sqlHargaMenu)) {

            stmtRes.setString(1, username.trim());
            ResultSet rs = stmtRes.executeQuery();

            if (rs.next()) {
                int idReservasi = rs.getInt("id_reservasi");

                stmtMenu.setInt(1, idMenu);
                ResultSet rsMenu = stmtMenu.executeQuery();
                double hargaSatuan = rsMenu.next()
                        ? rsMenu.getDouble("harga")
                        : (jumlah > 0 ? totalHarga / jumlah : totalHarga);

                try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                    stmtInsert.setInt(1, idReservasi);
                    stmtInsert.setInt(2, idMenu);
                    stmtInsert.setInt(3, jumlah);
                    stmtInsert.setDouble(4, hargaSatuan);
                    int rows = stmtInsert.executeUpdate();
                    return rows > 0 ? "OK" : "Gagal menyimpan pesanan ke tabel pesanan_makanan.";
                }
            } else {
                return "Pemesanan makanan hanya tersedia setelah Anda melakukan Check-In di Resepsionis.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public List<PesananFBDetail> getRiwayatPesananByUsername(String username) {
        List<PesananFBDetail> list = new ArrayList<>();
        String sql = "SELECT pm.id_pesanan, pm.id_reservasi, COALESCE(k.no_kamar, '-') AS no_kamar, " +
                "m.nama_menu, pm.jumlah, pm.subtotal, " +
                "CASE WHEN LOWER(r.status_reservasi) = 'selesai' THEN 'Selesai' " +
                "ELSE COALESCE(pm.status_pesanan, 'Terkirim ke Dapur') END AS status_pesanan " +
                "FROM pesanan_makanan pm " +
                "JOIN reservasi r ON pm.id_reservasi = r.id_reservasi " +
                "JOIN users u ON r.id_user = u.id_user " +
                "JOIN menu m ON pm.id_menu = m.id_menu " +
                "LEFT JOIN kamar k ON r.id_kamar = k.id_kamar " +
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