package com.homestaysafe.gui;

import com.homestaysafe.util.UIHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardFrame extends JFrame {

    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

    public DashboardFrame() {
        setTitle("HomestaySafe - Dashboard Utama");
        setSize(660, 460);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(COLOR_BG);
        outerPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel cardPanel = UIHelper.createRoundedCard(22, 42, 45);
        cardPanel.setLayout(new BorderLayout(0, 24));

        JPanel headerPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        headerPanel.setOpaque(false);

        JLabel lblTag = new JLabel("HOMESTAYSAFE RESORT & RESERVASI", SwingConstants.CENTER);
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTag.setForeground(COLOR_GOLD);

        JLabel lblTitle = new JLabel("Selamat Datang", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Serif", Font.BOLD, 32));
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

        JButton btnLogin = UIHelper.goldButton("MASUK KE SISTEM (LOGIN)");
        btnLogin.setPreferredSize(new Dimension(300, 48));

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