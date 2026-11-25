package com.homestaysafe.gui.panel.tamu;

import com.homestaysafe.util.UIHelper;
import com.homestaysafe.dao.MenuDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FBCardPanel extends JPanel {

    private final String usernameTamu;
    private final Map<MenuDAO.Menu, Integer> cartItems = new HashMap<>();
    private final List<MenuDAO.Menu> allMenuList;

    private final JPanel gridMenuPanel;
    private final JComboBox<String> cbKategori;
    private final JTextField txtCariMenu;

    private JLabel lblCartSummaryCount;
    private JLabel lblCartGrandTotal;
    private JPanel cartListPanel;

    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);

    public FBCardPanel(String usernameTamu) {
        this.usernameTamu = usernameTamu;

        setLayout(new BorderLayout(14, 14));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(16, 18, 16, 18));

        // Header + Filter Kategori & Pencarian
        JPanel topBar = new JPanel(new BorderLayout(10, 8));
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Pemesanan Makanan & Minuman (F&B)");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 21));
        lblTitle.setForeground(COLOR_NAVY);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterBar.setOpaque(false);

        cbKategori = new JComboBox<>(new String[]{
                "Semua Kategori", "Coffee", "Cocktail", "Non-Coffee", "Jus",
                "Makanan Pembuka", "Makanan Utama", "Pencuci Mulut", "Cemilan",
                "Minuman Khas Hotel", "Makanan Khas Hotel"
        });
        cbKategori.setPreferredSize(new Dimension(165, 34));
        UIHelper.styleField(cbKategori);

        txtCariMenu = new JTextField(14);
        txtCariMenu.setPreferredSize(new Dimension(165, 34));
        txtCariMenu.setToolTipText("Cari nama makanan/minuman...");
        UIHelper.styleField(txtCariMenu);

        JButton btnCari = UIHelper.primaryButton("Cari");
        btnCari.setPreferredSize(new Dimension(72, 34));
        btnCari.setBorder(new EmptyBorder(6, 12, 6, 12));

        filterBar.add(new JLabel("Kategori:"));
        filterBar.add(cbKategori);
        filterBar.add(txtCariMenu);
        filterBar.add(btnCari);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(filterBar, BorderLayout.EAST);

        MenuDAO menuDAO = new MenuDAO();
        allMenuList = menuDAO.getAllMenu();

        // Wrapper yang mengikuti lebar viewport agar tidak melebar ke kanan
        JPanel menuWrapper = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                if (getParent() != null) {
                    d.width = getParent().getWidth();
                }
                return d;
            }
        };
        menuWrapper.setBackground(COLOR_BG);

        gridMenuPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        gridMenuPanel.setBackground(COLOR_BG);
        menuWrapper.add(gridMenuPanel, BorderLayout.NORTH);

        Runnable applyFilter = () -> renderMenuCards(
                cbKategori.getSelectedItem() != null ? cbKategori.getSelectedItem().toString() : "Semua Kategori",
                txtCariMenu.getText()
        );
        cbKategori.addActionListener(e -> applyFilter.run());
        btnCari.addActionListener(e -> applyFilter.run());
        txtCariMenu.addActionListener(e -> applyFilter.run());

        renderMenuCards("Semua Kategori", "");

        JScrollPane scrollMenu = new JScrollPane(menuWrapper);
        scrollMenu.setBorder(null);
        scrollMenu.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollMenu.getViewport().setBackground(COLOR_BG);
        scrollMenu.getVerticalScrollBar().setUnitIncrement(16);

        // Panel Keranjang Pesanan di Kanan
        JPanel cartPanel = UIHelper.createRoundedCard(16, 16, 16);
        cartPanel.setLayout(new BorderLayout(10, 10));
        cartPanel.setPreferredSize(new Dimension(350, 0));

        JLabel lblCartHeader = new JLabel("Keranjang Pesanan");
        lblCartHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCartHeader.setForeground(COLOR_NAVY);

        cartListPanel = new JPanel();
        cartListPanel.setLayout(new BoxLayout(cartListPanel, BoxLayout.Y_AXIS));
        cartListPanel.setBackground(Color.WHITE);

        JScrollPane scrollCart = new JScrollPane(cartListPanel);
        scrollCart.setBorder(null);
        scrollCart.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollCart.getViewport().setBackground(Color.WHITE);
        scrollCart.getVerticalScrollBar().setUnitIncrement(12);

        JPanel cartBottomPanel = new JPanel(new GridLayout(3, 1, 6, 6));
        cartBottomPanel.setOpaque(false);

        lblCartSummaryCount = new JLabel("Jumlah Item: 0");
        lblCartGrandTotal = new JLabel("Total Tagihan: Rp 0");
        lblCartGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCartGrandTotal.setForeground(COLOR_GOLD);

        JButton btnCheckout = UIHelper.goldButton("Checkout & Kirim ke Dapur");
        btnCheckout.setPreferredSize(new Dimension(0, 40));
        btnCheckout.addActionListener(e -> processCartCheckout());

        cartBottomPanel.add(lblCartSummaryCount);
        cartBottomPanel.add(lblCartGrandTotal);
        cartBottomPanel.add(btnCheckout);

        cartPanel.add(lblCartHeader, BorderLayout.NORTH);
        cartPanel.add(scrollCart, BorderLayout.CENTER);
        cartPanel.add(cartBottomPanel, BorderLayout.SOUTH);

        add(topBar, BorderLayout.NORTH);
        add(scrollMenu, BorderLayout.CENTER);
        add(cartPanel, BorderLayout.EAST);

        updateCartUI();
    }

    private void renderMenuCards(String kategoriFilter, String keyword) {
        gridMenuPanel.removeAll();
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();

        for (MenuDAO.Menu m : allMenuList) {
            String kat = m.getKategori() != null ? m.getKategori() : "Makanan Utama";
            boolean matchKat = "Semua Kategori".equalsIgnoreCase(kategoriFilter) || kat.equalsIgnoreCase(kategoriFilter);
            boolean matchKw = kw.isEmpty() || m.getNamaMenu().toLowerCase().contains(kw) || kat.toLowerCase().contains(kw);
            if (!matchKat || !matchKw) continue;

            JPanel card = UIHelper.createRoundedCard(14, 10, 12);
            card.setLayout(new BorderLayout(10, 0));

            JLabel lblFoto = new JLabel(UIHelper.getMenuImageIcon(m.getNamaMenu(), 76, 72, 10));
            lblFoto.setPreferredSize(new Dimension(76, 72));

            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            JLabel lblKatBadge = new JLabel(kat.toUpperCase());
            lblKatBadge.setFont(new Font("Segoe UI", Font.BOLD, 9));
            lblKatBadge.setForeground(new Color(100, 116, 139));
            lblKatBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblNama = new JLabel("<html>" + m.getNamaMenu() + "</html>");
            lblNama.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblNama.setForeground(COLOR_NAVY);
            lblNama.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblNama.setBorder(new EmptyBorder(2, 0, 3, 0));

            JLabel lblHarga = new JLabel(String.format("Rp %,.0f", m.getHarga()));
            lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblHarga.setForeground(COLOR_GOLD);
            lblHarga.setAlignmentX(Component.LEFT_ALIGNMENT);

            infoPanel.add(lblKatBadge);
            infoPanel.add(lblNama);
            infoPanel.add(lblHarga);

            JPanel actionWrap = new JPanel(new GridBagLayout());
            actionWrap.setOpaque(false);

            JButton btnAdd = m.isAvailable()
                    ? UIHelper.primaryButton("+ Tambah")
                    : UIHelper.softButton("Habis");
            btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btnAdd.setBorder(new EmptyBorder(6, 10, 6, 10));
            btnAdd.setEnabled(m.isAvailable());
            if (m.isAvailable()) {
                btnAdd.addActionListener(e -> {
                    cartItems.put(m, cartItems.getOrDefault(m, 0) + 1);
                    updateCartUI();
                });
            }
            actionWrap.add(btnAdd);

            card.add(lblFoto, BorderLayout.WEST);
            card.add(infoPanel, BorderLayout.CENTER);
            card.add(actionWrap, BorderLayout.EAST);
            gridMenuPanel.add(card);
        }

        gridMenuPanel.revalidate();
        gridMenuPanel.repaint();
    }

    private JButton createQtyButton(String symbol, Color normalBg, Color hoverBg, Color pressedBg, Color fgColor, String tooltip, Runnable onClick) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                Color bg = getModel().isPressed()
                        ? pressedBg
                        : (getModel().isRollover() ? hoverBg : normalBg);

                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 6, 6));

                g2.setColor(fgColor);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                if ("-".equals(symbol)) {
                    g2.drawLine(6, h / 2, w - 6, h / 2);
                } else if ("+".equals(symbol)) {
                    g2.drawLine(6, h / 2, w - 6, h / 2);
                    g2.drawLine(w / 2, 6, w / 2, h - 6);
                } else {
                    int pad = 6;
                    g2.drawLine(pad, pad, w - pad, h - pad);
                    g2.drawLine(w - pad, pad, pad, h - pad);
                }

                g2.dispose();
            }
        };
        Dimension size = new Dimension(22, 22);
        btn.setPreferredSize(size);
        btn.setMinimumSize(size);
        btn.setMaximumSize(size);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);
        btn.addActionListener(e -> onClick.run());
        return btn;
    }

    private void updateCartUI() {
        if (cartListPanel == null) return;
        cartListPanel.removeAll();
        double grandTotal = 0;
        int totalQty = 0;

        for (Map.Entry<MenuDAO.Menu, Integer> entry : cartItems.entrySet()) {
            MenuDAO.Menu m = entry.getKey();
            int qty = entry.getValue();
            double totalItemHarga = m.getHarga() * qty;

            grandTotal += totalItemHarga;
            totalQty += qty;

            JPanel row = UIHelper.createRoundedCard(12, 10, 10);
            row.setBackground(new Color(248, 250, 252));
            row.setLayout(new BorderLayout(6, 6));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Bagian atas baris: Nama menu + harga subtotal + tombol hapus item (×)
            JLabel lblName = new JLabel(
                    "<html><body style='width:115px; margin:0; padding:0;'>"
                            + "<b>" + m.getNamaMenu() + "</b>"
                            + "</body></html>"
            );
            lblName.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblName.setForeground(COLOR_NAVY);

            JLabel lblPrice = new JLabel(String.format("Rp %,.0f", totalItemHarga));
            lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblPrice.setForeground(COLOR_NAVY);

            JButton btnDelete = createQtyButton(
                    "×",
                    UIHelper.COLOR_DANGER,
                    UIHelper.COLOR_DANGER_HOVER,
                    UIHelper.COLOR_DANGER.darker(),
                    Color.WHITE,
                    "Hapus item dari keranjang",
                    () -> {
                        cartItems.remove(m);
                        updateCartUI();
                    }
            );

            JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            topRight.setOpaque(false);
            topRight.add(lblPrice);
            topRight.add(btnDelete);

            JPanel topRow = new JPanel(new BorderLayout(4, 0));
            topRow.setOpaque(false);
            topRow.add(lblName, BorderLayout.WEST);
            topRow.add(topRight, BorderLayout.EAST);

            // Bagian bawah baris: Kontrol jumlah (- / qty / +)
            JButton btnMinus = createQtyButton(
                    "-",
                    new Color(226, 232, 240),
                    new Color(203, 213, 225),
                    new Color(148, 163, 184),
                    COLOR_NAVY,
                    "Kurangi 1 porsi",
                    () -> {
                        int current = cartItems.getOrDefault(m, 0);
                        if (current > 1) {
                            cartItems.put(m, current - 1);
                        } else {
                            cartItems.remove(m);
                        }
                        updateCartUI();
                    }
            );

            JLabel lblQty = new JLabel(String.valueOf(qty), SwingConstants.CENTER);
            lblQty.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblQty.setForeground(COLOR_NAVY);
            lblQty.setPreferredSize(new Dimension(24, 22));

            JButton btnPlus = createQtyButton(
                    "+",
                    COLOR_NAVY,
                    new Color(30, 41, 59),
                    COLOR_NAVY.darker(),
                    Color.WHITE,
                    "Tambah 1 porsi",
                    () -> {
                        cartItems.put(m, cartItems.getOrDefault(m, 0) + 1);
                        updateCartUI();
                    }
            );

            JPanel qtyControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            qtyControls.setOpaque(false);
            qtyControls.add(btnMinus);
            qtyControls.add(lblQty);
            qtyControls.add(btnPlus);

            row.add(topRow, BorderLayout.NORTH);
            row.add(qtyControls, BorderLayout.SOUTH);

            Dimension pref = row.getPreferredSize();
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Math.max(66, pref.height + 6)));

            cartListPanel.add(row);
            cartListPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        lblCartSummaryCount.setText("Jumlah Item: " + totalQty);
        lblCartGrandTotal.setText(String.format("Total Tagihan: Rp %,.0f", grandTotal));

        cartListPanel.revalidate();
        cartListPanel.repaint();
    }

    private void processCartCheckout() {
        MenuDAO menuDAO = new MenuDAO();
        if (!menuDAO.hasActiveReservation(usernameTamu)) {
            JOptionPane.showMessageDialog(this,
                    "Pemesanan makanan belum tersedia!\nLayanan F&B hanya dapat dipesan setelah Anda melakukan Check-In dan menerima kunci kamar dari Resepsionis.",
                    "Belum Check-In", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang belanja makanan masih kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int itemBerhasil = 0;
        double grandTotal = 0;
        StringBuilder detail = new StringBuilder();
        String lastError = "";

        for (Map.Entry<MenuDAO.Menu, Integer> entry : cartItems.entrySet()) {
            MenuDAO.Menu menu = entry.getKey();
            int qty = entry.getValue();
            double subtotal = menu.getHarga() * qty;

            String hasil = menuDAO.simpanPesananFB(usernameTamu, menu.getIdMenu(), qty, subtotal, "Tagihan Kamar (Room Charge)");
            if ("OK".equals(hasil)) {
                itemBerhasil++;
                grandTotal += subtotal;
                detail.append("• ").append(menu.getNamaMenu()).append(" (x").append(qty).append(")\n");
            } else {
                lastError = hasil;
            }
        }

        if (itemBerhasil > 0) {
            JOptionPane.showMessageDialog(this, "Berhasil mengirim " + itemBerhasil + " item makanan ke Dapur!\n\nDetail:\n" + detail + "\nTotal Tagihan: Rp " + String.format("%,.0f", grandTotal), "Pesanan Terkirim ke Dapur", JOptionPane.INFORMATION_MESSAGE);
            cartItems.clear();
            updateCartUI();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan pesanan makanan:\n" + lastError, "Error Database F&B", JOptionPane.ERROR_MESSAGE);
        }
    }
}