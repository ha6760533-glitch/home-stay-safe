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

public class CheckInPanel extends JPanel {

    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    private final DefaultTableModel model;
    private final ResepsionisDAO dao = new ResepsionisDAO();
    private final JLabel lblSiapCheckIn = new JLabel("0 Tamu");

    public CheckInPanel(JPanel contentPanel, CardLayout cardLayout) {
        setLayout(new BorderLayout(15, 16));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel northContainer = new JPanel(new BorderLayout(0, 12));
        northContainer.setOpaque(false);

        JPanel topHeader = new JPanel(new BorderLayout(0, 10));
        topHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Proses Check-In & Penyerahan Kunci Kamar");
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

        JPanel statCard = new JPanel(new GridLayout(2, 1, 2, 4));
        statCard.setBackground(Color.WHITE);
        statCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));
        JLabel t = new JLabel("Reservasi Disetujui & Siap Check-In");
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(new Color(100, 116, 139));
        lblSiapCheckIn.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblSiapCheckIn.setForeground(new Color(5, 150, 105));
        statCard.add(t);
        statCard.add(lblSiapCheckIn);

        northContainer.add(topHeader, BorderLayout.NORTH);
        northContainer.add(statCard, BorderLayout.SOUTH);

        String[] cols = {"ID Booking", "Username", "Nama Tamu", "No Identitas", "No Kamar", "Tipe", "Check-in", "Status Booking"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
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

        DefaultTableCellRenderer centerRenderer = createBaseRenderer(SwingConstants.CENTER);
        DefaultTableCellRenderer leftRenderer = createBaseRenderer(SwingConstants.LEFT);
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(new Color(5, 150, 105));
                if (!sel) lbl.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                return lbl;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(statusRenderer);

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

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);

        JButton btnCheckIn = createStyledButton("Proses Check-in & Serahkan Kunci", COLOR_GOLD, Color.WHITE, 270, 42);
        btnCheckIn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int idRes = Integer.parseInt(table.getValueAt(row, 0).toString().replace("RSV-", ""));
                String noKamar = table.getValueAt(row, 4).toString();
                if (dao.updateStatusReservasi(idRes, "Check-in")) {
                    JOptionPane.showMessageDialog(this, "Check-in Berhasil!\nStatus Kamar " + noKamar + " diubah jadi TERISI.\nSilakan serahkan kunci kepada Tamu.", "Check-In Sukses", JOptionPane.INFORMATION_MESSAGE);
                    loadData(txtSearch.getText());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Pilih salah satu baris transaksi yang sudah Disetujui!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        });

        bottom.add(btnCheckIn);

        add(northContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadData(String keyword) {
        model.setRowCount(0);
        int count = 0;
        List<ResepsionisDAO.ReservasiDetail> list = dao.getDaftarReservasi(keyword);
        for (ResepsionisDAO.ReservasiDetail r : list) {
            if (r.getStatusReservasi() != null && r.getStatusReservasi().equalsIgnoreCase("Disetujui")) {
                count++;
                model.addRow(new Object[]{
                        "RSV-" + r.getIdReservasi(),
                        r.getUsername(),
                        r.getNamaTamu(),
                        r.getNoIdentitas(),
                        r.getNoKamar(),
                        r.getTipeKamar(),
                        r.getTglCheckin(),
                        r.getStatusReservasi()
                });
            }
        }
        lblSiapCheckIn.setText(count + " Tamu Siap Check-In");
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
}