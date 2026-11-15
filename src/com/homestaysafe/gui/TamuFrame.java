package com.homestaysafe.gui;

import com.homestaysafe.gui.panel.BookingCardPanel;
import com.homestaysafe.gui.panel.FBCardPanel;
import com.homestaysafe.gui.panel.LihatKamarPanel;
import com.homestaysafe.gui.panel.RiwayatPanel;
import com.homestaysafe.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TamuFrame extends JFrame {

    private final String usernameTamu;
    private String namaLengkapTamu = "-";
    private String noIdentitasTamu = "-";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final JPanel sidebarPanel;
    private boolean isSidebarOpen = false;

    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_SIDEBAR_HOVER = new Color(30, 41, 59);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_BG = new Color(245, 247, 250);

    public TamuFrame(String username) {
        this.usernameTamu = username;
        loadDataTamu();

        setTitle("HomestaySafe - Portal Tamu (" + username + ")");
        setSize(1240, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_NAVY);
        header.setPreferredSize(new Dimension(1240, 68));
        header.setBorder(new EmptyBorder(0, 25, 0, 25));

        JLabel lblBrand = new JLabel("HomeStaySafe Resort");
        lblBrand.setFont(new Font("Serif", Font.BOLD, 22));
        lblBrand.setForeground(COLOR_GOLD);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 13));
        rightHeader.setOpaque(false);

        JLabel lblUser = new JLabel("Tamu: " + usernameTamu);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(Color.WHITE);

        JButton btnProfile = new JButton(new UserAvatarIcon());
        btnProfile.setBackground(Color.WHITE);
        btnProfile.setPreferredSize(new Dimension(40, 40));
        btnProfile.setFocusPainted(false);
        btnProfile.setOpaque(true);
        btnProfile.setBorder(new LineBorder(COLOR_GOLD, 1, true));
        btnProfile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProfile.setToolTipText("Profil Tamu & Logout");
        btnProfile.addActionListener(e -> showProfileDialog());

        JButton btnToggleSidebar = new JButton("...");
        btnToggleSidebar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnToggleSidebar.setBackground(COLOR_GOLD);
        btnToggleSidebar.setForeground(Color.WHITE);
        btnToggleSidebar.setPreferredSize(new Dimension(46, 40));
        btnToggleSidebar.setFocusPainted(false);
        btnToggleSidebar.setOpaque(true);
        btnToggleSidebar.setBorderPainted(false);
        btnToggleSidebar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggleSidebar.setToolTipText("Buka / Tutup Menu Navigasi");
        btnToggleSidebar.addActionListener(e -> toggleSidebar());

        rightHeader.add(lblUser);
        rightHeader.add(btnProfile);
        rightHeader.add(btnToggleSidebar);

        header.add(lblBrand, BorderLayout.WEST);
        header.add(rightHeader, BorderLayout.EAST);

        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(COLOR_NAVY);
        sidebarPanel.setPreferredSize(new Dimension(250, 0));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, COLOR_SIDEBAR_HOVER));
        sidebarPanel.setVisible(false);

        JLabel lblMenuSection = new JLabel("MENU LAYANAN TAMU");
        lblMenuSection.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMenuSection.setForeground(COLOR_GOLD);
        lblMenuSection.setBorder(new EmptyBorder(22, 22, 10, 15));
        lblMenuSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(lblMenuSection);

        sidebarPanel.add(createSidebarButton("Lihat Tipe Kamar", () -> {
            contentPanel.add(new LihatKamarPanel(), "TipeKamar");
            cardLayout.show(contentPanel, "TipeKamar");
            toggleSidebar();
        }));

        sidebarPanel.add(createSidebarButton("Booking Online", () -> {
            contentPanel.add(new BookingCardPanel(usernameTamu, this, contentPanel, cardLayout), "Booking");
            cardLayout.show(contentPanel, "Booking");
            toggleSidebar();
        }));

        sidebarPanel.add(createSidebarButton("Layanan Makanan", () -> {
            contentPanel.add(new FBCardPanel(usernameTamu), "FB");
            cardLayout.show(contentPanel, "FB");
            toggleSidebar();
        }));

        sidebarPanel.add(createSidebarButton("Riwayat Pemesanan", () -> {
            contentPanel.add(new RiwayatPanel(usernameTamu), "Riwayat");
            cardLayout.show(contentPanel, "Riwayat");
            toggleSidebar();
        }));

        sidebarPanel.add(Box.createVerticalGlue());

        contentPanel.setBackground(COLOR_BG);
        contentPanel.add(new LihatKamarPanel(), "TipeKamar");
        contentPanel.add(new BookingCardPanel(usernameTamu, this, contentPanel, cardLayout), "Booking");
        contentPanel.add(new FBCardPanel(usernameTamu), "FB");
        contentPanel.add(new RiwayatPanel(usernameTamu), "Riwayat");

        add(header, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(sidebarPanel, BorderLayout.EAST);

        cardLayout.show(contentPanel, "TipeKamar");
    }

    private void toggleSidebar() {
        isSidebarOpen = !isSidebarOpen;
        sidebarPanel.setVisible(isSidebarOpen);
        revalidate();
        repaint();
    }

    private JButton createSidebarButton(String text, Runnable action) {
        JButton btn = new JButton("  •  " + text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_NAVY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(250, 46));
        btn.setPreferredSize(new Dimension(250, 46));
        btn.setBorder(new EmptyBorder(0, 18, 0, 15));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(COLOR_SIDEBAR_HOVER);
                btn.setForeground(COLOR_GOLD);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(COLOR_NAVY);
                btn.setForeground(Color.WHITE);
            }
        });

        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void loadDataTamu() {
        String sql = "SELECT nama, no_identitas FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usernameTamu.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.namaLengkapTamu = rs.getString("nama");
                this.noIdentitasTamu = rs.getString("no_identitas") != null ? rs.getString("no_identitas") : "-";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProfileDialog() {
        JDialog dialog = new JDialog(this, "Profil Akun Tamu", true);
        dialog.setSize(420, 330);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel hdr = new JLabel("<html><center><b style='color:#c5a880;font-size:14px;'>PROFIL AKUN TAMU</b><br>" +
                "<span style='color:white;font-size:11px;'>HomeStaySafe Resort</span></center></html>", SwingConstants.CENTER);
        hdr.setOpaque(true);
        hdr.setBackground(COLOR_NAVY);
        hdr.setBorder(new EmptyBorder(14, 10, 14, 10));

        JPanel infoGrid = new JPanel(new GridLayout(3, 2, 8, 10));
        infoGrid.setBackground(new Color(248, 250, 252));
        infoGrid.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        addProfilRow(infoGrid, "Nama Lengkap", namaLengkapTamu);
        addProfilRow(infoGrid, "No. Identitas", noIdentitasTamu);
        addProfilRow(infoGrid, "Username & Role", usernameTamu + " (Tamu)");

        JButton btnLogout = new JButton("Logout dari Akun");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setBackground(new Color(220, 38, 38));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setPreferredSize(new Dimension(0, 42));
        btnLogout.setFocusPainted(false);
        btnLogout.setOpaque(true);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            dialog.dispose();
            dispose();
            new LoginFrame().setVisible(true);
        });

        root.add(hdr, BorderLayout.NORTH);
        root.add(infoGrid, BorderLayout.CENTER);
        root.add(btnLogout, BorderLayout.SOUTH);

        dialog.add(root);
        dialog.setVisible(true);
    }

    private void addProfilRow(JPanel grid, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(100, 116, 139));

        JLabel v = new JLabel(value, SwingConstants.RIGHT);
        v.setFont(new Font("Segoe UI", Font.BOLD, 12));
        v.setForeground(COLOR_NAVY);

        grid.add(l);
        grid.add(v);
    }

    private class UserAvatarIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR_GOLD);
            g2.fillOval(x + 7, y + 3, 10, 10);
            g2.fillArc(x + 2, y + 14, 20, 14, 0, 180);
            g2.dispose();
        }

        @Override public int getIconWidth() { return 24; }
        @Override public int getIconHeight() { return 24; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TamuFrame("tamu1").setVisible(true));
    }
}