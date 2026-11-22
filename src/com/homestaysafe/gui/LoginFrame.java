package com.homestaysafe.gui;

import com.homestaysafe.dao.UserDAO;
import com.homestaysafe.util.UIHelper;
import com.homestaysafe.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JToggleButton btnTogglePass;
    private JButton btnLogin;
    private JButton btnRegister;
    private JButton btnBack;
    private UserDAO userDAO;

    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

    public LoginFrame() {
        userDAO = new UserDAO();

        setTitle("HomestaySafe - Login System");
        setSize(490, 540);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(COLOR_BG);
        outerPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel cardPanel = UIHelper.createRoundedCard(22, 30, 34);
        cardPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblHeaderTag = new JLabel("HOMESTAYSAFE RESORT & HOTEL", SwingConstants.CENTER);
        lblHeaderTag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHeaderTag.setForeground(COLOR_GOLD);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        cardPanel.add(lblHeaderTag, gbc);

        JLabel lblTitle = new JLabel("Masuk ke Akun", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Serif", Font.BOLD, 25));
        lblTitle.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 6, 16, 6);
        cardPanel.add(lblTitle, gbc);

        gbc.insets = new Insets(4, 6, 4, 6);

        JLabel lblUser = new JLabel("USERNAME");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblUser.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        cardPanel.add(lblUser, gbc);

        txtUsername = new JTextField();
        txtUsername.setPreferredSize(new Dimension(300, 40));
        UIHelper.styleField(txtUsername);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        cardPanel.add(txtUsername, gbc);

        JLabel lblPass = new JLabel("PASSWORD");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPass.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        cardPanel.add(lblPass, gbc);

        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(250, 40));
        txtPassword.setEchoChar('•');
        UIHelper.styleField(txtPassword);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        cardPanel.add(txtPassword, gbc);

        btnTogglePass = createEyeToggleButton(40);
        btnTogglePass.addActionListener((ActionEvent e) -> {
            if (btnTogglePass.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
            btnTogglePass.repaint();
        });
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        cardPanel.add(btnTogglePass, gbc);

        btnLogin = UIHelper.goldButton("MASUK");
        btnLogin.setPreferredSize(new Dimension(300, 46));
        btnLogin.addActionListener(e -> processLogin());

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 6, 4, 6);
        cardPanel.add(btnLogin, gbc);

        btnRegister = new JButton("Belum Punya Akun Tamu? Daftar di Sini");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRegister.setForeground(COLOR_GOLD);
        btnRegister.setContentAreaFilled(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> {
            dispose();
            new RegisterFrame().setVisible(true);
        });

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 6, 2, 6);
        cardPanel.add(btnRegister, gbc);

        btnBack = new JButton("← Kembali ke Dashboard Utama");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBack.setForeground(COLOR_TEXT_MUTED);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
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

    private void processLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Password wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User loggedIn = userDAO.login(user, pass);

        if (loggedIn != null) {
            JOptionPane.showMessageDialog(this,
                    "Login Berhasil!\nSelamat datang, " + loggedIn.getNama() +
                            "\nRole: " + loggedIn.getDisplayRole(),
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);

            dispose();
            loggedIn.createDashboard().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Username atau Password salah!", "Login Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}