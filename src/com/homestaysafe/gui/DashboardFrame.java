package com.homestaysafe.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class DashboardFrame extends JFrame {

    private final Color COLOR_BG = new Color(245, 247, 250);         // Warm Luxury Gray
    private final Color COLOR_CARD = new Color(255, 255, 255);       // Pure White Card
    private final Color COLOR_NAVY = new Color(15, 23, 42);          // Deep Hotel Navy
    private final Color COLOR_GOLD = new Color(197, 168, 128);       // Warm Gold Accent
    private final Color COLOR_TEXT_MUTED = new Color(100, 116, 139); // Soft Slate

    public DashboardFrame() {
        setTitle("HomestaySafe - Dashboard Utama");
        setSize(650, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(COLOR_BG);
        outerPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(COLOR_CARD);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(40, 40, 40, 40)
        ));

        JPanel headerPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        headerPanel.setOpaque(false);

        JLabel lblTag = new JLabel("HOMESTAYSAFE RESORT & RESERVASI", SwingConstants.CENTER);
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTag.setForeground(COLOR_GOLD);

        JLabel lblTitle = new JLabel("Selamat Datang", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Serif", Font.BOLD, 30));
        lblTitle.setForeground(COLOR_NAVY);

        JLabel lblSub = new JLabel("Sistem Pemesanan & Pengelolaan Homestay Aman dan Nyaman", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(COLOR_TEXT_MUTED);

        headerPanel.add(lblTag);
        headerPanel.add(lblTitle);
        headerPanel.add(lblSub);
        cardPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        JButton btnLogin = new JButton("MASUK KE SISTEM (LOGIN)");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(COLOR_GOLD);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(300, 45));
        btnLogin.setFocusPainted(false);
        btnLogin.setOpaque(true);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnLogin.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        centerPanel.add(btnLogin);
        cardPanel.add(centerPanel, BorderLayout.CENTER);

        outerPanel.add(cardPanel);
        add(outerPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DashboardFrame().setVisible(true));
    }
}