package com.homestaysafe.gui.panel.manajer;

import com.homestaysafe.util.UIHelper;
import com.homestaysafe.dao.ManajerDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LaporanPendapatanPanel extends JPanel {

    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    private final ManajerDAO dao = new ManajerDAO();
    private final DefaultTableModel model;
    private final JLabel lblTotalPendapatanVal = new JLabel("Rp 0");
    private final JLabel lblTotalTransaksiVal = new JLabel("0 Transaksi");
    private final JLabel lblRataRataVal = new JLabel("Rp 0");
    private List<ManajerDAO.PendapatanItem> dataGrafik = new ArrayList<>();
    private final BarChartPanel chartPanel = new BarChartPanel();

    public LaporanPendapatanPanel() {
        setLayout(new BorderLayout(15, 16));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel topHeader = new JPanel(new BorderLayout(0, 10));
        topHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Laporan & Grafik Pendapatan Hotel");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_NAVY);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        JLabel lblPeriode = new JLabel("Periode:");
        lblPeriode.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPeriode.setForeground(COLOR_NAVY);

        JComboBox<String> cbPeriode = new JComboBox<>(new String[]{"Harian", "Bulanan", "Tahunan"});
        cbPeriode.setSelectedItem("Bulanan");
        cbPeriode.setPreferredSize(new Dimension(160, 36));
        UIHelper.styleField(cbPeriode);

        JButton btnTerapkan = UIHelper.createRoundedButton("Hitung Laporan", COLOR_GOLD, UIHelper.COLOR_GOLD_HOVER, Color.WHITE, 12);
        btnTerapkan.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnTerapkan.setPreferredSize(new Dimension(145, 36));

        filterPanel.add(lblPeriode);
        filterPanel.add(cbPeriode);
        filterPanel.add(btnTerapkan);

        topHeader.add(lblTitle, BorderLayout.NORTH);
        topHeader.add(filterPanel, BorderLayout.SOUTH);

        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryRow.setOpaque(false);
        summaryRow.add(createSummaryCard("TOTAL AKUMULASI PENDAPATAN", lblTotalPendapatanVal, new Color(5, 150, 105)));
        summaryRow.add(createSummaryCard("TOTAL RESERVASI VALID", lblTotalTransaksiVal, COLOR_NAVY));
        summaryRow.add(createSummaryCard("RATA-RATA PENDAPATAN / PERIODE", lblRataRataVal, new Color(180, 83, 9)));

        JPanel centerSplit = new JPanel(new GridLayout(1, 2, 16, 0));
        centerSplit.setOpaque(false);

        JPanel chartContainer = UIHelper.createRoundedCard(16, 16, 16);
        chartContainer.setLayout(new BorderLayout(0, 10));
        JLabel lblChartHeader = new JLabel("Visualisasi Grafik Pendapatan", SwingConstants.CENTER);
        lblChartHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblChartHeader.setForeground(COLOR_NAVY);
        chartContainer.add(lblChartHeader, BorderLayout.NORTH);
        chartContainer.add(chartPanel, BorderLayout.CENTER);

        String[] cols = {"Periode", "Jumlah Transaksi", "Total Pendapatan"};
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
                lbl.setHorizontalAlignment(c == 2 ? SwingConstants.RIGHT : SwingConstants.CENTER);
                lbl.setForeground(c == 2 ? new Color(5, 150, 105) : COLOR_NAVY);
                lbl.setFont(new Font("Segoe UI", c == 2 ? Font.BOLD : Font.PLAIN, 13));
                return lbl;
            }
        };
        for (int i = 0; i < cols.length; i++) table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        scrollTable.getViewport().setBackground(Color.WHITE);

        centerSplit.add(chartContainer);
        centerSplit.add(scrollTable);

        JPanel mainBody = new JPanel(new BorderLayout(15, 15));
        mainBody.setOpaque(false);
        mainBody.add(summaryRow, BorderLayout.NORTH);
        mainBody.add(centerSplit, BorderLayout.CENTER);

        add(topHeader, BorderLayout.NORTH);
        add(mainBody, BorderLayout.CENTER);

        btnTerapkan.addActionListener(e -> loadLaporan(cbPeriode.getSelectedItem().toString()));
        loadLaporan("Bulanan");
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = UIHelper.createRoundedCard(14, 14, 18);
        card.setLayout(new GridLayout(2, 1, 2, 6));

        JLabel lblTop = new JLabel(title);
        lblTop.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTop.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        valueLabel.setForeground(accentColor);

        card.add(lblTop);
        card.add(valueLabel);
        return card;
    }

    private void loadLaporan(String modePeriode) {
        model.setRowCount(0);
        dataGrafik = dao.getLaporanPendapatan(modePeriode);

        double grandTotal = 0;
        int totalTrx = 0;

        for (ManajerDAO.PendapatanItem item : dataGrafik) {
            grandTotal += item.getTotalPendapatan();
            totalTrx += item.getJumlahTransaksi();
            model.addRow(new Object[]{
                    item.getPeriodeLabel(),
                    item.getJumlahTransaksi() + " Transaksi",
                    String.format("Rp %,.0f", item.getTotalPendapatan())
            });
        }

        double rataRata = dataGrafik.isEmpty() ? 0 : grandTotal / dataGrafik.size();
        lblTotalPendapatanVal.setText(String.format("Rp %,.0f", grandTotal));
        lblTotalTransaksiVal.setText(totalTrx + " Transaksi");
        lblRataRataVal.setText(String.format("Rp %,.0f", rataRata));

        chartPanel.repaint();
    }

    private class BarChartPanel extends JPanel {
        public BarChartPanel() {
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (dataGrafik == null || dataGrafik.isEmpty()) {
                g2.setColor(new Color(100, 116, 139));
                g2.drawString("Belum ada data pendapatan pada periode ini.", w / 2 - 115, h / 2);
                return;
            }

            double maxVal = dataGrafik.stream().mapToDouble(ManajerDAO.PendapatanItem::getTotalPendapatan).max().orElse(1);
            if (maxVal <= 0) maxVal = 1;

            int padLeft = 45, padRight = 25, padTop = 30, padBottom = 45;
            int chartW = w - padLeft - padRight;
            int chartH = h - padTop - padBottom;

            g2.setColor(new Color(203, 213, 225));
            g2.drawLine(padLeft, padTop, padLeft, padTop + chartH);
            g2.drawLine(padLeft, padTop + chartH, padLeft + chartW, padTop + chartH);

            int n = dataGrafik.size();
            int slotW = chartW / n;
            int barW = Math.min(55, Math.max(22, slotW - 18));

            for (int i = 0; i < n; i++) {
                ManajerDAO.PendapatanItem item = dataGrafik.get(i);
                int barH = (int) ((item.getTotalPendapatan() / maxVal) * (chartH - 22));
                int x = padLeft + i * slotW + (slotW - barW) / 2;
                int y = padTop + chartH - barH;

                g2.setColor(i % 2 == 0 ? COLOR_NAVY : COLOR_GOLD);
                g2.fillRoundRect(x, y, barW, barH, 8, 8);

                g2.setColor(COLOR_NAVY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String valShort = String.format("Rp %,.0fK", item.getTotalPendapatan() / 1000.0);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(valShort, x + (barW - fm.stringWidth(valShort)) / 2, Math.max(padTop + 12, y - 6));

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                FontMetrics fmLbl = g2.getFontMetrics();
                g2.drawString(item.getPeriodeLabel(), x + (barW - fmLbl.stringWidth(item.getPeriodeLabel())) / 2, padTop + chartH + 18);
            }
        }
    }
}