package com.homestaysafe.gui.panel.manajer;

import com.homestaysafe.util.UIHelper;
import com.homestaysafe.dao.ManajerDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.List;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;

public class LaporanPendapatanPanel extends JPanel {

    private static final String MODE_MINGGUAN = "Mingguan (1 Bulan)";
    private static final String MODE_BULANAN = "Bulanan";
    private static final String MODE_TAHUNAN = "Tahunan";
    private static final String ALL_MONTHS_OPTION = "Semua Bulan";

    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_ORANGE = new Color(234, 88, 12);
    private final Color COLOR_EMERALD = new Color(5, 150, 105);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    private final ManajerDAO dao = new ManajerDAO();
    private final DefaultTableModel model;
    private final JTable table;
    private final JScrollPane pageScrollPane;

    private final JComboBox<String> cbPeriode;
    private final JLabel lblBulan;
    private final JComboBox<String> cbBulan;
    private boolean isUpdatingBulanCombo = false;

    private final JLabel lblTotalKamarVal = new JLabel("Rp 0");
    private final JLabel lblTotalMenuVal = new JLabel("Rp 0");
    private final JLabel lblGrandTotalVal = new JLabel("Rp 0");

    // Simpan data gabungan terakhir untuk keperluan export CSV & PDF
    private String currentPeriodeMode = MODE_MINGGUAN;
    private String currentBulanFilter = YearMonth.now().toString();
    private double lastTotalKamar = 0;
    private double lastTotalMenu = 0;
    private double lastGrandTotal = 0;
    private final List<GabunganRow> lastRows = new ArrayList<>();

    private final ComboChartPanel chartKamarPanel = new ComboChartPanel(
            "Pendapatan Kamar (Rp)", "Jumlah Reservasi (Trx)",
            new Color(30, 58, 138), COLOR_ORANGE, "Trx"
    );
    private final ComboChartPanel chartMenuPanel = new ComboChartPanel(
            "Pendapatan Menu F&B (Rp)", "Total Porsi Terjual",
            COLOR_GOLD, COLOR_EMERALD, "Porsi"
    );

    private static class GabunganRow {
        final String periode;
        final int trxKamar;
        final double pendapatanKamar;
        final int porsiMenu;
        final double pendapatanMenu;
        final double totalGabungan;

        GabunganRow(String periode, int trxKamar, double pendapatanKamar,
                    int porsiMenu, double pendapatanMenu) {
            this.periode = periode;
            this.trxKamar = trxKamar;
            this.pendapatanKamar = pendapatanKamar;
            this.porsiMenu = porsiMenu;
            this.pendapatanMenu = pendapatanMenu;
            this.totalGabungan = pendapatanKamar + pendapatanMenu;
        }
    }

    public LaporanPendapatanPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(18, 24, 18, 24));

        // 1. Header & Filter + Tombol Export
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

        cbPeriode = new JComboBox<>(new String[]{MODE_MINGGUAN, MODE_BULANAN, MODE_TAHUNAN});
        cbPeriode.setSelectedItem(MODE_MINGGUAN);
        cbPeriode.setPreferredSize(new Dimension(170, 35));
        UIHelper.styleField(cbPeriode);

        lblBulan = new JLabel("Bulan:");
        lblBulan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBulan.setForeground(COLOR_NAVY);

        cbBulan = new JComboBox<>(new String[]{YearMonth.now().toString()});
        cbBulan.setSelectedItem(YearMonth.now().toString());
        cbBulan.setPreferredSize(new Dimension(145, 35));
        UIHelper.styleField(cbBulan);

        JButton btnTerapkan = UIHelper.createRoundedButton("Hitung Laporan", COLOR_GOLD, UIHelper.COLOR_GOLD_HOVER, Color.WHITE, 12);
        btnTerapkan.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnTerapkan.setPreferredSize(new Dimension(135, 35));

        JButton btnExportCsv = UIHelper.createRoundedButton("Download CSV", COLOR_NAVY, new Color(30, 41, 59), Color.WHITE, 12);
        btnExportCsv.setBorder(new EmptyBorder(8, 14, 8, 14));
        btnExportCsv.setPreferredSize(new Dimension(132, 35));

        JButton btnExportPdf = UIHelper.createRoundedButton("Download PDF", COLOR_EMERALD, new Color(4, 120, 87), Color.WHITE, 12);
        btnExportPdf.setBorder(new EmptyBorder(8, 14, 8, 14));
        btnExportPdf.setPreferredSize(new Dimension(132, 35));

        filterPanel.add(lblPeriode);
        filterPanel.add(cbPeriode);
        filterPanel.add(lblBulan);
        filterPanel.add(cbBulan);
        filterPanel.add(btnTerapkan);
        filterPanel.add(btnExportCsv);
        filterPanel.add(btnExportPdf);

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
        chartsSplit.setPreferredSize(new Dimension(0, 325));

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

        // 4. Tabel Rincian Gabungan per Periode (tampil penuh, ikut scroll halaman)
        String[] cols = {"Periode", "Reservasi Kamar", "Pendapatan Kamar", "Porsi Menu Terjual", "Pendapatan Menu (F&B)", "Total Gabungan"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));
        table.setFillsViewportHeight(false);

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

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        tableContainer.add(table.getTableHeader(), BorderLayout.NORTH);
        tableContainer.add(table, BorderLayout.CENTER);

        JPanel scrollableContent = new JPanel();
        scrollableContent.setLayout(new BoxLayout(scrollableContent, BoxLayout.Y_AXIS));
        scrollableContent.setOpaque(false);
        scrollableContent.setBorder(new EmptyBorder(2, 0, 12, 4));

        summaryRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        chartsSplit.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        scrollableContent.add(summaryRow);
        scrollableContent.add(Box.createVerticalStrut(14));
        scrollableContent.add(chartsSplit);
        scrollableContent.add(Box.createVerticalStrut(14));
        scrollableContent.add(tableContainer);

        pageScrollPane = new JScrollPane(scrollableContent);
        pageScrollPane.setBorder(null);
        pageScrollPane.setOpaque(false);
        pageScrollPane.getViewport().setOpaque(false);
        pageScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pageScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        pageScrollPane.getVerticalScrollBar().setUnitIncrement(18);

        add(topHeader, BorderLayout.NORTH);
        add(pageScrollPane, BorderLayout.CENTER);

        cbPeriode.addActionListener(e -> {
            String mode = cbPeriode.getSelectedItem() != null ? cbPeriode.getSelectedItem().toString() : MODE_MINGGUAN;
            boolean showBulan = MODE_MINGGUAN.equalsIgnoreCase(mode) || MODE_BULANAN.equalsIgnoreCase(mode);
            lblBulan.setVisible(showBulan);
            cbBulan.setVisible(showBulan);

            String targetBulan = MODE_MINGGUAN.equalsIgnoreCase(mode)
                    ? resolveDefaultMonthForWeekly()
                    : ALL_MONTHS_OPTION;
            loadLaporan(mode, targetBulan, true);
        });

        cbBulan.addActionListener(e -> {
            if (isUpdatingBulanCombo) return;
            String mode = cbPeriode.getSelectedItem() != null ? cbPeriode.getSelectedItem().toString() : MODE_MINGGUAN;
            String bulanDipilih = cbBulan.getSelectedItem() != null ? cbBulan.getSelectedItem().toString() : resolveDefaultMonthForWeekly();
            loadLaporan(mode, bulanDipilih, false);
        });

        btnTerapkan.addActionListener(e -> {
            String mode = cbPeriode.getSelectedItem() != null ? cbPeriode.getSelectedItem().toString() : MODE_MINGGUAN;
            String bulanDipilih = cbBulan.getSelectedItem() != null ? cbBulan.getSelectedItem().toString() : resolveDefaultMonthForWeekly();
            loadLaporan(mode, bulanDipilih, false);
        });

        btnExportCsv.addActionListener(e -> exportLaporanCsv());
        btnExportPdf.addActionListener(e -> exportLaporanPdf());

        loadLaporan(MODE_MINGGUAN, YearMonth.now().toString(), true);
    }

    private String resolveDefaultMonthForWeekly() {
        String current = cbBulan.getSelectedItem() != null ? cbBulan.getSelectedItem().toString() : "";
        if (!current.isBlank() && !ALL_MONTHS_OPTION.equalsIgnoreCase(current)) {
            return current;
        }
        return YearMonth.now().toString();
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

    private void updateTablePreferredHeight() {
        int rows = Math.max(model.getRowCount(), 1);
        int totalHeight = rows * table.getRowHeight();
        table.setPreferredSize(new Dimension(table.getPreferredSize().width, totalHeight));
        table.revalidate();
    }

    private void refreshBulanOptions(List<String> chronologicalMonths, boolean includeAllOption, String keepSelection) {
        isUpdatingBulanCombo = true;
        try {
            Set<String> monthSet = new LinkedHashSet<>();
            List<String> descMonths = new ArrayList<>(chronologicalMonths);
            Collections.reverse(descMonths);
            monthSet.addAll(descMonths);
            monthSet.add(YearMonth.now().toString());

            DefaultComboBoxModel<String> bulanModel = new DefaultComboBoxModel<>();
            if (includeAllOption) {
                bulanModel.addElement(ALL_MONTHS_OPTION);
            }
            for (String ym : monthSet) {
                bulanModel.addElement(ym);
            }
            cbBulan.setModel(bulanModel);

            if (keepSelection != null && bulanModel.getIndexOf(keepSelection) >= 0) {
                cbBulan.setSelectedItem(keepSelection);
            } else if (!includeAllOption && bulanModel.getSize() > 0) {
                String nowYm = YearMonth.now().toString();
                cbBulan.setSelectedItem(bulanModel.getIndexOf(nowYm) >= 0 ? nowYm : bulanModel.getElementAt(0));
            } else {
                cbBulan.setSelectedItem(ALL_MONTHS_OPTION);
            }
        } finally {
            isUpdatingBulanCombo = false;
        }
    }

    private List<String> buildWeeklyLabelsForMonth(String yearMonthStr) {
        YearMonth ym;
        try {
            ym = YearMonth.parse(yearMonthStr.trim());
        } catch (Exception e) {
            ym = YearMonth.now();
        }
        int lastDay = ym.lengthOfMonth();
        List<String> labels = new ArrayList<>();
        int weekNum = 1;
        for (int startDay = 1; startDay <= lastDay && weekNum <= 5; startDay += 7) {
            int endDay = Math.min(startDay + 6, lastDay);
            labels.add(String.format("M%d (%02d-%02d)", weekNum, startDay, endDay));
            weekNum++;
        }
        return labels;
    }

    private String normalizeWeekKey(String rawPeriode) {
        if (rawPeriode == null) return "";
        String trimmed = rawPeriode.trim();
        if (trimmed.matches("^M[1-5]$")) {
            return trimmed;
        }
        return trimmed;
    }

    private void loadLaporan(String modePeriode, String bulanFilter, boolean refreshMonthList) {
        currentPeriodeMode = modePeriode;
        model.setRowCount(0);
        lastRows.clear();

        // Kumpulkan daftar bulan yang tersedia dari data bulanan agar dropdown bulan selalu lengkap
        List<ManajerDAO.PendapatanItem> allMonthsKamar = dao.getLaporanPendapatanKamar(MODE_BULANAN);
        List<ManajerDAO.PendapatanItem> allMonthsMenu = dao.getLaporanPendapatanMenu(MODE_BULANAN);

        TreeSet<String> availableMonthsSet = new TreeSet<>();
        for (ManajerDAO.PendapatanItem item : allMonthsKamar) {
            if (item.getPeriodeLabel() != null && !item.getPeriodeLabel().isBlank()) {
                availableMonthsSet.add(item.getPeriodeLabel().trim());
            }
        }
        for (ManajerDAO.PendapatanItem item : allMonthsMenu) {
            if (item.getPeriodeLabel() != null && !item.getPeriodeLabel().isBlank()) {
                availableMonthsSet.add(item.getPeriodeLabel().trim());
            }
        }

        List<String> chronologicalMonths = new ArrayList<>();
        if (!availableMonthsSet.isEmpty()) {
            YearMonth startYm = YearMonth.parse(availableMonthsSet.first());
            YearMonth endYm = YearMonth.parse(availableMonthsSet.last());
            if (endYm.isBefore(YearMonth.now())) {
                endYm = YearMonth.now();
            }
            for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
                chronologicalMonths.add(ym.toString());
            }
        } else {
            chronologicalMonths.add(YearMonth.now().toString());
        }

        boolean isMingguan = MODE_MINGGUAN.equalsIgnoreCase(modePeriode);
        boolean isBulanan = MODE_BULANAN.equalsIgnoreCase(modePeriode);

        if (isMingguan) {
            String selectedMonth = (bulanFilter == null || bulanFilter.isBlank() || ALL_MONTHS_OPTION.equalsIgnoreCase(bulanFilter))
                    ? YearMonth.now().toString()
                    : bulanFilter.trim();
            currentBulanFilter = selectedMonth;
            if (refreshMonthList) {
                refreshBulanOptions(chronologicalMonths, false, currentBulanFilter);
            }

            List<String> weekLabels = buildWeeklyLabelsForMonth(currentBulanFilter);
            List<ManajerDAO.PendapatanItem> weekKamar = dao.getLaporanPendapatanKamarMingguan(currentBulanFilter);
            List<ManajerDAO.PendapatanItem> weekMenu = dao.getLaporanPendapatanMenuMingguan(currentBulanFilter);

            Map<String, ManajerDAO.PendapatanItem> mapKamar = new HashMap<>();
            Map<String, ManajerDAO.PendapatanItem> mapMenu = new HashMap<>();

            for (ManajerDAO.PendapatanItem item : weekKamar) {
                mapKamar.put(normalizeWeekKey(item.getPeriodeLabel()), item);
            }
            for (ManajerDAO.PendapatanItem item : weekMenu) {
                mapMenu.put(normalizeWeekKey(item.getPeriodeLabel()), item);
            }

            List<ManajerDAO.PendapatanItem> syncedKamar = new ArrayList<>();
            List<ManajerDAO.PendapatanItem> syncedMenu = new ArrayList<>();

            double totalKamar = 0;
            double totalMenu = 0;

            for (int idx = 0; idx < weekLabels.size(); idx++) {
                String labelMinggu = weekLabels.get(idx);
                String weekCode = "M" + (idx + 1);

                ManajerDAO.PendapatanItem rawK = mapKamar.getOrDefault(weekCode, new ManajerDAO.PendapatanItem(weekCode, 0, 0));
                ManajerDAO.PendapatanItem rawM = mapMenu.getOrDefault(weekCode, new ManajerDAO.PendapatanItem(weekCode, 0, 0));

                ManajerDAO.PendapatanItem kItem = new ManajerDAO.PendapatanItem(labelMinggu, rawK.getJumlahTransaksi(), rawK.getTotalPendapatan());
                ManajerDAO.PendapatanItem mItem = new ManajerDAO.PendapatanItem(labelMinggu, rawM.getJumlahTransaksi(), rawM.getTotalPendapatan());

                syncedKamar.add(kItem);
                syncedMenu.add(mItem);

                totalKamar += kItem.getTotalPendapatan();
                totalMenu += mItem.getTotalPendapatan();

                GabunganRow rowData = new GabunganRow(
                        labelMinggu,
                        kItem.getJumlahTransaksi(),
                        kItem.getTotalPendapatan(),
                        mItem.getJumlahTransaksi(),
                        mItem.getTotalPendapatan()
                );
                lastRows.add(rowData);

                model.addRow(new Object[]{
                        labelMinggu,
                        rowData.trxKamar + " Transaksi",
                        String.format("Rp %,.0f", rowData.pendapatanKamar),
                        rowData.porsiMenu + " Porsi",
                        String.format("Rp %,.0f", rowData.pendapatanMenu),
                        String.format("Rp %,.0f", rowData.totalGabungan)
                });
            }

            lastTotalKamar = totalKamar;
            lastTotalMenu = totalMenu;
            lastGrandTotal = totalKamar + totalMenu;

            updateTablePreferredHeight();

            lblTotalKamarVal.setText(String.format("Rp %,.0f", totalKamar));
            lblTotalMenuVal.setText(String.format("Rp %,.0f", totalMenu));
            lblGrandTotalVal.setText(String.format("Rp %,.0f", lastGrandTotal));

            chartKamarPanel.setData(syncedKamar);
            chartMenuPanel.setData(syncedMenu);

            SwingUtilities.invokeLater(() -> pageScrollPane.getVerticalScrollBar().setValue(0));
            return;
        }

        // Mode Bulanan atau Tahunan
        currentBulanFilter = bulanFilter != null ? bulanFilter : ALL_MONTHS_OPTION;
        if (isBulanan && refreshMonthList) {
            refreshBulanOptions(chronologicalMonths, true, currentBulanFilter);
        }

        List<ManajerDAO.PendapatanItem> allListKamar = dao.getLaporanPendapatanKamar(modePeriode);
        List<ManajerDAO.PendapatanItem> allListMenu = dao.getLaporanPendapatanMenu(modePeriode);

        TreeSet<String> rawPeriodes = new TreeSet<>();
        Map<String, ManajerDAO.PendapatanItem> mapKamar = new HashMap<>();
        Map<String, ManajerDAO.PendapatanItem> mapMenu = new HashMap<>();

        for (ManajerDAO.PendapatanItem item : allListKamar) {
            if (item.getPeriodeLabel() != null && !item.getPeriodeLabel().isBlank()) {
                rawPeriodes.add(item.getPeriodeLabel().trim());
                mapKamar.put(item.getPeriodeLabel().trim(), item);
            }
        }
        for (ManajerDAO.PendapatanItem item : allListMenu) {
            if (item.getPeriodeLabel() != null && !item.getPeriodeLabel().isBlank()) {
                rawPeriodes.add(item.getPeriodeLabel().trim());
                mapMenu.put(item.getPeriodeLabel().trim(), item);
            }
        }

        List<String> orderedPeriodes = new ArrayList<>();
        if (isBulanan && !rawPeriodes.isEmpty()) {
            YearMonth startYm = YearMonth.parse(rawPeriodes.first());
            YearMonth endYm = YearMonth.parse(rawPeriodes.last());
            for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
                orderedPeriodes.add(ym.toString());
            }
        } else {
            orderedPeriodes.addAll(rawPeriodes);
        }

        if (isBulanan && !ALL_MONTHS_OPTION.equalsIgnoreCase(currentBulanFilter)) {
            orderedPeriodes.removeIf(p -> !p.equals(currentBulanFilter));
        }

        List<ManajerDAO.PendapatanItem> syncedKamar = new ArrayList<>();
        List<ManajerDAO.PendapatanItem> syncedMenu = new ArrayList<>();

        double totalKamar = 0;
        double totalMenu = 0;

        for (String p : orderedPeriodes) {
            ManajerDAO.PendapatanItem kItem = mapKamar.getOrDefault(p, new ManajerDAO.PendapatanItem(p, 0, 0));
            ManajerDAO.PendapatanItem mItem = mapMenu.getOrDefault(p, new ManajerDAO.PendapatanItem(p, 0, 0));

            syncedKamar.add(kItem);
            syncedMenu.add(mItem);

            totalKamar += kItem.getTotalPendapatan();
            totalMenu += mItem.getTotalPendapatan();

            GabunganRow rowData = new GabunganRow(
                    p,
                    kItem.getJumlahTransaksi(),
                    kItem.getTotalPendapatan(),
                    mItem.getJumlahTransaksi(),
                    mItem.getTotalPendapatan()
            );
            lastRows.add(rowData);

            model.addRow(new Object[]{
                    p,
                    rowData.trxKamar + " Transaksi",
                    String.format("Rp %,.0f", rowData.pendapatanKamar),
                    rowData.porsiMenu + " Porsi",
                    String.format("Rp %,.0f", rowData.pendapatanMenu),
                    String.format("Rp %,.0f", rowData.totalGabungan)
            });
        }

        lastTotalKamar = totalKamar;
        lastTotalMenu = totalMenu;
        lastGrandTotal = totalKamar + totalMenu;

        updateTablePreferredHeight();

        lblTotalKamarVal.setText(String.format("Rp %,.0f", totalKamar));
        lblTotalMenuVal.setText(String.format("Rp %,.0f", totalMenu));
        lblGrandTotalVal.setText(String.format("Rp %,.0f", lastGrandTotal));

        chartKamarPanel.setData(syncedKamar);
        chartMenuPanel.setData(syncedMenu);

        SwingUtilities.invokeLater(() -> pageScrollPane.getVerticalScrollBar().setValue(0));
    }

    private String getPeriodeKeterangan() {
        if (MODE_MINGGUAN.equalsIgnoreCase(currentPeriodeMode)) {
            return "Mingguan Bulan " + currentBulanFilter;
        }
        if (MODE_BULANAN.equalsIgnoreCase(currentPeriodeMode) && !ALL_MONTHS_OPTION.equalsIgnoreCase(currentBulanFilter)) {
            return "Bulanan (" + currentBulanFilter + ")";
        }
        return currentPeriodeMode;
    }

    /** Export laporan gabungan Kamar + Menu F&B ke file CSV */
    private void exportLaporanCsv() {
        if (lastRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada data laporan untuk diunduh.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String labelFile = getPeriodeKeterangan().replaceAll("[^a-zA-Z0-9_-]", "_");
        String defaultName = "Laporan_Pendapatan_Gabungan_" + labelFile + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".csv";

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan Laporan Pendapatan Gabungan (CSV)");
        chooser.setSelectedFile(new File(defaultName));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV File (*.csv)", "csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".csv")) {
            target = new File(target.getParentFile(), target.getName() + ".csv");
        }

        try (Writer out = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
            out.write('\uFEFF');
            out.write("LAPORAN PENDAPATAN GABUNGAN (KAMAR & MENU F&B)\n");
            out.write("Periode;" + escapeCsv(getPeriodeKeterangan()) + "\n");
            out.write("Tanggal Cetak;" + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()) + "\n\n");

            out.write("Periode;Reservasi Kamar (Trx);Pendapatan Kamar (Rp);Porsi Menu Terjual;Pendapatan Menu F&B (Rp);Total Gabungan (Rp)\n");
            int totalTrxKamar = 0;
            int totalPorsiMenu = 0;

            for (GabunganRow r : lastRows) {
                totalTrxKamar += r.trxKamar;
                totalPorsiMenu += r.porsiMenu;
                out.write(String.format(Locale.US, "%s;%d;%.0f;%d;%.0f;%.0f%n",
                        escapeCsv(r.periode),
                        r.trxKamar,
                        r.pendapatanKamar,
                        r.porsiMenu,
                        r.pendapatanMenu,
                        r.totalGabungan
                ));
            }

            out.write(String.format(Locale.US, "TOTAL AKUMULASI;%d;%.0f;%d;%.0f;%.0f%n",
                    totalTrxKamar,
                    lastTotalKamar,
                    totalPorsiMenu,
                    lastTotalMenu,
                    lastGrandTotal
            ));

            JOptionPane.showMessageDialog(this, "Laporan CSV berhasil disimpan ke:\n" + target.getAbsolutePath(),
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan file CSV: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(";") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    /** Export laporan gabungan Kamar + Menu F&B ke file PDF */
    private void exportLaporanPdf() {
        if (lastRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada data laporan untuk diunduh.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String labelFile = getPeriodeKeterangan().replaceAll("[^a-zA-Z0-9_-]", "_");
        String defaultName = "Laporan_Pendapatan_Gabungan_" + labelFile + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".pdf";

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan Laporan Pendapatan Gabungan (PDF)");
        chooser.setSelectedFile(new File(defaultName));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Document (*.pdf)", "pdf"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".pdf")) {
            target = new File(target.getParentFile(), target.getName() + ".pdf");
        }

        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("Laporan Pendapatan Gabungan");

            PrintService pdfService = null;
            for (PrintService ps : PrintServiceLookup.lookupPrintServices(null, null)) {
                String name = ps.getName().toLowerCase();
                if (name.contains("pdf") || name.contains("print to file")) {
                    pdfService = ps;
                    break;
                }
            }
            if (pdfService != null) {
                job.setPrintService(pdfService);
            }

            PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
            attrs.add(new Destination(target.toURI()));

            PageFormat pageFormat = job.defaultPage();
            pageFormat.setOrientation(PageFormat.LANDSCAPE);

            job.setPrintable(createPdfPrintable(), pageFormat);
            job.print(attrs);

            JOptionPane.showMessageDialog(this, "Laporan PDF berhasil dibuat di:\n" + target.getAbsolutePath(),
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this,
                    "Tidak dapat mencetak ke PDF secara otomatis (" + ex.getMessage() + ").\n" +
                            "Silakan pilih printer 'Microsoft Print to PDF' pada dialog cetak.",
                    "Info Cetak PDF", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Printable createPdfPrintable() {
        return (graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;

            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double x = pageFormat.getImageableX() + 18;
            double y = pageFormat.getImageableY() + 20;
            double width = pageFormat.getImageableWidth() - 36;

            g2.setColor(COLOR_NAVY);
            g2.setFont(new Font("Serif", Font.BOLD, 17));
            g2.drawString("HOMESTAYSAFE - LAPORAN PENDAPATAN GABUNGAN (KAMAR & MENU F&B)", (int) x, (int) y);

            y += 18;
            g2.setColor(new Color(100, 116, 139));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String meta = "Periode: " + getPeriodeKeterangan() + "   |   Dicetak: " +
                    new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
            g2.drawString(meta, (int) x, (int) y);

            y += 18;
            g2.setColor(COLOR_NAVY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString(String.format("Total Kamar: Rp %,.0f    |    Total Menu F&B: Rp %,.0f    |    Akumulasi Gabungan: Rp %,.0f",
                    lastTotalKamar, lastTotalMenu, lastGrandTotal), (int) x, (int) y);

            y += 16;
            String[] headers = {"Periode", "Res. Kamar", "Pendapatan Kamar", "Porsi Menu", "Pendapatan Menu", "Total Gabungan"};
            double[] colWidths = {
                    width * 0.16,
                    width * 0.12,
                    width * 0.18,
                    width * 0.12,
                    width * 0.18,
                    width * 0.24
            };

            int rowH = 20;
            g2.setColor(COLOR_NAVY);
            g2.fillRect((int) x, (int) y, (int) width, rowH);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));

            double curX = x;
            for (int c = 0; c < headers.length; c++) {
                g2.drawString(headers[c], (int) curX + 6, (int) y + 14);
                curX += colWidths[c];
            }
            y += rowH;

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 0; i < lastRows.size(); i++) {
                if (y + rowH > pageFormat.getImageableY() + pageFormat.getImageableHeight() - 10) break;
                GabunganRow r = lastRows.get(i);

                g2.setColor(i % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                g2.fillRect((int) x, (int) y, (int) width, rowH);
                g2.setColor(COLOR_BORDER);
                g2.drawRect((int) x, (int) y, (int) width, rowH);

                String[] vals = {
                        r.periode,
                        r.trxKamar + " Trx",
                        String.format("Rp %,.0f", r.pendapatanKamar),
                        r.porsiMenu + " Porsi",
                        String.format("Rp %,.0f", r.pendapatanMenu),
                        String.format("Rp %,.0f", r.totalGabungan)
                };

                curX = x;
                for (int c = 0; c < vals.length; c++) {
                    g2.setColor(c == 5 ? COLOR_EMERALD : COLOR_NAVY);
                    g2.drawString(vals[c], (int) curX + 6, (int) y + 14);
                    curX += colWidths[c];
                }
                y += rowH;
            }

            g2.dispose();
            return Printable.PAGE_EXISTS;
        };
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

            int gridSteps = 4;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int step = 0; step <= gridSteps; step++) {
                int yGrid = padTop + chartH - (int) ((step / (double) gridSteps) * chartH);

                g2.setColor(step == 0 ? new Color(148, 163, 184) : new Color(241, 245, 249));
                g2.drawLine(padLeft, yGrid, padLeft + chartW, yGrid);

                double valRp = (maxBar * step) / gridSteps;
                String lblLeft = formatCompactRp(valRp);
                FontMetrics fmL = g2.getFontMetrics();
                g2.setColor(new Color(71, 85, 105));
                g2.drawString(lblLeft, padLeft - fmL.stringWidth(lblLeft) - 6, yGrid + 4);

                int valCount = (int) Math.round((maxLine * step) / (double) gridSteps);
                String lblRight = String.valueOf(valCount);
                g2.setColor(lineColor);
                g2.drawString(lblRight, padLeft + chartW + 6, yGrid + 4);
            }

            g2.setColor(new Color(203, 213, 225));
            g2.drawLine(padLeft, padTop, padLeft, padTop + chartH);
            g2.drawLine(padLeft + chartW, padTop, padLeft + chartW, padTop + chartH);

            int n = items.size();
            int slotW = chartW / n;
            int barW = Math.min(44, Math.max(12, (int) (slotW * 0.56)));

            int[] lineX = new int[n];
            int[] lineY = new int[n];

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

                if (slotW >= 34 && item.getTotalPendapatan() > 0) {
                    g2.setColor(new Color(30, 41, 59));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    String barValText = formatCompactRp(item.getTotalPendapatan());
                    FontMetrics fmVal = g2.getFontMetrics();
                    g2.drawString(barValText, centerX - fmVal.stringWidth(barValText) / 2, Math.max(padTop + 10, barY - 4));
                }

                int pointH = (int) Math.round((item.getJumlahTransaksi() / (double) maxLine) * (chartH - 14));
                lineX[i] = centerX;
                lineY[i] = padTop + chartH - pointH;

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