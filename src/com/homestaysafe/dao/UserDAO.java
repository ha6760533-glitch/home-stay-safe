package com.homestaysafe.dao;

import com.homestaysafe.util.DatabaseConnection;
import com.homestaysafe.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.homestaysafe.model.User;
import com.homestaysafe.model.Tamu;
import com.homestaysafe.model.Resepsionis;
import com.homestaysafe.model.Manager;

public class UserDAO {

    public String checkLogin(String username, String password) {
        String sql = "SELECT password_hash, role FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(?))";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String dbPass = rs.getString("password_hash").trim();
                String role = rs.getString("role").trim();
                String hashedInput = PasswordUtil.hash(password.trim());

                if (dbPass.equals(hashedInput) || dbPass.equals(password.trim())) {
                    return role;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Login OOP: mengembalikan object User sesuai role (Polymorphism) */
    public User login(String username, String password) {
        String sql = "SELECT id_user, nama, username, no_identitas, password_hash, role " +
                "FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(?))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String dbPass = rs.getString("password_hash").trim();
                String hashedInput = PasswordUtil.hash(password.trim());

                if (dbPass.equals(hashedInput) || dbPass.equals(password.trim())) {
                    int id = rs.getInt("id_user");
                    String nama = rs.getString("nama");
                    String uname = rs.getString("username");
                    String nik = rs.getString("no_identitas");
                    String role = rs.getString("role").trim();

                    if (role.equalsIgnoreCase("Tamu")) {
                        return new Tamu(id, nama, uname, nik);
                    } else if (role.equalsIgnoreCase("Resepsionis")) {
                        return new Resepsionis(id, nama, uname, nik);
                    } else if (role.equalsIgnoreCase("Manager") || role.equalsIgnoreCase("Manajer")) {
                        return new Manager(id, nama, uname, nik);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isNamaExist(String nama) {
        String sql = "SELECT id_user FROM users WHERE LOWER(TRIM(nama)) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nama.trim());
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isNoIdentitasExist(String noIdentitas) {
        String sql = "SELECT id_user FROM users WHERE TRIM(no_identitas) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, noIdentitas.trim());
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getNamaByNoIdentitas(String noIdentitas) {
        String sql = "SELECT nama FROM users WHERE TRIM(no_identitas) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, noIdentitas.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nama");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isUsernameExist(String username) {
        String sql = "SELECT id_user FROM users WHERE LOWER(TRIM(username)) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isPasswordExist(String password) {
        String sql = "SELECT id_user FROM users WHERE password_hash = ? OR password_hash = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String cleanPass = password.trim();
            stmt.setString(1, PasswordUtil.hash(cleanPass));
            stmt.setString(2, cleanPass);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean register(String nama, String noIdentitas, String username, String password, String role) {
        String sql = "INSERT INTO users (nama, no_identitas, username, password_hash, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nama.trim());
            stmt.setString(2, noIdentitas.trim());
            stmt.setString(3, username.trim());
            stmt.setString(4, PasswordUtil.hash(password.trim()));
            stmt.setString(5, role.trim());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}