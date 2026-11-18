package com.homestaysafe.gui.panel.tamu;

import com.homestaysafe.dao.MenuDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FBCardPanel extends JPanel {

    private final String usernameTamu;
    private final Map<MenuDAO.Menu, Integer> cartItems = new HashMap<>();

    private JLabel lblCartSummaryCount;
    private JLabel lblCartGrandTotal;
    private JPanel cartListPanel;

    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    public FBCardPanel(String usernameTamu) {
        this.usernameTamu = usernameTamu;

        setLayout(new BorderLayout(15, 15));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Pemesanan Makanan & Minuman (F&B)");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_NAVY);

        JPanel gridMenuPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        gridMenuPanel.setBackground(COLOR_BG);

        MenuDAO menuDAO = new MenuDAO();
        List<MenuDAO.Menu> listMenu = menuDAO.getAllMenu();

        for (MenuDAO.Menu m : listMenu) {
            JPanel card = new JPanel(new BorderLayout(10, 10));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(12, 15, 12, 15)
            ));

            JLabel lblNama = new JLabel(m.getNamaMenu());
            lblNama.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblNama.setForeground(COLOR_NAVY);

            JLabel lblHarga = new JLabel(String.format("Rp %,.0f", m.getHarga()));
            lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblHarga.setForeground(COLOR_GOLD);

            JPanel infoPanel = new JPanel(new GridLayout(2, 1, 2, 2));
            infoPanel.setOpaque(false);
            infoPanel.add(lblNama);
            infoPanel.add(lblHarga);

            JButton btnAdd = new JButton("+ Tambah");
            btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnAdd.setBackground(COLOR_NAVY);
            btnAdd.setForeground(Color.WHITE);
            btnAdd.setFocusPainted(false);
            btnAdd.setBorderPainted(false);
            btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAdd.addActionListener(e -> {
                cartItems.put(m, cartItems.getOrDefault(m, 0) + 1);
                updateCartUI();
            });

            card.add(infoPanel, BorderLayout.CENTER);
            card.add(btnAdd, BorderLayout.EAST);
            gridMenuPanel.add(card);
        }

        JScrollPane scrollMenu = new JScrollPane(gridMenuPanel);
        scrollMenu.setBorder(null);

        JPanel cartPanel = new JPanel(new BorderLayout(10, 10));
        cartPanel.setBackground(Color.WHITE);
        cartPanel.setPreferredSize(new Dimension(320, 0));
        cartPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblCartHeader = new JLabel("Keranjang Pesanan");
        lblCartHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCartHeader.setForeground(COLOR_NAVY);

        cartListPanel = new JPanel();
        cartListPanel.setLayout(new BoxLayout(cartListPanel, BoxLayout.Y_AXIS));
        cartListPanel.setBackground(Color.WHITE);

        JScrollPane scrollCart = new JScrollPane(cartListPanel);
        scrollCart.setBorder(null);

        JPanel cartBottomPanel = new JPanel(new GridLayout(3, 1, 6, 6));
        cartBottomPanel.setOpaque(false);

        lblCartSummaryCount = new JLabel("Jumlah Item: 0");
        lblCartGrandTotal = new JLabel("Total Tagihan: Rp 0");
        lblCartGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCartGrandTotal.setForeground(COLOR_GOLD);

        JButton btnCheckout = new JButton("Checkout & Kirim ke Dapur");
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCheckout.setBackground(COLOR_GOLD);
        btnCheckout.setForeground(Color.WHITE);
        btnCheckout.setFocusPainted(false);
        btnCheckout.setBorderPainted(false);
        btnCheckout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCheckout.addActionListener(e -> processCartCheckout());

        cartBottomPanel.add(lblCartSummaryCount);
        cartBottomPanel.add(lblCartGrandTotal);
        cartBottomPanel.add(btnCheckout);

        cartPanel.add(lblCartHeader, BorderLayout.NORTH);
        cartPanel.add(scrollCart, BorderLayout.CENTER);
        cartPanel.add(cartBottomPanel, BorderLayout.SOUTH);

        add(lblTitle, BorderLayout.NORTH);
        add(scrollMenu, BorderLayout.CENTER);
        add(cartPanel, BorderLayout.EAST);

        updateCartUI();
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

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(new Color(248, 250, 252));
            row.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(226, 232, 240), 1, true),
                    new EmptyBorder(8, 10, 8, 8)
            ));
            row.setMaximumSize(new Dimension(300, 48));

            JLabel lblName = new JLabel("<html><b>" + m.getNamaMenu() + "</b><br><span style='color:#64748b; font-size:10px;'>x" + qty + "</span></html>");
            lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            JLabel lblPrice = new JLabel(String.format("Rp %,.0f", totalItemHarga));
            lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblPrice.setForeground(COLOR_NAVY);
            lblPrice.setHorizontalAlignment(SwingConstants.RIGHT);

            JButton btnDelete = new JButton("X");
            btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnDelete.setForeground(Color.WHITE);
            btnDelete.setBackground(new Color(239, 68, 68));
            btnDelete.setMargin(new Insets(0, 0, 0, 0));
            btnDelete.setFocusPainted(false);
            btnDelete.setBorderPainted(false);
            btnDelete.setOpaque(true);
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDelete.setPreferredSize(new Dimension(28, 28));
            btnDelete.setToolTipText("Hapus item");
            btnDelete.addActionListener(e -> {
                cartItems.remove(m);
                updateCartUI();
            });

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rightPanel.setOpaque(false);
            rightPanel.add(lblPrice);
            rightPanel.add(btnDelete);

            row.add(lblName, BorderLayout.CENTER);
            row.add(rightPanel, BorderLayout.EAST);

            cartListPanel.add(row);
            cartListPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        }

        lblCartSummaryCount.setText("Jumlah Item: " + totalQty);
        lblCartGrandTotal.setText(String.format("Total Tagihan: Rp %,.0f", grandTotal));

        cartListPanel.revalidate();
        cartListPanel.repaint();
    }

    private void processCartCheckout() {
        MenuDAO menuDAO = new MenuDAO();
        if (!menuDAO.hasActiveReservation(usernameTamu)) {
            JOptionPane.showMessageDialog(this, "Pemesanan makanan ditolak!\nAnda belum memiliki reservasi kamar yang disetujui / aktif.", "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
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