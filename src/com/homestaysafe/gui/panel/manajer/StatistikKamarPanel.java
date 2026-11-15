package com.homestaysafe.gui.panel.manajer;

import com.homestaysafe.dao.ManajerDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StatistikKamarPanel extends JPanel {

    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    private final ManajerDAO dao = new ManajerDAO();
    private final DefaultTableModel model;
    private final JLabel lblKamarJuara = new JLabel("-");

    public StatistikKamarPanel() {
        setLayout(new BorderLayout(15, 16));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel topHeader = new JPanel(new BorderLayout(0, 10));
        topHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Statistik & Ranking Kamar Terfavorit");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_NAVY);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter Waktu:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFilter.setForeground(COLOR_NAVY);

        JComboBox<String> cbWaktu = new JComboBox<>(new String[]{"Semua Waktu", "7 Hari Terakhir", "Bulan Ini", "Tahun Ini"});
        cbWaktu.setPreferredSize(new Dimension(175, 36));
        cbWaktu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbWaktu.setBackground(Color.WHITE);

        JButton btnAnalisis = new JButton("Analisis Statistik");
        btnAnalisis.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAnalisis.setBackground(COLOR_GOLD);
        btnAnalisis.setForeground(Color.WHITE);
        btnAnalisis.setPreferredSize(new Dimension(150, 36));
        btnAnalisis.setFocusPainted(false);
        btnAnalisis.setOpaque(true);
        btnAnalisis.setBorderPainted(false);
        btnAnalisis.setCursor(new Cursor(Cursor.HAND_CURSOR));

        filterPanel.add(lblFilter);
        filterPanel.add(cbWaktu);
        filterPanel.add(btnAnalisis);

        topHeader.add(lblTitle, BorderLayout.NORTH);
        topHeader.add(filterPanel, BorderLayout.SOUTH);

        JPanel highlightCard = new JPanel(new GridLayout(2, 1, 2, 4));
        highlightCard.setBackground(COLOR_NAVY);
        highlightCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_GOLD, 2, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel lblBadge = new JLabel("KAMAR TERFAVORIT PERINGKAT #1 (FREKUENSI TERTINGGI)");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setForeground(COLOR_GOLD);

        lblKamarJuara.setFont(new Font("Serif", Font.BOLD, 18));
        lblKamarJuara.setForeground(Color.WHITE);

        highlightCard.add(lblBadge);
        highlightCard.add(lblKamarJuara);

        String[] cols = {"Peringkat", "Nomor Kamar", "Tipe Kamar", "Frekuensi Dipesan", "Kontribusi Pendapatan", "Kategori Popularitas"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));
        table.setFillsViewportHeight(true);

        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(val != null ? val.toString() : "", SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBackground(COLOR_NAVY);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                return lbl;
            }
        });

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (!sel) lbl.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                lbl.setBorder(new EmptyBorder(0, 10, 0, 14));
                lbl.setHorizontalAlignment(c == 4 ? SwingConstants.RIGHT : SwingConstants.CENTER);

                if (c == 0) {
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    lbl.setForeground(r == 0 ? new Color(180, 83, 9) : COLOR_NAVY);
                } else if (c == 4) {
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    lbl.setForeground(new Color(5, 150, 105));
                } else if (c == 5) {
                    String kat = val != null ? val.toString() : "";
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    lbl.setForeground("Sangat Diminati".equals(kat) ? new Color(5, 150, 105)
                            : ("Normal".equals(kat) ? new Color(37, 99, 235) : new Color(100, 116, 139)));
                } else {
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    lbl.setForeground(COLOR_NAVY);
                }
                return lbl;
            }
        };
        for (int i = 0; i < cols.length; i++) table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        scrollTable.getViewport().setBackground(Color.WHITE);

        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(highlightCard, BorderLayout.NORTH);
        centerPanel.add(scrollTable, BorderLayout.CENTER);

        add(topHeader, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        btnAnalisis.addActionListener(e -> loadStatistik(cbWaktu.getSelectedItem().toString()));
        loadStatistik("Semua Waktu");
    }

    private void loadStatistik(String filterWaktu) {
        model.setRowCount(0);
        List<ManajerDAO.StatistikKamarItem> list = dao.getStatistikKamarTerfavorit(filterWaktu);

        if (!list.isEmpty() && list.get(0).getFrekuensiBooking() > 0) {
            ManajerDAO.StatistikKamarItem top = list.get(0);
            lblKamarJuara.setText("Kamar No. " + top.getNoKamar() + " (" + top.getTipeKamar() + ")  •  Dipesan " + top.getFrekuensiBooking() + " Kali  •  Omzet: " + String.format("Rp %,.0f", top.getTotalOmzet()));
        } else {
            lblKamarJuara.setText("Belum ada pemesanan tercatat pada filter waktu: " + filterWaktu);
        }

        for (ManajerDAO.StatistikKamarItem item : list) {
            String kategori = item.getFrekuensiBooking() >= 3 ? "Sangat Diminati"
                    : (item.getFrekuensiBooking() > 0 ? "Normal" : "Belum Dipesan");

            model.addRow(new Object[]{
                    "#" + item.getPeringkat(),
                    "Kamar No. " + item.getNoKamar(),
                    item.getTipeKamar(),
                    item.getFrekuensiBooking() + " Kali",
                    String.format("Rp %,.0f", item.getTotalOmzet()),
                    kategori
            });
        }
    }
}