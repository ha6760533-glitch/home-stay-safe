package com.homestaysafe.gui;

import com.homestaysafe.dao.UserDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JToggleButton btnTogglePass;
    private JButton btnLogin;
    private JButton btnRegister;
    private JButton btnBack;
    private UserDAO userDAO;

    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_CARD = new Color(255, 255, 255);
    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

    public LoginFrame() {
        userDAO = new UserDAO();

        setTitle("HomestaySafe - Login System");
        setSize(480, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(COLOR_BG);
        outerPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(COLOR_CARD);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(25, 30, 25, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblHeaderTag = new JLabel("HOMESTAYSAFE RESORT & HOTEL", SwingConstants.CENTER);
        lblHeaderTag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHeaderTag.setForeground(COLOR_GOLD);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        cardPanel.add(lblHeaderTag, gbc);

        JLabel lblTitle = new JLabel("Masuk ke Akun", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Serif", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 6, 15, 6);
        cardPanel.add(lblTitle, gbc);

        gbc.insets = new Insets(4, 6, 4, 6);

        JLabel lblUser = new JLabel("USERNAME");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblUser.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        cardPanel.add(lblUser, gbc);

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsername.setPreferredSize(new Dimension(300, 36));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        cardPanel.add(txtUsername, gbc);

        JLabel lblPass = new JLabel("PASSWORD");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPass.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        cardPanel.add(lblPass, gbc);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setPreferredSize(new Dimension(250, 36));
        txtPassword.setEchoChar('•');
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        cardPanel.add(txtPassword, gbc);

        btnTogglePass = new JToggleButton("👁");
        btnTogglePass.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        btnTogglePass.setPreferredSize(new Dimension(50, 36));
        btnTogglePass.setFocusable(false);
        btnTogglePass.setBackground(COLOR_BG);
        btnTogglePass.setBorder(new LineBorder(new Color(203, 213, 225), 1));
        btnTogglePass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTogglePass.addActionListener((ActionEvent e) -> {
            if (btnTogglePass.isSelected()) {
                txtPassword.setEchoChar((char) 0);
                btnTogglePass.setText("🙈");
            } else {
                txtPassword.setEchoChar('•');
                btnTogglePass.setText("👁");
            }
        });
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        cardPanel.add(btnTogglePass, gbc);

        btnLogin = new JButton("MASUK SEKARANG");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setBackground(COLOR_NAVY);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(300, 42));
        btnLogin.setFocusPainted(false);
        btnLogin.setOpaque(true);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> processLogin());

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 6, 4, 6);
        cardPanel.add(btnLogin, gbc);

        btnRegister = new JButton("Belum Punya Akun Tamu? Daftar di Sini");
        btnRegister.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRegister.setForeground(COLOR_GOLD);
        btnRegister.setContentAreaFilled(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> {
            dispose();
            new RegisterFrame().setVisible(true);
        });

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.insets = new Insets(2, 6, 2, 6);
        cardPanel.add(btnRegister, gbc);

        btnBack = new JButton("← Kembali ke Dashboard Utama");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBack.setForeground(COLOR_TEXT_MUTED);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            dispose();
            new DashboardFrame().setVisible(true);
        });

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        cardPanel.add(btnBack, gbc);

        outerPanel.add(cardPanel);
        add(outerPanel);
    }

    private void processLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Password wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String role = userDAO.checkLogin(user, pass);

        if (role != null) {
            String cleanRole = role.trim();
            JOptionPane.showMessageDialog(this, "Login Berhasil!\nSelamat datang, Anda masuk sebagai: " + cleanRole, "Sukses", JOptionPane.INFORMATION_MESSAGE);

            if (cleanRole.equalsIgnoreCase("Tamu")) {
                dispose();
                new TamuFrame(user).setVisible(true);
            } else if (cleanRole.equalsIgnoreCase("Resepsionis")) {
                dispose();
                new ResepsionisFrame(user).setVisible(true);
            } else if (cleanRole.equalsIgnoreCase("Manajer") || cleanRole.equalsIgnoreCase("Manager")) {
                dispose();
                new ManajerFrame(user).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Role di database tidak dikenali: '" + cleanRole + "'", "Error Role", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Username atau Password salah!", "Login Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}