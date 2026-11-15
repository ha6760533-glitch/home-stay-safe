package com.homestaysafe.gui;

import com.homestaysafe.dao.UserDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RegisterFrame extends JFrame {
    private JTextField txtNama;
    private JTextField txtNoIdentitas;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JToggleButton btnTogglePass;
    private JButton btnRegister;
    private JButton btnBackToLogin;
    private UserDAO userDAO;

    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_CARD = new Color(255, 255, 255);
    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

    public RegisterFrame() {
        userDAO = new UserDAO();

        setTitle("HomestaySafe - Registrasi Akun Tamu");
        setSize(480, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(COLOR_BG);
        outerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

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

        JLabel lblTitle = new JLabel("Registrasi Tamu Baru", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Serif", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 6, 15, 6);
        cardPanel.add(lblTitle, gbc);

        gbc.insets = new Insets(4, 6, 4, 6);

        JLabel lblNama = new JLabel("NAMA LENGKAP");
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblNama.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        cardPanel.add(lblNama, gbc);

        txtNama = createStyledTextField();
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        cardPanel.add(txtNama, gbc);

        JLabel lblNoIdentitas = new JLabel("NO. IDENTITAS KTP (16 DIGIT ANGKA)");
        lblNoIdentitas.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblNoIdentitas.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        cardPanel.add(lblNoIdentitas, gbc);

        txtNoIdentitas = createStyledTextField();
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        cardPanel.add(txtNoIdentitas, gbc);

        JLabel lblUser = new JLabel("USERNAME");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblUser.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        cardPanel.add(lblUser, gbc);

        txtUsername = createStyledTextField();
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        cardPanel.add(txtUsername, gbc);

        JLabel lblPass = new JLabel("PASSWORD (MINIMAL 6 DIGIT)");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPass.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        cardPanel.add(lblPass, gbc);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setPreferredSize(new Dimension(250, 36));
        txtPassword.setEchoChar('•');
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 1;
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
        gbc.gridx = 1; gbc.gridy = 9; gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        cardPanel.add(btnTogglePass, gbc);

        btnRegister = new JButton("BUAT AKUN TAMU");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRegister.setBackground(COLOR_GOLD);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setPreferredSize(new Dimension(300, 42));
        btnRegister.setFocusPainted(false);
        btnRegister.setOpaque(true);
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> processRegister());

        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 4, 6);
        cardPanel.add(btnRegister, gbc);

        btnBackToLogin = new JButton("Sudah Memiliki Akun? Masuk di Sini");
        btnBackToLogin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBackToLogin.setForeground(COLOR_TEXT_MUTED);
        btnBackToLogin.setContentAreaFilled(false);
        btnBackToLogin.setBorderPainted(false);
        btnBackToLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBackToLogin.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 2;
        gbc.insets = new Insets(2, 6, 2, 6);
        cardPanel.add(btnBackToLogin, gbc);

        outerPanel.add(cardPanel);
        add(outerPanel);
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(300, 36));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return tf;
    }

    private void processRegister() {
        String nama = txtNama.getText().trim();
        String noIdentitas = txtNoIdentitas.getText().trim();
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();
        String role = "Tamu";

        if (nama.isEmpty() || noIdentitas.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seluruh formulir pendaftaran wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!nama.matches("^[a-zA-Z\\s.'-]+$")) {
            JOptionPane.showMessageDialog(this, "Nama lengkap hanya boleh berisi huruf dan tanda baca nama yang valid!", "Validasi Nama", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!noIdentitas.matches("\\d{16}")) {
            JOptionPane.showMessageDialog(this, "No. Identitas harus berupa angka dan pas 16 digit!", "Validasi No. Identitas", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (pass.length() < 6) {
            JOptionPane.showMessageDialog(this, "Demi keamanan, Password minimal harus 6 karakter!", "Keamanan Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userDAO.isNamaExist(nama)) {
            JOptionPane.showMessageDialog(this, "Registrasi Ditolak!\nNama Lengkap '" + nama + "' sudah terdaftar di sistem.", "Data Duplikat", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userDAO.isNoIdentitasExist(noIdentitas)) {
            JOptionPane.showMessageDialog(this, "Registrasi Ditolak!\nNo. Identitas KTP '" + noIdentitas + "' sudah digunakan oleh akun lain.", "Data Duplikat", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userDAO.isUsernameExist(user)) {
            JOptionPane.showMessageDialog(this, "Registrasi Ditolak!\nUsername '" + user + "' sudah dipakai. Silakan pilih username lain.", "Data Duplikat", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userDAO.isPasswordExist(pass)) {
            JOptionPane.showMessageDialog(this, "Registrasi Ditolak!\nPassword tersebut sudah digunakan oleh pengguna lain. Gunakan kombinasi password yang unik!", "Keamanan Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isSuccess = userDAO.register(nama, noIdentitas, user, pass, role);

        if (isSuccess) {
            JOptionPane.showMessageDialog(this, "Selamat! Akun Tamu HomestaySafe berhasil dibuat.\nSilakan masuk dengan akun baru Anda.", "Registrasi Sukses", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginFrame().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Registrasi Gagal! Terjadi kesalahan saat menyimpan data ke database.", "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }
}