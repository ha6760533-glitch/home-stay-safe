package com.homestaysafe.gui.panel.resepsionis;

import com.homestaysafe.dao.ResepsionisDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class VerifikasiBookingPanel extends JPanel {

    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    private final DefaultTableModel model;
    private final ResepsionisDAO dao = new ResepsionisDAO();
    private List<ResepsionisDAO.ReservasiDetail> currentList;

    private final JLabel lblCountPending = new JLabel("0 Pemesanan");
    private final JLabel lblTotalNominal = new JLabel("Rp 0");

    public VerifikasiBookingPanel(JPanel contentPanel, CardLayout cardLayout) {
        setLayout(new BorderLayout(15, 16));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel northContainer = new JPanel(new BorderLayout(0, 12));
        northContainer.setOpaque(false);

        JPanel topHeader = new JPanel(new BorderLayout(0, 10));
        topHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Verifikasi & Konfirmasi Booking Online");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_NAVY);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

        JLabel lblCari = new JLabel("Cari:");
        lblCari.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCari.setForeground(COLOR_NAVY);

        JTextField txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(220, 36));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JButton btnSearch = createStyledButton("Cari", COLOR_NAVY, Color.WHITE, 80, 36);
        JButton btnReset = createStyledButton("Refresh", Color.WHITE, COLOR_NAVY, 85, 36);
        btnReset.setBorder(new LineBorder(new Color(203, 213, 225), 1, true));
        btnReset.setBorderPainted(true);

        searchPanel.add(lblCari);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);

        topHeader.add(lblTitle, BorderLayout.NORTH);
        topHeader.add(searchPanel, BorderLayout.SOUTH);

        JPanel statsRow = new JPanel(new GridLayout(1, 2, 15, 0));
        statsRow.setOpaque(false);
        statsRow.add(createStatCard("Antrean Menunggu Verifikasi", lblCountPending, new Color(217, 119, 6)));
        statsRow.add(createStatCard("Total Nilai Reservasi Pending", lblTotalNominal, COLOR_NAVY));

        northContainer.add(topHeader, BorderLayout.NORTH);
        northContainer.add(statsRow, BorderLayout.SOUTH);

        String[] columns = {"ID Booking", "Username", "Nama Tamu", "No Identitas", "Kamar Dipesan", "Check-in", "Check-out", "Total Biaya", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        styleModernTable(table);

        DefaultTableCellRenderer centerRenderer = createBaseRenderer(SwingConstants.CENTER);
        DefaultTableCellRenderer leftRenderer = createBaseRenderer(SwingConstants.LEFT);
        DefaultTableCellRenderer rightRenderer = createBaseRenderer(SwingConstants.RIGHT);

        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(new Color(217, 119, 6));
                if (!sel) lbl.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                return lbl;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(8).setCellRenderer(statusRenderer);

        loadData("");

        btnSearch.addActionListener(e -> loadData(txtSearch.getText()));
        txtSearch.addActionListener(e -> loadData(txtSearch.getText()));
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadData("");
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionPanel.setOpaque(false);

        JButton btnKonfirmasi = createStyledButton("Periksa & Konfirmasi Pemesanan", COLOR_GOLD, Color.WHITE, 265, 42);
        btnKonfirmasi.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih salah satu baris pemesanan pada tabel terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int idRes = Integer.parseInt(table.getValueAt(row, 0).toString().replace("RSV-", ""));
            ResepsionisDAO.ReservasiDetail detail = currentList.stream()
                    .filter(r -> r.getIdReservasi() == idRes)
                    .findFirst()
                    .orElse(null);
            if (detail != null) {
                showKonfirmasiDialog(detail, txtSearch.getText());
            }
        });

        actionPanel.add(btnKonfirmasi);

        add(northContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void showKonfirmasiDialog(ResepsionisDAO.ReservasiDetail detail, String currentKeyword) {
        if (!detail.getStatusReservasi().trim().equalsIgnoreCase("Pending")) {
            JOptionPane.showMessageDialog(this,
                    "Pemesanan RSV-" + detail.getIdReservasi() + " sudah berstatus '" + detail.getStatusReservasi() + "' dan tidak dapat diubah lagi!",
                    "Status Terkunci", JOptionPane.WARNING_MESSAGE);
            loadData(currentKeyword);
            return;
        }

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Verifikasi Booking RSV-" + detail.getIdReservasi(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(460, 430);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel container = new JPanel(new BorderLayout(15, 15));
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel headerBox = new JPanel(new GridLayout(2, 1, 0, 2));
        headerBox.setBackground(COLOR_NAVY);
        headerBox.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel lblHeader = new JLabel("DETAIL VERIFIKASI PEMESANAN TAMU", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Serif", Font.BOLD, 16));
        lblHeader.setForeground(COLOR_GOLD);

        JLabel lblSub = new JLabel("Kode Reservasi: RSV-" + detail.getIdReservasi(), SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.WHITE);

        headerBox.add(lblHeader);
        headerBox.add(lblSub);

        JPanel infoGrid = new JPanel(new GridLayout(8, 2, 10, 10));
        infoGrid.setBackground(new Color(248, 250, 252));
        infoGrid.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(15, 16, 15, 16)
        ));

        infoGrid.add(makeBoldLabel("ID Booking:"));
        infoGrid.add(new JLabel("RSV-" + detail.getIdReservasi()));
        infoGrid.add(makeBoldLabel("Username Akun:"));
        infoGrid.add(new JLabel(detail.getUsername()));
        infoGrid.add(makeBoldLabel("Nama Lengkap Tamu:"));
        infoGrid.add(new JLabel(detail.getNamaTamu()));
        infoGrid.add(makeBoldLabel("No. Identitas (NIK/KTP):"));
        infoGrid.add(new JLabel(detail.getNoIdentitas()));
        infoGrid.add(makeBoldLabel("Kamar yang Dipesan:"));
        infoGrid.add(new JLabel("No. " + detail.getNoKamar() + " (" + detail.getTipeKamar() + ")"));
        infoGrid.add(makeBoldLabel("Jadwal Menginap:"));
        infoGrid.add(new JLabel(detail.getTglCheckin() + " s/d " + detail.getTglCheckout()));
        infoGrid.add(makeBoldLabel("Total Biaya Kamar:"));
        JLabel lblHarga = new JLabel(String.format("Rp %,.0f", detail.getTotalHarga()));
        lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHarga.setForeground(new Color(5, 150, 105));
        infoGrid.add(lblHarga);
        infoGrid.add(makeBoldLabel("Status Saat Ini:"));
        infoGrid.add(new JLabel(detail.getStatusReservasi()));

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        btnPanel.setOpaque(false);

        JButton btnTolak = createStyledButton("Ditolak / Koreksi", new Color(220, 38, 38), Color.WHITE, 180, 40);
        JButton btnSetuju = createStyledButton("Disetujui (Valid)", new Color(5, 150, 105), Color.WHITE, 180, 40);

        btnSetuju.addActionListener(e -> {
            if (dao.updateStatusReservasi(detail.getIdReservasi(), "Disetujui")) {
                JOptionPane.showMessageDialog(dialog, "Pemesanan RSV-" + detail.getIdReservasi() + " telah DISETUJUI.", "Verifikasi Valid", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadData(currentKeyword);
            }
        });

        btnTolak.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Yakin ingin menolak pemesanan RSV-" + detail.getIdReservasi() + "?",
                    "Konfirmasi Penolakan", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION && dao.updateStatusReservasi(detail.getIdReservasi(), "Ditolak")) {
                JOptionPane.showMessageDialog(dialog, "Pemesanan RSV-" + detail.getIdReservasi() + " telah DITOLAK.", "Pemesanan Ditolak", JOptionPane.WARNING_MESSAGE);
                dialog.dispose();
                loadData(currentKeyword);
            }
        });

        btnPanel.add(btnTolak);
        btnPanel.add(btnSetuju);

        container.add(headerBox, BorderLayout.NORTH);
        container.add(infoGrid, BorderLayout.CENTER);
        container.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(container);
        dialog.setVisible(true);
    }

    private void loadData(String keyword) {
        model.setRowCount(0);
        currentList = dao.getDaftarReservasi(keyword);
        int count = 0;
        double total = 0;

        for (ResepsionisDAO.ReservasiDetail r : currentList) {
            String st = r.getStatusReservasi() != null ? r.getStatusReservasi().trim() : "";
            if (st.equalsIgnoreCase("Pending")) {
                count++;
                total += r.getTotalHarga();
                model.addRow(new Object[]{
                        "RSV-" + r.getIdReservasi(),
                        r.getUsername(),
                        r.getNamaTamu(),
                        r.getNoIdentitas(),
                        "No. " + r.getNoKamar() + " (" + r.getTipeKamar() + ")",
                        r.getTglCheckin(),
                        r.getTglCheckout(),
                        String.format("Rp %,.0f", r.getTotalHarga()),
                        st
                });
            }
        }
        lblCountPending.setText(count + " Pemesanan");
        lblTotalNominal.setText(String.format("Rp %,.0f", total));
    }

    private JButton createStyledButton(String text, Color bg, Color fg, int w, int h) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setPreferredSize(new Dimension(w, h));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createStatCard(String title, JLabel valLabel, Color accent) {
        JPanel card = new JPanel(new GridLayout(2, 1, 2, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(new Color(100, 116, 139));
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        valLabel.setForeground(accent);
        card.add(t);
        card.add(valLabel);
        return card;
    }

    private void styleModernTable(JTable table) {
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(new Color(241, 245, 249));
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(226, 232, 240));
        table.setSelectionForeground(COLOR_NAVY);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(val != null ? val.toString() : "");
                lbl.setOpaque(true);
                lbl.setBackground(COLOR_NAVY);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                return lbl;
            }
        });
    }

    private DefaultTableCellRenderer createBaseRenderer(int align) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                setHorizontalAlignment(align);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!sel) comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                return comp;
            }
        };
    }

    private JLabel makeBoldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_NAVY);
        return lbl;
    }
}