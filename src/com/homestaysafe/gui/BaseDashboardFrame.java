package com.homestaysafe.gui;

import com.homestaysafe.util.UIHelper;
import com.homestaysafe.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

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

    protected abstract String getPortalTitle();
    protected abstract String getBrandText();
    protected abstract String getUserPrefix();
    protected abstract String getProfileTooltip();
    protected abstract String getMenuSectionTitle();
    protected abstract String getProfileDialogTitle();
    protected abstract String getProfileHeader();
    protected abstract String getProfileSubHeader();
    protected abstract String[][] getProfileRows();
    protected abstract void buildMenu();

    protected boolean isSidebarClosedAfterClick() { return true; }

    protected final void initUI() {
        setTitle("HomestaySafe - " + getPortalTitle() + " (" + user.getUsername() + ")");
        setSize(1240, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(COLOR_NAVY);
        sidebarPanel.setPreferredSize(new Dimension(255, 0));
        sidebarPanel.setBorder(new EmptyBorder(10, 12, 16, 12));
        sidebarPanel.setVisible(false);

        JLabel lblMenuSection = new JLabel(getMenuSectionTitle());
        lblMenuSection.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMenuSection.setForeground(COLOR_GOLD);
        lblMenuSection.setBorder(new EmptyBorder(14, 14, 12, 10));
        lblMenuSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(lblMenuSection);

        buildMenu();

        sidebarPanel.add(Box.createVerticalGlue());
        contentPanel.setBackground(COLOR_BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(sidebarPanel, BorderLayout.EAST);

        if (defaultPageKey != null) cardLayout.show(contentPanel, defaultPageKey);
    }

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
        sidebarPanel.add(Box.createVerticalStrut(6));
    }

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
        header.setPreferredSize(new Dimension(1240, 70));
        header.setBorder(new EmptyBorder(0, 28, 0, 28));

        JLabel lblBrand = new JLabel(getBrandText());
        lblBrand.setFont(new Font("Serif", Font.BOLD, 22));
        lblBrand.setForeground(COLOR_GOLD);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        rightHeader.setOpaque(false);

        JLabel lblUser = new JLabel(getUserPrefix() + ": " + user.getUsername());
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(Color.WHITE);

        // Tombol Profil Rounded
        JButton btnProfile = new JButton(new UserAvatarIcon()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
                g2.setColor(COLOR_GOLD);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, 14, 14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnProfile.setPreferredSize(new Dimension(42, 42));
        btnProfile.setFocusPainted(false);
        btnProfile.setContentAreaFilled(false);
        btnProfile.setBorderPainted(false);
        btnProfile.setOpaque(false);
        btnProfile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProfile.setToolTipText(getProfileTooltip());
        btnProfile.addActionListener(e -> showProfileDialog());

        // Tombol Menu Sidebar (Padding dikecilkan supaya ikon ≡ tidak terpotong menjadi ...)
        JButton btnToggleSidebar = UIHelper.createRoundedButton("≡",
                COLOR_GOLD, UIHelper.COLOR_GOLD_HOVER, UIHelper.COLOR_GOLD_PRESS, Color.WHITE, 14, 20);
        btnToggleSidebar.setBorder(new EmptyBorder(0, 0, 2, 0));
        btnToggleSidebar.setPreferredSize(new Dimension(44, 42));
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
        JButton btn = UIHelper.createRoundedButton("  •  " + text,
                COLOR_NAVY, COLOR_SIDEBAR_HOVER, COLOR_NAVY.darker(), Color.WHITE, 12, 13);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 16, 12, 16));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void showProfileDialog() {
        String[][] rows = getProfileRows();

        JDialog dialog = new JDialog(this, getProfileDialogTitle(), true);
        dialog.setSize(430, 255 + rows.length * 32);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel hdrPanel = new JPanel(new GridLayout(2, 1, 0, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_NAVY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        hdrPanel.setOpaque(false);
        hdrPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel lblH1 = new JLabel(getProfileHeader(), SwingConstants.CENTER);
        lblH1.setFont(new Font("Serif", Font.BOLD, 16));
        lblH1.setForeground(COLOR_GOLD);

        JLabel lblH2 = new JLabel(getProfileSubHeader(), SwingConstants.CENTER);
        lblH2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblH2.setForeground(Color.WHITE);

        hdrPanel.add(lblH1);
        hdrPanel.add(lblH2);

        JPanel infoGrid = UIHelper.createRoundedCard(14, 16, 18);
        infoGrid.setBackground(new Color(248, 250, 252));
        infoGrid.setLayout(new GridLayout(rows.length, 2, 8, 10));
        for (String[] row : rows) addProfilRow(infoGrid, row[0], row[1]);

        JButton btnLogout = UIHelper.dangerButton("Logout dari Akun");
        btnLogout.setPreferredSize(new Dimension(0, 44));
        btnLogout.addActionListener(e -> {
            dialog.dispose();
            dispose();
            new LoginFrame().setVisible(true);
        });

        root.add(hdrPanel, BorderLayout.NORTH);
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