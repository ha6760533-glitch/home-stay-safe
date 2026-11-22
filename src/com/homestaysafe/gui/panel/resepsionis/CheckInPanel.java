package com.homestaysafe.gui.panel.resepsionis;

import com.homestaysafe.dao.ResepsionisDAO;
import com.homestaysafe.util.UIHelper;

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
    private List<ResepsionisDAO.ReservasiDetail> currentList;
    private final JLabel lblSiapCheckIn = new JLabel("0 Tamu");

    public CheckInPanel(JPanel contentPanel, CardLayout cardLayout) {
        setLayout(new BorderLayout(15, 16));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel northContainer = new JPanel(new BorderLayout(0, 12));
        northContainer.setOpaque(false);

        JPanel topHeader = new JPanel(new BorderLayout(0, 10));
        topHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Proses Check-In & Penetapan Nomor Kamar");
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
        txtSearch.setBorder(UIHelper.createRoundedFieldBorder(new Color(203, 213, 225), 12));

        JButton btnSearch = UIHelper.createRoundedButton("Cari", COLOR_NAVY, UIHelper.COLOR_NAVY_HOVER, Color.WHITE, 12);
        btnSearch.setPreferredSize(new Dimension(85, 36));

        JButton btnReset = UIHelper.createRoundedButton("Refresh", new Color(226, 232, 240), new Color(203, 213, 225), COLOR_NAVY, 12);
        btnReset.setPreferredSize(new Dimension(90, 36));

        searchPanel.add(lblCari);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);

        topHeader.add(lblTitle, BorderLayout.NORTH);
        topHeader.add(searchPanel, BorderLayout.SOUTH);

        JPanel statCard = UIHelper.createRoundedCard(14, 12, 16);
        statCard.setLayout(new GridLayout(2, 1, 2, 4));
        JLabel t = new JLabel("Reservasi Disetujui & Siap Check-In");
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(new Color(100, 116, 139));
        lblSiapCheckIn.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblSiapCheckIn.setForeground(new Color(5, 150, 105));
        statCard.add(t);
        statCard.add(lblSiapCheckIn);

        northContainer.add(topHeader, BorderLayout.NORTH);
        northContainer.add(statCard, BorderLayout.SOUTH);

        String[] cols = {"ID Booking", "Username", "Nama Tamu", "No Identitas", "Tipe Kamar", "No Kamar", "Check-in", "Status Booking"};
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

        JButton btnCheckIn = UIHelper.createRoundedButton("Pilih Kamar & Proses Check-In", COLOR_GOLD, UIHelper.COLOR_GOLD_HOVER, Color.WHITE, 14);
        btnCheckIn.setPreferredSize(new Dimension(265, 42));
        btnCheckIn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih salah satu baris transaksi yang sudah Disetujui!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int idRes = Integer.parseInt(table.getValueAt(row, 0).toString().replace("RSV-", ""));
            ResepsionisDAO.ReservasiDetail detail = currentList.stream()
                    .filter(r -> r.getIdReservasi() == idRes)
                    .findFirst()
                    .orElse(null);
            if (detail != null) {
                showCheckInDialog(detail, txtSearch.getText());
            }
        });

        bottom.add(btnCheckIn);

        add(northContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void showCheckInDialog(ResepsionisDAO.ReservasiDetail detail, String currentKeyword) {
        List<String> kamarKosong = dao.getNomorKamarKosongByTipe(
                detail.getTipeKamar(),
                detail.getTglCheckin(),
                detail.getTglCheckout()
        );

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Check-In & Penetapan Kamar RSV-" + detail.getIdReservasi(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(460, 410);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel container = new JPanel(new BorderLayout(15, 15));
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel headerBox = new JPanel(new GridLayout(2, 1, 0, 2));
        headerBox.setBackground(COLOR_NAVY);
        headerBox.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel lblHeader = new JLabel("CHECK-IN & PENYERAHAN KUNCI KAMAR", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Serif", Font.BOLD, 15));
        lblHeader.setForeground(COLOR_GOLD);

        JLabel lblSub = new JLabel("Kode Reservasi: RSV-" + detail.getIdReservasi(), SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.WHITE);

        headerBox.add(lblHeader);
        headerBox.add(lblSub);

        JPanel infoGrid = new JPanel(new GridLayout(6, 2, 10, 10));
        infoGrid.setBackground(new Color(248, 250, 252));
        infoGrid.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(15, 16, 15, 16)
        ));

        infoGrid.add(makeBoldLabel("Nama Tamu:"));
        infoGrid.add(new JLabel(detail.getNamaTamu() + " (" + detail.getUsername() + ")"));
        infoGrid.add(makeBoldLabel("No. Identitas:"));
        infoGrid.add(new JLabel(detail.getNoIdentitas()));
        infoGrid.add(makeBoldLabel("Jadwal Menginap:"));
        infoGrid.add(new JLabel(detail.getTglCheckin() + " s/d " + detail.getTglCheckout()));
        infoGrid.add(makeBoldLabel("Tipe Kamar Dipesan:"));
        infoGrid.add(new JLabel(detail.getTipeKamar()));
        infoGrid.add(makeBoldLabel("Tetapkan Nomor Kamar:"));

        JComboBox<String> cmbNoKamar = new JComboBox<>();
        cmbNoKamar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        if (kamarKosong.isEmpty()) {
            cmbNoKamar.addItem("-- Tidak ada kamar " + detail.getTipeKamar() + " tersedia --");
            cmbNoKamar.setEnabled(false);
        } else {
            for (String no : kamarKosong) {
                cmbNoKamar.addItem(no);
            }
            if (detail.getNoKamar() != null && !detail.getNoKamar().equals("-")) {
                cmbNoKamar.setSelectedItem(detail.getNoKamar());
            }
        }
        infoGrid.add(cmbNoKamar);

        infoGrid.add(makeBoldLabel("Status Saat Ini:"));
        infoGrid.add(new JLabel(detail.getStatusReservasi()));

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        btnPanel.setOpaque(false);

        JButton btnBatal = UIHelper.createRoundedButton("Batal", new Color(226, 232, 240), new Color(203, 213, 225), COLOR_NAVY, 12);
        btnBatal.setPreferredSize(new Dimension(160, 40));

        JButton btnProses = UIHelper.createRoundedButton("Konfirmasi Check-In", new Color(5, 150, 105), new Color(4, 120, 87), Color.WHITE, 12);
        btnProses.setPreferredSize(new Dimension(190, 40));
        btnProses.setEnabled(!kamarKosong.isEmpty());

        btnBatal.addActionListener(e -> dialog.dispose());

        btnProses.addActionListener(e -> {
            String noKamarPilihan = (String) cmbNoKamar.getSelectedItem();
            if (noKamarPilihan == null || kamarKosong.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Tidak ada kamar kosong untuk tipe " + detail.getTipeKamar() + "!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (dao.checkInDanTetapkanKamar(detail.getIdReservasi(), noKamarPilihan)) {
                JOptionPane.showMessageDialog(dialog,
                        "Check-in Berhasil!\n\nNomor Kamar ditetapkan: No. " + noKamarPilihan +
                                "\nStatus Kamar diubah menjadi TERISI.\nSilakan serahkan kunci kepada Tamu.",
                        "Check-In Sukses", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadData(currentKeyword);
            }
        });

        btnPanel.add(btnBatal);
        btnPanel.add(btnProses);

        container.add(headerBox, BorderLayout.NORTH);
        container.add(infoGrid, BorderLayout.CENTER);
        container.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(container);
        dialog.setVisible(true);
    }

    private void loadData(String keyword) {
        model.setRowCount(0);
        int count = 0;
        currentList = dao.getDaftarReservasi(keyword);
        for (ResepsionisDAO.ReservasiDetail r : currentList) {
            if (r.getStatusReservasi() != null && r.getStatusReservasi().equalsIgnoreCase("Disetujui")) {
                count++;
                String tampilKamar = (r.getNoKamar() == null || r.getNoKamar().equals("-")) ? "Belum ditetapkan" : r.getNoKamar();
                model.addRow(new Object[]{
                        "RSV-" + r.getIdReservasi(),
                        r.getUsername(),
                        r.getNamaTamu(),
                        r.getNoIdentitas(),
                        r.getTipeKamar(),
                        tampilKamar,
                        r.getTglCheckin(),
                        r.getStatusReservasi()
                });
            }
        }
        lblSiapCheckIn.setText(count + " Tamu Siap Check-In");
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