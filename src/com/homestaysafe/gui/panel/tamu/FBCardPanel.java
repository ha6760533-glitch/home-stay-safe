package com.homestaysafe.gui.panel.tamu;

import com.homestaysafe.util.UIHelper;
import com.homestaysafe.dao.MenuDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
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
        cartPanel.setPreferredSize(new Dimension(330, 0));

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

            // Foto Makanan/Minuman yang proporsional agar muat 2 kolom tanpa scroll samping
            JLabel lblFoto = new JLabel(UIHelper.getMenuImageIcon(m.getNamaMenu(), 76, 72, 10));
            lblFoto.setPreferredSize(new Dimension(76, 72));

            // Info Menu di Tengah Card (tanpa fixed HTML width agar fleksibel)
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

            // Tombol Tambah di Kanan Card
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
            row.setLayout(new BorderLayout(8, 0));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Lebar HTML dibatasi 145px agar nama panjang turun baris rapi dan tidak terpotong
            JLabel lblName = new JLabel(
                    "<html><body style='width:145px; margin:0; padding:0;'>"
                            + "<b>" + m.getNamaMenu() + "</b><br>"
                            + "<span style='color:#64748b; font-size:10px;'>Qty: x" + qty + "</span>"
                            + "</body></html>"
            );
            lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblName.setVerticalAlignment(SwingConstants.CENTER);

            JLabel lblPrice = new JLabel(String.format("Rp %,.0f", totalItemHarga));
            lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblPrice.setForeground(COLOR_NAVY);
            lblPrice.setHorizontalAlignment(SwingConstants.RIGHT);

            JButton btnDelete = UIHelper.createRoundedButton("×",
                    UIHelper.COLOR_DANGER, UIHelper.COLOR_DANGER_HOVER, UIHelper.COLOR_DANGER.darker(), Color.WHITE, 8, 15);
            btnDelete.setBorder(new EmptyBorder(0, 0, 2, 0));
            btnDelete.setMargin(new Insets(0, 0, 0, 0));
            btnDelete.setPreferredSize(new Dimension(26, 26));
            btnDelete.setToolTipText("Hapus item");
            btnDelete.addActionListener(e -> {
                cartItems.remove(m);
                updateCartUI();
            });

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            rightPanel.setOpaque(false);
            rightPanel.add(lblPrice);
            rightPanel.add(btnDelete);

            JPanel rightWrap = new JPanel(new GridBagLayout());
            rightWrap.setOpaque(false);
            rightWrap.add(rightPanel);

            row.add(lblName, BorderLayout.CENTER);
            row.add(rightWrap, BorderLayout.EAST);

            // Tinggi baris otomatis mengikuti isi teks (tidak lagi terpotong di 52px)
            Dimension pref = row.getPreferredSize();
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Math.max(58, pref.height + 6)));

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