package com.homestaysafe.gui;

import com.homestaysafe.util.UIHelper;
import com.homestaysafe.dao.UserDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;

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
    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

    public RegisterFrame() {
        userDAO = new UserDAO();

        setTitle("HomestaySafe - Registrasi Akun Tamu");
        setSize(490, 665);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(COLOR_BG);
        outerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel cardPanel = UIHelper.createRoundedCard(22, 22, 32);
        cardPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblLogo = new JLabel(UIHelper.getLogoIcon(52, 56), SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        cardPanel.add(lblLogo, gbc);

        JLabel lblHeaderTag = new JLabel("HOMESTAYSAFE RESORT & HOTEL", SwingConstants.CENTER);
        lblHeaderTag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHeaderTag.setForeground(COLOR_GOLD);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        cardPanel.add(lblHeaderTag, gbc);

        JLabel lblTitle = new JLabel("Registrasi Tamu Baru", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Serif", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 6, 12, 6);
        cardPanel.add(lblTitle, gbc);

        gbc.insets = new Insets(4, 6, 4, 6);

        JLabel lblNama = new JLabel("NAMA LENGKAP");
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblNama.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        cardPanel.add(lblNama, gbc);

        txtNama = createStyledTextField();
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        cardPanel.add(txtNama, gbc);

        JLabel lblNoIdentitas = new JLabel("NO. IDENTITAS KTP (16 DIGIT ANGKA)");
        lblNoIdentitas.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblNoIdentitas.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        cardPanel.add(lblNoIdentitas, gbc);

        txtNoIdentitas = createStyledTextField();
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        cardPanel.add(txtNoIdentitas, gbc);

        JLabel lblUser = new JLabel("USERNAME");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblUser.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        cardPanel.add(lblUser, gbc);

        txtUsername = createStyledTextField();
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        cardPanel.add(txtUsername, gbc);

        JLabel lblPass = new JLabel("PASSWORD (MINIMAL 6 DIGIT)");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPass.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
        cardPanel.add(lblPass, gbc);

        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(250, 38));
        txtPassword.setEchoChar('•');
        UIHelper.styleField(txtPassword);
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        cardPanel.add(txtPassword, gbc);

        btnTogglePass = createEyeToggleButton(38);
        btnTogglePass.addActionListener((ActionEvent e) -> {
            if (btnTogglePass.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
            btnTogglePass.repaint();
        });
        gbc.gridx = 1; gbc.gridy = 10; gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        cardPanel.add(btnTogglePass, gbc);

        btnRegister = UIHelper.goldButton("BUAT AKUN TAMU");
        btnRegister.setPreferredSize(new Dimension(300, 44));
        btnRegister.addActionListener(e -> processRegister());

        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 6, 4, 6);
        cardPanel.add(btnRegister, gbc);

        btnBackToLogin = new JButton("Sudah Memiliki Akun? Masuk di Sini");
        btnBackToLogin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBackToLogin.setForeground(COLOR_TEXT_MUTED);
        btnBackToLogin.setContentAreaFilled(false);
        btnBackToLogin.setBorderPainted(false);
        btnBackToLogin.setFocusPainted(false);
        btnBackToLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBackToLogin.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        gbc.gridx = 0; gbc.gridy = 12; gbc.gridwidth = 2;
        gbc.insets = new Insets(2, 6, 2, 6);
        cardPanel.add(btnBackToLogin, gbc);

        outerPanel.add(cardPanel);
        add(outerPanel);
    }

    private JToggleButton createEyeToggleButton(int height) {
        JToggleButton btn = new JToggleButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? UIHelper.COLOR_SOFT_HOVER : UIHelper.COLOR_SOFT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.setColor(UIHelper.COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.setColor(COLOR_NAVY);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(cx - 10, cy - 6, 20, 12, 0, 180);
                g2.drawArc(cx - 10, cy - 6, 20, 12, 180, 180);
                g2.fillOval(cx - 3, cy - 3, 6, 6);

                if (isSelected()) {
                    g2.setColor(UIHelper.COLOR_DANGER);
                    g2.drawLine(cx - 9, cy + 7, cx + 9, cy - 7);
                }
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(48, height));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setFocusable(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setToolTipText("Tampilkan / Sembunyikan Password");
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(300, 38));
        UIHelper.styleField(tf);
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