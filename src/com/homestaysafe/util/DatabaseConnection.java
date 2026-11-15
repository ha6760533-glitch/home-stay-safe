package com.homestaysafe.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // URL ke database PostgreSQL milikmu ('home_stay_safe')
    private static final String URL = "jdbc:postgresql://localhost:5432/home_stay_safe";
    private static final String USER = "postgres";
    private static final String PASSWORD = "h"; // ⚠️ GANTI SESUAI PASSWORD POSTGRESQL KAMU!

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