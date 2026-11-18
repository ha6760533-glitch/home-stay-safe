package com.homestaysafe.gui;

import com.homestaysafe.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Abstract class dasar untuk semua dashboard (Tamu, Resepsionis, Manajer).
 *
 * Pola Template Method: kerangka tampilan (header, sidebar, dialog profil, logout)
 * ditulis SEKALI di sini, sedangkan bagian yang berbeda per role (judul, menu, data
 * profil) dipaksakan lewat method abstract yang di-override subclass.
 *
 * Cara pakai di subclass: panggil {@link #initUI()} di akhir constructor.
 */
public abstract class BaseDashboardFrame extends JFrame {

    protected static final Color COLOR_NAVY = new Color(15, 23, 42);
    protected static final Color COLOR_SIDEBAR_HOVER = new Color(30, 41, 59);
    protected static final Color COLOR_GOLD = new Color(197, 168, 128);
    protected static final Color COLOR_BG = new Color(245, 247, 250);

    protected final User user;
    protected final CardLayout cardLayout = new CardLayout();
    protected final JPanel contentPanel = new JPanel(cardLayout);

    private final JPanel sidebarPanel = new JPanel();
    private final Map<String, JPanel> pages = new HashMap<>();
    private final Map<String, Supplier<JPanel>> factories = new HashMap<>();
    private String defaultPageKey;
    private boolean isSidebarOpen = false;

    protected BaseDashboardFrame(User user) {
        this.user = user;
    }

    // ===== Bagian yang WAJIB berbeda per role (abstract) =====

    /** Contoh: "Portal Tamu" → judul jendela "HomestaySafe - Portal Tamu (username)". */
    protected abstract String getPortalTitle();
    protected abstract String getBrandText();
    /** Contoh: "Tamu" → label header "Tamu: username". */
    protected abstract String getUserPrefix();
    protected abstract String getProfileTooltip();
    protected abstract String getMenuSectionTitle();
    protected abstract String getProfileDialogTitle();
    protected abstract String getProfileHeader();
    protected abstract String getProfileSubHeader();
    /** Pasangan {label, nilai} untuk ditampilkan di dialog profil. */
    protected abstract String[][] getProfileRows();
    /** Daftarkan menu sidebar dengan memanggil {@link #addMenuItem}. */
    protected abstract void buildMenu();

    /** Hook opsional: apakah sidebar ditutup otomatis setelah menu diklik. */
    protected boolean isSidebarClosedAfterClick() { return true; }

    // ===== Kerangka tampilan (dipakai bersama) =====

    protected final void initUI() {
        setTitle("HomestaySafe - " + getPortalTitle() + " (" + user.getUsername() + ")");
        setSize(1240, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(COLOR_NAVY);
        sidebarPanel.setPreferredSize(new Dimension(250, 0));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, COLOR_SIDEBAR_HOVER));
        sidebarPanel.setVisible(false);

        JLabel lblMenuSection = new JLabel(getMenuSectionTitle());
        lblMenuSection.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMenuSection.setForeground(COLOR_GOLD);
        lblMenuSection.setBorder(new EmptyBorder(22, 22, 10, 15));
        lblMenuSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(lblMenuSection);

        buildMenu(); // diisi oleh subclass

        sidebarPanel.add(Box.createVerticalGlue());
        contentPanel.setBackground(COLOR_BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(sidebarPanel, BorderLayout.EAST);

        if (defaultPageKey != null) cardLayout.show(contentPanel, defaultPageKey);
    }

    /**
     * Menambah satu menu sidebar sekaligus mendaftarkan halamannya.
     * Halaman pertama yang didaftarkan menjadi halaman awal.
     */
    protected final void addMenuItem(String text, String pageKey, Supplier<JPanel> panelFactory) {
        factories.put(pageKey, panelFactory);
        JPanel initial = panelFactory.get();
        pages.put(pageKey, initial);
        contentPanel.add(initial, pageKey);
        if (defaultPageKey == null) defaultPageKey = pageKey;

        sidebarPanel.add(createSidebarButton(text, () -> {
            showPage(pageKey);
            if (isSidebarClosedAfterClick()) toggleSidebar();
        }));
    }

    /** Membuat ulang halaman (agar datanya segar) lalu menampilkannya. */
    private void showPage(String pageKey) {
        JPanel old = pages.get(pageKey);
        if (old != null) contentPanel.remove(old);
        JPanel fresh = factories.get(pageKey).get();
        pages.put(pageKey, fresh);
        contentPanel.add(fresh, pageKey);
        cardLayout.show(contentPanel, pageKey);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_NAVY);
        header.setPreferredSize(new Dimension(1240, 68));
        header.setBorder(new EmptyBorder(0, 25, 0, 25));

        JLabel lblBrand = new JLabel(getBrandText());
        lblBrand.setFont(new Font("Serif", Font.BOLD, 22));
        lblBrand.setForeground(COLOR_GOLD);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 13));
        rightHeader.setOpaque(false);

        JLabel lblUser = new JLabel(getUserPrefix() + ": " + user.getUsername());
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(Color.WHITE);

        JButton btnProfile = new JButton(new UserAvatarIcon());
        btnProfile.setBackground(Color.WHITE);
        btnProfile.setPreferredSize(new Dimension(40, 40));
        btnProfile.setFocusPainted(false);
        btnProfile.setOpaque(true);
        btnProfile.setBorder(new LineBorder(COLOR_GOLD, 1, true));
        btnProfile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProfile.setToolTipText(getProfileTooltip());
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
        return header;
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

    private void showProfileDialog() {
        String[][] rows = getProfileRows();

        JDialog dialog = new JDialog(this, getProfileDialogTitle(), true);
        dialog.setSize(420, 240 + rows.length * 30);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel hdr = new JLabel("<html><center><b style='color:#c5a880;font-size:14px;'>" + getProfileHeader() + "</b><br>" +
                "<span style='color:white;font-size:11px;'>" + getProfileSubHeader() + "</span></center></html>", SwingConstants.CENTER);
        hdr.setOpaque(true);
        hdr.setBackground(COLOR_NAVY);
        hdr.setBorder(new EmptyBorder(14, 10, 14, 10));

        JPanel infoGrid = new JPanel(new GridLayout(rows.length, 2, 8, 10));
        infoGrid.setBackground(new Color(248, 250, 252));
        infoGrid.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        for (String[] row : rows) addProfilRow(infoGrid, row[0], row[1]);

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

    /** Helper untuk subclass: nilai null/kosong ditampilkan sebagai "-". */
    protected static String orDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    private static class UserAvatarIcon implements Icon {
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
}
