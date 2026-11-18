package com.homestaysafe.gui.panel.tamu;

import com.homestaysafe.dao.MenuDAO;
import com.homestaysafe.dao.ReservasiDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class RiwayatPanel extends JPanel {

    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    private final String usernameTamu;
    private DefaultTableModel model;
    private JLabel lblTotalKamar;
    private JLabel lblTotalFB;
    private JLabel lblGrandTotal;

    private JButton btnFilterSemua;
    private JButton btnFilterKamar;
    private JButton btnFilterFB;
    private String activeFilter = "SEMUA";

    public RiwayatPanel(String usernameTamu) {
        this.usernameTamu = usernameTamu;

        setLayout(new BorderLayout(15, 15));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel topPanel = new JPanel(new BorderLayout(10, 12));
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Riwayat Pemesanan Tamu (" + usernameTamu + ")");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_NAVY);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.setOpaque(false);

        btnFilterSemua = createFilterButton("Semua Transaksi", true);
        btnFilterKamar = createFilterButton("Reservasi Kamar", false);
        btnFilterFB = createFilterButton("Pesanan Makanan (F&B)", false);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setBackground(COLOR_GOLD);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setOpaque(true);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setPreferredSize(new Dimension(90, 32));

        btnFilterSemua.addActionListener(e -> setFilter("SEMUA"));
        btnFilterKamar.addActionListener(e -> setFilter("KAMAR"));
        btnFilterFB.addActionListener(e -> setFilter("FB"));
        btnRefresh.addActionListener(e -> loadData());

        filterBar.add(btnFilterSemua);
        filterBar.add(btnFilterKamar);
        filterBar.add(btnFilterFB);
        filterBar.add(Box.createHorizontalStrut(10));
        filterBar.add(btnRefresh);

        topPanel.add(lblTitle, BorderLayout.NORTH);
        topPanel.add(filterBar, BorderLayout.SOUTH);

        String[] columnNames = {"ID", "Jenis Pesanan", "Detail Pesanan", "Total Harga", "Status"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(241, 245, 249));
        table.setSelectionForeground(COLOR_NAVY);
        table.setFillsViewportHeight(true);
        table.setRowMargin(0);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 44));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value != null ? value.toString() : "");
                label.setOpaque(true);
                label.setBackground(COLOR_NAVY);
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return label;
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                return c;
            }
        };

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(new EmptyBorder(0, 12, 0, 10));
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                return c;
            }
        };

        DefaultTableCellRenderer jenisRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setBorder(new EmptyBorder(0, 12, 0, 10));
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));

                String text = value != null ? value.toString() : "";
                if (text.contains("F&B") || text.contains("Makanan")) {
                    label.setForeground(new Color(180, 83, 9));
                } else {
                    label.setForeground(COLOR_NAVY);
                }

                if (!isSelected) {
                    label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                return label;
            }
        };

        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setBorder(new EmptyBorder(0, 8, 0, 8));

                String status = value != null ? value.toString().toLowerCase() : "";
                if (status.contains("selesai") || status.contains("disetujui") || status.contains("check-in")) {
                    label.setForeground(new Color(5, 150, 105));
                } else if (status.contains("terkirim") || status.contains("diproses")) {
                    label.setForeground(new Color(37, 99, 235));
                } else if (status.contains("pending") || status.contains("menunggu")) {
                    label.setForeground(new Color(217, 119, 6));
                } else if (status.contains("ditolak") || status.contains("dibatalkan")) {
                    label.setForeground(new Color(220, 38, 38));
                } else {
                    label.setForeground(COLOR_NAVY);
                }

                if (!isSelected) {
                    label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                return label;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(jenisRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(statusRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(0).setMaxWidth(85);
        table.getColumnModel().getColumn(1).setPreferredWidth(155);
        table.getColumnModel().getColumn(2).setPreferredWidth(320);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(135);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryPanel.setOpaque(false);

        lblTotalKamar = new JLabel("Rp 0");
        lblTotalFB = new JLabel("Rp 0");
        lblGrandTotal = new JLabel("Rp 0");

        summaryPanel.add(createSummaryCard("Total Tagihan Kamar", lblTotalKamar, COLOR_NAVY));
        summaryPanel.add(createSummaryCard("Total Tagihan Makanan (F&B)", lblTotalFB, new Color(180, 83, 9)));
        summaryPanel.add(createSummaryCard("Akumulasi Seluruh Transaksi", lblGrandTotal, new Color(5, 150, 105)));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(summaryPanel, BorderLayout.SOUTH);

        loadData();
    }

    private JButton createFilterButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(165, 32));
        styleFilterButton(btn, active);
        return btn;
    }

    private void styleFilterButton(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(COLOR_NAVY);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(COLOR_NAVY);
        }
    }

    private void setFilter(String filter) {
        this.activeFilter = filter;
        styleFilterButton(btnFilterSemua, "SEMUA".equals(filter));
        styleFilterButton(btnFilterKamar, "KAMAR".equals(filter));
        styleFilterButton(btnFilterFB, "FB".equals(filter));
        loadData();
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new GridLayout(2, 1, 2, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setForeground(accentColor);

        card.add(lblTitle);
        card.add(valueLabel);
        return card;
    }

    private void loadData() {
        model.setRowCount(0);

        double totalKamar = 0;
        double totalFB = 0;

        MenuDAO menuDAO = new MenuDAO();
        List<MenuDAO.PesananFBDetail> listFB = menuDAO.getRiwayatPesananByUsername(usernameTamu);
        for (MenuDAO.PesananFBDetail fb : listFB) {
            totalFB += fb.getSubtotal();
        }

        ReservasiDAO reservasiDAO = new ReservasiDAO();
        List<ReservasiDAO.RiwayatItem> listKamar = reservasiDAO.getRiwayatByUsername(usernameTamu);

        for (ReservasiDAO.RiwayatItem r : listKamar) {
            String jenis = r.getJenisPesanan() != null ? r.getJenisPesanan().toLowerCase() : "";
            // Lewati baris "Food & Beverage" yang ikut terbawa dari ReservasiDAO
            if (jenis.contains("food") || jenis.contains("beverage") || jenis.contains("f&b") || jenis.contains("makanan")) {
                continue;
            }

            if (!r.getStatus().equalsIgnoreCase("Ditolak") && !r.getStatus().equalsIgnoreCase("Dibatalkan")) {
                totalKamar += r.getTotal();
            }
            if ("SEMUA".equals(activeFilter) || "KAMAR".equals(activeFilter)) {
                model.addRow(new Object[]{
                        "RSV-" + r.getIdTransaksi(),
                        r.getJenisPesanan(),
                        r.getDetail(),
                        String.format("Rp %,.0f", r.getTotal()),
                        r.getStatus()
                });
            }
        }

        for (MenuDAO.PesananFBDetail fb : listFB) {
            if ("SEMUA".equals(activeFilter) || "FB".equals(activeFilter)) {
                String detailFB = fb.getNamaMenu() + " (x" + fb.getJumlah() + ") - Kamar " + fb.getNoKamar();
                model.addRow(new Object[]{
                        "F&B-" + fb.getIdPesanan(),
                        "Makanan & Minuman (F&B)",
                        detailFB,
                        String.format("Rp %,.0f", fb.getSubtotal()),
                        fb.getStatusPesanan()
                });
            }
        }

        lblTotalKamar.setText(String.format("Rp %,.0f", totalKamar));
        lblTotalFB.setText(String.format("Rp %,.0f", totalFB));
        lblGrandTotal.setText(String.format("Rp %,.0f", (totalKamar + totalFB)));
    }
}