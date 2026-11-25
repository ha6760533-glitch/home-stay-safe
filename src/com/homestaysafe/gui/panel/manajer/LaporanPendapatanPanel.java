package com.homestaysafe.gui.panel.manajer;

import com.homestaysafe.util.UIHelper;
import com.homestaysafe.dao.ManajerDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

public class LaporanPendapatanPanel extends JPanel {

    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_ORANGE = new Color(234, 88, 12);
    private final Color COLOR_EMERALD = new Color(5, 150, 105);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    private final ManajerDAO dao = new ManajerDAO();
    private final DefaultTableModel model;

    private final JLabel lblTotalKamarVal = new JLabel("Rp 0");
    private final JLabel lblTotalMenuVal = new JLabel("Rp 0");
    private final JLabel lblGrandTotalVal = new JLabel("Rp 0");

    private final ComboChartPanel chartKamarPanel = new ComboChartPanel(
            "Pendapatan Kamar (Rp)", "Jumlah Reservasi (Trx)",
            new Color(30, 58, 138), COLOR_ORANGE, "Trx"
    );
    private final ComboChartPanel chartMenuPanel = new ComboChartPanel(
            "Pendapatan Menu F&B (Rp)", "Total Porsi Terjual",
            COLOR_GOLD, COLOR_EMERALD, "Porsi"
    );

    public LaporanPendapatanPanel() {
        setLayout(new BorderLayout(15, 14));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Header & Filter (Bulanan / Tahunan)
        JPanel topHeader = new JPanel(new BorderLayout(0, 8));
        topHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Laporan & Combo Chart Pendapatan (Kamar & Menu F&B)");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 21));
        lblTitle.setForeground(COLOR_NAVY);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblPeriode = new JLabel("Periode Laporan:");
        lblPeriode.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPeriode.setForeground(COLOR_NAVY);

        JComboBox<String> cbPeriode = new JComboBox<>(new String[]{"Bulanan", "Tahunan"});
        cbPeriode.setSelectedItem("Bulanan");
        cbPeriode.setPreferredSize(new Dimension(160, 35));
        UIHelper.styleField(cbPeriode);

        JButton btnTerapkan = UIHelper.createRoundedButton("Hitung Laporan", COLOR_GOLD, UIHelper.COLOR_GOLD_HOVER, Color.WHITE, 12);
        btnTerapkan.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnTerapkan.setPreferredSize(new Dimension(145, 35));

        filterPanel.add(lblPeriode);
        filterPanel.add(cbPeriode);
        filterPanel.add(btnTerapkan);

        topHeader.add(lblTitle, BorderLayout.NORTH);
        topHeader.add(filterPanel, BorderLayout.SOUTH);

        // 2. Kartu Ringkasan (Kamar, Menu, Total Gabungan)
        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 14, 0));
        summaryRow.setOpaque(false);
        summaryRow.add(createSummaryCard("TOTAL PENDAPATAN KAMAR", lblTotalKamarVal, new Color(30, 58, 138)));
        summaryRow.add(createSummaryCard("TOTAL PENDAPATAN MENU (F&B)", lblTotalMenuVal, new Color(180, 83, 9)));
        summaryRow.add(createSummaryCard("TOTAL AKUMULASI PENDAPATAN", lblGrandTotalVal, COLOR_EMERALD));

        // 3. Dua Grafik Dual-Axis Combo Chart (Kiri: Kamar, Kanan: Menu)
        JPanel chartsSplit = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsSplit.setOpaque(false);
        chartsSplit.setPreferredSize(new Dimension(0, 315));

        JPanel cardChartKamar = UIHelper.createRoundedCard(16, 12, 14);
        cardChartKamar.setLayout(new BorderLayout(0, 6));
        JLabel lblHdrKamar = new JLabel("Combo Chart Pendapatan Kamar & Reservasi", SwingConstants.CENTER);
        lblHdrKamar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHdrKamar.setForeground(COLOR_NAVY);
        cardChartKamar.add(lblHdrKamar, BorderLayout.NORTH);
        cardChartKamar.add(chartKamarPanel, BorderLayout.CENTER);

        JPanel cardChartMenu = UIHelper.createRoundedCard(16, 12, 14);
        cardChartMenu.setLayout(new BorderLayout(0, 6));
        JLabel lblHdrMenu = new JLabel("Combo Chart Pendapatan Menu & Porsi Terjual", SwingConstants.CENTER);
        lblHdrMenu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHdrMenu.setForeground(COLOR_NAVY);
        cardChartMenu.add(lblHdrMenu, BorderLayout.NORTH);
        cardChartMenu.add(chartMenuPanel, BorderLayout.CENTER);

        chartsSplit.add(cardChartKamar);
        chartsSplit.add(cardChartMenu);

        // 4. Tabel Rincian Gabungan per Periode
        String[] cols = {"Periode", "Reservasi Kamar", "Pendapatan Kamar", "Porsi Menu Terjual", "Pendapatan Menu (F&B)", "Total Gabungan"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));
        table.setFillsViewportHeight(true);

        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
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
                lbl.setBorder(new EmptyBorder(0, 10, 0, 12));
                boolean isCurrency = (c == 2 || c == 4 || c == 5);
                lbl.setHorizontalAlignment(isCurrency ? SwingConstants.RIGHT : SwingConstants.CENTER);
                lbl.setForeground(c == 5 ? COLOR_EMERALD : COLOR_NAVY);
                lbl.setFont(new Font("Segoe UI", c == 5 ? Font.BOLD : Font.PLAIN, 12));
                return lbl;
            }
        };
        for (int i = 0; i < cols.length; i++) table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        scrollTable.getViewport().setBackground(Color.WHITE);

        JPanel centerContainer = new JPanel(new BorderLayout(0, 14));
        centerContainer.setOpaque(false);
        centerContainer.add(chartsSplit, BorderLayout.NORTH);
        centerContainer.add(scrollTable, BorderLayout.CENTER);

        JPanel mainBody = new JPanel(new BorderLayout(0, 14));
        mainBody.setOpaque(false);
        mainBody.add(summaryRow, BorderLayout.NORTH);
        mainBody.add(centerContainer, BorderLayout.CENTER);

        add(topHeader, BorderLayout.NORTH);
        add(mainBody, BorderLayout.CENTER);

        cbPeriode.addActionListener(e -> loadLaporan(cbPeriode.getSelectedItem().toString()));
        btnTerapkan.addActionListener(e -> loadLaporan(cbPeriode.getSelectedItem().toString()));
        loadLaporan("Bulanan");
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = UIHelper.createRoundedCard(14, 12, 16);
        card.setLayout(new GridLayout(2, 1, 2, 4));

        JLabel lblTop = new JLabel(title);
        lblTop.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTop.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(accentColor);

        card.add(lblTop);
        card.add(valueLabel);
        return card;
    }

    private void loadLaporan(String modePeriode) {
        model.setRowCount(0);

        List<ManajerDAO.PendapatanItem> listKamar = dao.getLaporanPendapatanKamar(modePeriode);
        List<ManajerDAO.PendapatanItem> listMenu = dao.getLaporanPendapatanMenu(modePeriode);

        // Gabungkan daftar periode agar urutan bulan/tahun sinkron di kedua chart & tabel
        Set<String> allPeriodes = new TreeSet<>();
        Map<String, ManajerDAO.PendapatanItem> mapKamar = new HashMap<>();
        Map<String, ManajerDAO.PendapatanItem> mapMenu = new HashMap<>();

        for (ManajerDAO.PendapatanItem item : listKamar) {
            allPeriodes.add(item.getPeriodeLabel());
            mapKamar.put(item.getPeriodeLabel(), item);
        }
        for (ManajerDAO.PendapatanItem item : listMenu) {
            allPeriodes.add(item.getPeriodeLabel());
            mapMenu.put(item.getPeriodeLabel(), item);
        }

        List<ManajerDAO.PendapatanItem> syncedKamar = new ArrayList<>();
        List<ManajerDAO.PendapatanItem> syncedMenu = new ArrayList<>();

        double totalKamar = 0;
        double totalMenu = 0;

        for (String p : allPeriodes) {
            ManajerDAO.PendapatanItem kItem = mapKamar.getOrDefault(p, new ManajerDAO.PendapatanItem(p, 0, 0));
            ManajerDAO.PendapatanItem mItem = mapMenu.getOrDefault(p, new ManajerDAO.PendapatanItem(p, 0, 0));

            syncedKamar.add(kItem);
            syncedMenu.add(mItem);

            totalKamar += kItem.getTotalPendapatan();
            totalMenu += mItem.getTotalPendapatan();

            model.addRow(new Object[]{
                    p,
                    kItem.getJumlahTransaksi() + " Transaksi",
                    String.format("Rp %,.0f", kItem.getTotalPendapatan()),
                    mItem.getJumlahTransaksi() + " Porsi",
                    String.format("Rp %,.0f", mItem.getTotalPendapatan()),
                    String.format("Rp %,.0f", kItem.getTotalPendapatan() + mItem.getTotalPendapatan())
            });
        }

        lblTotalKamarVal.setText(String.format("Rp %,.0f", totalKamar));
        lblTotalMenuVal.setText(String.format("Rp %,.0f", totalMenu));
        lblGrandTotalVal.setText(String.format("Rp %,.0f", totalKamar + totalMenu));

        chartKamarPanel.setData(syncedKamar);
        chartMenuPanel.setData(syncedMenu);
    }

    /** Komponen Dual-Axis Combo Chart (Batang = Pendapatan Rp, Garis = Jumlah Transaksi/Porsi) */
    private static class ComboChartPanel extends JPanel {
        private final String barLegend;
        private final String lineLegend;
        private final Color barColor;
        private final Color lineColor;
        private final String unitSuffix;
        private List<ManajerDAO.PendapatanItem> items = new ArrayList<>();

        public ComboChartPanel(String barLegend, String lineLegend, Color barColor, Color lineColor, String unitSuffix) {
            this.barLegend = barLegend;
            this.lineLegend = lineLegend;
            this.barColor = barColor;
            this.lineColor = lineColor;
            this.unitSuffix = unitSuffix;
            setBackground(Color.WHITE);
        }

        public void setData(List<ManajerDAO.PendapatanItem> items) {
            this.items = items != null ? items : new ArrayList<>();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = getWidth();
            int h = getHeight();

            if (items == null || items.isEmpty()) {
                g2.setColor(new Color(100, 116, 139));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.drawString("Belum ada data pada periode ini.", w / 2 - 90, h / 2);
                g2.dispose();
                return;
            }

            double maxBar = items.stream().mapToDouble(ManajerDAO.PendapatanItem::getTotalPendapatan).max().orElse(1);
            if (maxBar <= 0) maxBar = 1;

            int maxLine = items.stream().mapToInt(ManajerDAO.PendapatanItem::getJumlahTransaksi).max().orElse(1);
            if (maxLine <= 0) maxLine = 1;

            int padLeft = 58;
            int padRight = 46;
            int padTop = 24;
            int padBottom = items.size() > 7 ? 62 : 48;

            int chartW = Math.max(50, w - padLeft - padRight);
            int chartH = Math.max(50, h - padTop - padBottom);

            // Grid garis horizontal & label Sumbu Y Kiri (Rp) + Sumbu Y Kanan (Jumlah)
            int gridSteps = 4;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int step = 0; step <= gridSteps; step++) {
                int yGrid = padTop + chartH - (int) ((step / (double) gridSteps) * chartH);

                g2.setColor(step == 0 ? new Color(148, 163, 184) : new Color(241, 245, 249));
                g2.drawLine(padLeft, yGrid, padLeft + chartW, yGrid);

                // Label Sumbu Y Kiri (Pendapatan)
                double valRp = (maxBar * step) / gridSteps;
                String lblLeft = formatCompactRp(valRp);
                FontMetrics fmL = g2.getFontMetrics();
                g2.setColor(new Color(71, 85, 105));
                g2.drawString(lblLeft, padLeft - fmL.stringWidth(lblLeft) - 6, yGrid + 4);

                // Label Sumbu Y Kanan (Transaksi / Porsi)
                int valCount = (int) Math.round((maxLine * step) / (double) gridSteps);
                String lblRight = String.valueOf(valCount);
                g2.setColor(lineColor);
                g2.drawString(lblRight, padLeft + chartW + 6, yGrid + 4);
            }

            // Garis sumbu vertikal kiri & kanan
            g2.setColor(new Color(203, 213, 225));
            g2.drawLine(padLeft, padTop, padLeft, padTop + chartH);
            g2.drawLine(padLeft + chartW, padTop, padLeft + chartW, padTop + chartH);

            int n = items.size();
            int slotW = chartW / n;
            int barW = Math.min(44, Math.max(12, (int) (slotW * 0.56)));

            int[] lineX = new int[n];
            int[] lineY = new int[n];

            // 1. Gambar Batang (Bar) untuk Pendapatan (Rp)
            for (int i = 0; i < n; i++) {
                ManajerDAO.PendapatanItem item = items.get(i);
                int centerX = padLeft + i * slotW + slotW / 2;
                int barH = (int) Math.round((item.getTotalPendapatan() / maxBar) * (chartH - 14));
                int barX = centerX - barW / 2;
                int barY = padTop + chartH - barH;

                g2.setColor(barColor);
                if (barH > 0) {
                    g2.fill(new RoundRectangle2D.Float(barX, barY, barW, barH, 6, 6));
                }

                // Nilai batang di atas bar (tampilkan jika slot cukup lebar)
                if (slotW >= 34 && item.getTotalPendapatan() > 0) {
                    g2.setColor(new Color(30, 41, 59));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    String barValText = formatCompactRp(item.getTotalPendapatan());
                    FontMetrics fmVal = g2.getFontMetrics();
                    g2.drawString(barValText, centerX - fmVal.stringWidth(barValText) / 2, Math.max(padTop + 10, barY - 4));
                }

                // Hitung koordinat titik garis (Sumbu Y Kanan)
                int pointH = (int) Math.round((item.getJumlahTransaksi() / (double) maxLine) * (chartH - 14));
                lineX[i] = centerX;
                lineY[i] = padTop + chartH - pointH;

                // Label Periode Sumbu X (dimiringkan jika periode banyak agar tidak bertumpuk)
                g2.setColor(new Color(51, 65, 85));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String pLabel = item.getPeriodeLabel();
                FontMetrics fmP = g2.getFontMetrics();
                if (n > 7) {
                    Graphics2D gRot = (Graphics2D) g2.create();
                    gRot.translate(centerX, padTop + chartH + 8);
                    gRot.rotate(Math.toRadians(-35));
                    gRot.drawString(pLabel, -fmP.stringWidth(pLabel), 6);
                    gRot.dispose();
                } else {
                    g2.drawString(pLabel, centerX - fmP.stringWidth(pLabel) / 2, padTop + chartH + 16);
                }
            }

            // 2. Gambar Garis (Line) & Titik Node untuk Jumlah Transaksi / Porsi
            g2.setColor(lineColor);
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < n - 1; i++) {
                g2.drawLine(lineX[i], lineY[i], lineX[i + 1], lineY[i + 1]);
            }

            for (int i = 0; i < n; i++) {
                int r = 4;
                g2.setColor(Color.WHITE);
                g2.fill(new Ellipse2D.Float(lineX[i] - r - 1.5f, lineY[i] - r - 1.5f, (r + 1.5f) * 2, (r + 1.5f) * 2));
                g2.setColor(lineColor);
                g2.fill(new Ellipse2D.Float(lineX[i] - r, lineY[i] - r, r * 2, r * 2));

                if (slotW >= 30) {
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    String cntTxt = items.get(i).getJumlahTransaksi() + "";
                    FontMetrics fmC = g2.getFontMetrics();
                    int textY = Math.max(padTop + 11, lineY[i] - 7);
                    g2.drawString(cntTxt, lineX[i] - fmC.stringWidth(cntTxt) / 2, textY);
                }
            }

            // 3. Legenda Bawah (Batang vs Garis)
            int legendY = h - 8;
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics fmLeg = g2.getFontMetrics();

            int totalLegW = 14 + fmLeg.stringWidth(barLegend) + 24 + 18 + fmLeg.stringWidth(lineLegend + " (" + unitSuffix + ")");
            int startX = Math.max(10, (w - totalLegW) / 2);

            g2.setColor(barColor);
            g2.fillRoundRect(startX, legendY - 9, 11, 11, 3, 3);
            g2.setColor(new Color(30, 41, 59));
            g2.drawString(barLegend, startX + 15, legendY);

            int x2 = startX + 15 + fmLeg.stringWidth(barLegend) + 20;
            g2.setColor(lineColor);
            g2.setStroke(new BasicStroke(2.2f));
            g2.drawLine(x2, legendY - 4, x2 + 14, legendY - 4);
            g2.fill(new Ellipse2D.Float(x2 + 4, legendY - 7, 6, 6));
            g2.setColor(new Color(30, 41, 59));
            g2.drawString(lineLegend + " (" + unitSuffix + ")", x2 + 18, legendY);

            g2.dispose();
        }

        private String formatCompactRp(double val) {
            if (val >= 1_000_000) {
                return String.format("%.1fJt", val / 1_000_000.0);
            } else if (val >= 1_000) {
                return String.format("%.0fK", val / 1_000.0);
            }
            return String.format("%.0f", val);
        }
    }
}