package com.homestaysafe.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = env("DB_URL", "jdbc:postgresql://localhost:5432/home_stay_safe");
    private static final String USER = env("DB_USER", "postgres");
    private static final String PASSWORD = env("DB_PASSWORD", "h"); // default; sesuaikan dengan password PostgreSQL kamu

    private static String env(String nama, String bawaan) {
        String nilai = System.getenv(nama);
        return (nilai == null || nilai.isBlank()) ? bawaan : nilai;
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Driver PostgreSQL JDBC tidak ditemukan!");
            e.printStackTrace();
            throw new SQLException(e);
        }
    }

    // Main method untuk menguji apakah koneksi ke database berhasil
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("✅ OK: Sukses terhubung ke database PostgreSQL 'home_stay_safe'!");
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Gagal terhubung ke database PostgreSQL!");
            e.printStackTrace();
        }
    }
}