package com.homestaysafe.gui.panel.resepsionis;

import com.homestaysafe.util.UIHelper;

import com.homestaysafe.dao.ResepsionisDAO;
import com.homestaysafe.model.Reservasi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CheckOutPanel extends JPanel {

    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    private final ResepsionisDAO dao = new ResepsionisDAO();
    private final DefaultTableModel model;
    private List<ResepsionisDAO.ReservasiDetail> currentList;

    private final JLabel lblKamarAktif = new JLabel("0 Kamar");
    private final JLabel lblTotalFBBerjalan = new JLabel("Rp 0");
    private final JLabel lblEstimasiTotal = new JLabel("Rp 0");

    public CheckOutPanel(JPanel contentPanel, CardLayout cardLayout) {
        setLayout(new BorderLayout(15, 16));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel north = new JPanel(new BorderLayout(0, 12));
        north.setOpaque(false);

        JPanel topHeader = new JPanel(new BorderLayout(0, 10));
        topHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Proses Check-Out & Kalkulasi Pembayaran Akhir");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_NAVY);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);

        JLabel lblCari = new JLabel("Cari:");
        lblCari.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCari.setForeground(COLOR_NAVY);

        JTextField txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(220, 36));
        UIHelper.styleField(txtSearch);

        JButton btnSearch = UIHelper.createRoundedButton("Cari", COLOR_NAVY, UIHelper.COLOR_NAVY_HOVER, Color.WHITE, 12);
        btnSearch.setPreferredSize(new Dimension(85, 36));

        JButton btnReset = UIHelper.createRoundedButton("Refresh", new Color(226, 232, 240), new Color(203, 213, 225), COLOR_NAVY, 12);
        btnReset.setPreferredSize(new Dimension(90, 36));

        searchBar.add(lblCari);
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnReset);

        topHeader.add(lblTitle, BorderLayout.NORTH);
        topHeader.add(searchBar, BorderLayout.SOUTH);

        JPanel stats = new JPanel(new GridLayout(1, 3, 15, 0));
        stats.setOpaque(false);
        stats.add(makeCard("Tamu Sedang Menginap", lblKamarAktif, COLOR_NAVY));
        stats.add(makeCard("Total Tagihan F&B Aktif", lblTotalFBBerjalan, new Color(180, 83, 9)));
        stats.add(makeCard("Total Tagihan Berjalan", lblEstimasiTotal, new Color(5, 150, 105)));

        north.add(topHeader, BorderLayout.NORTH);
        north.add(stats, BorderLayout.SOUTH);

        String[] cols = {"ID Transaksi", "Nama Tamu", "No Kamar", "Batas Check-Out", "Biaya Kamar", "Tagihan F&B", "Total Sementara", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
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
                lbl.setBorder(new EmptyBorder(0, 10, 0, 12));
                if (val instanceof Double) {
                    lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                    lbl.setText(String.format("Rp %,.0f", (Double) val));
                } else {
                    lbl.setHorizontalAlignment(c == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
                }
                lbl.setForeground(c == 7 ? new Color(37, 99, 235) : COLOR_NAVY);
                lbl.setFont(new Font("Segoe UI", c == 7 ? Font.BOLD : Font.PLAIN, 13));
                return lbl;
            }
        };
        for (int i = 0; i < cols.length; i++) table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);

        loadData("");
        btnSearch.addActionListener(e -> loadData(txtSearch.getText()));
        txtSearch.addActionListener(e -> loadData(txtSearch.getText()));
        btnReset.addActionListener(e -> { txtSearch.setText(""); loadData(""); });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        JButton btnCheckOut = UIHelper.createRoundedButton("Hitung Denda, Checkout & Cetak Nota", COLOR_GOLD, UIHelper.COLOR_GOLD_HOVER, Color.WHITE, 14);
        btnCheckOut.setPreferredSize(new Dimension(295, 42));
        btnCheckOut.addActionListener(e -> prosesAksiCheckOut(table, contentPanel, cardLayout));
        bottom.add(btnCheckOut);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);

        add(north, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadData(String keyword) {
        model.setRowCount(0);
        int count = 0;
        double sumFB = 0, sumAll = 0;
        currentList = dao.getDaftarReservasi(keyword);

        for (ResepsionisDAO.ReservasiDetail r : currentList) {
            if (r.getStatusReservasi() != null && r.getStatusReservasi().toLowerCase().contains("check-in")) {
                double fb = dao.getTagihanMakanan(r.getIdReservasi());
                double sub = r.getTotalHarga() + fb;
                count++;
                sumFB += fb;
                sumAll += sub;

                model.addRow(new Object[]{
                        "RSV-" + r.getIdReservasi(),
                        r.getNamaTamu(),
                        r.getNoKamar(),
                        r.getTglCheckout() + " 12:00",
                        r.getTotalHarga(),
                        fb,
                        sub,
                        r.getStatusReservasi()
                });
            }
        }
        lblKamarAktif.setText(count + " Kamar");
        lblTotalFBBerjalan.setText(String.format("Rp %,.0f", sumFB));
        lblEstimasiTotal.setText(String.format("Rp %,.0f", sumAll));
    }

    private void prosesAksiCheckOut(JTable table, JPanel contentPanel, CardLayout cardLayout) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih transaksi tamu yang akan Check-Out!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idRes = Integer.parseInt(table.getValueAt(row, 0).toString().replace("RSV-", ""));
        ResepsionisDAO.ReservasiDetail detail = currentList.stream()
                .filter(r -> r.getIdReservasi() == idRes)
                .findFirst()
                .orElse(null);

        if (detail == null) return;

        double fb = dao.getTagihanMakanan(idRes);
        List<ResepsionisDAO.RincianFBItem> items = dao.getRincianMakanan(idRes);

        Reservasi reservasi = buatReservasi(detail, fb);
        String nama = reservasi.getNamaTamu();
        String noKamar = reservasi.getNoKamar();
        double kamar = reservasi.getTotalBiaya();

        LocalDateTime batasCheckOut = reservasi.getBatasCheckout();
        LocalDateTime waktuSekarang = LocalDateTime.now();

        long jamTelat = reservasi.hitungJamTelat(waktuSekarang);
        double dendaTelat = reservasi.hitungDenda(waktuSekarang);
        reservasi.setDenda(dendaTelat);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String statusWaktu = jamTelat > 0
                ? String.format("TERLAMBAT %d JAM (Denda Otomatis: Rp %,.0f @ Rp %,.0f/jam)", jamTelat, dendaTelat, Reservasi.TARIF_DENDA_PER_JAM)
                : "TEPAT WAKTU (Denda Keterlambatan: Rp 0)";

        String pesanKonfirmasi = String.format(
                "Batas Check-Out : %s\n" +
                        "Waktu Aktual    : %s\n" +
                        "Status Waktu    : %s\n\n" +
                        "Biaya Kamar     : Rp %,.0f\n" +
                        "Tagihan F&B     : Rp %,.0f\n" +
                        "Denda Telat     : Rp %,.0f\n\n" +
                        "Tambahan Denda Kerusakan Fasilitas (Rp):\n(Isi 0 jika tidak ada kerusakan)",
                batasCheckOut.format(fmt), waktuSekarang.format(fmt), statusWaktu,
                kamar, fb, dendaTelat
        );

        String inDendaKerusakan = JOptionPane.showInputDialog(this, pesanKonfirmasi, "0");
        if (inDendaKerusakan == null) return;

        double dendaKerusakan = 0;
        try {
            dendaKerusakan = Math.max(0, Double.parseDouble(inDendaKerusakan.trim()));
        } catch (Exception ignored) {}

        reservasi.setDendaKerusakan(dendaKerusakan);
        double grandTotal = reservasi.hitungGrandTotal();

        if (dao.prosesCheckOut(idRes, grandTotal)) {
            reservasi.setStatusReservasi("Selesai");
            reservasi.setTglCheckoutAktual(waktuSekarang);
            showNotaDialog(reservasi, idRes, nama, noKamar, kamar, fb, jamTelat, dendaTelat, dendaKerusakan, grandTotal, items);
            contentPanel.add(new CheckOutPanel(contentPanel, cardLayout), "CheckOut");
            cardLayout.show(contentPanel, "CheckOut");
        }
    }

    private Reservasi buatReservasi(ResepsionisDAO.ReservasiDetail d, double tagihanFB) {
        Reservasi r = new Reservasi();
        r.setIdReservasi(d.getIdReservasi());
        r.setNamaTamu(d.getNamaTamu());
        r.setNoKamar(d.getNoKamar());
        r.setTipeKamar(d.getTipeKamar());
        if (d.getTglCheckin() != null && d.getTglCheckin().length() >= 10) {
            r.setTglCheckin(LocalDate.parse(d.getTglCheckin().substring(0, 10)));
        }
        r.setTglCheckoutRencana(LocalDate.parse(d.getTglCheckout().substring(0, 10)));
        r.setTotalBiaya(d.getTotalHarga());
        r.setTotalMakanan(tagihanFB);
        r.setStatusReservasi(d.getStatusReservasi());
        return r;
    }

    private void showNotaDialog(Reservasi reservasi, int idRes, String nama, String noKamar, double kamar, double fb,
                                long jamTelat, double dendaTelat, double dendaKerusakan,
                                double total, List<ResepsionisDAO.RincianFBItem> items) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Nota Resmi - RSV-" + idRes, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(560, 580);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        String waktu = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
        JLabel hdr = new JLabel("<html><center><b style='font-size:14px;color:#c5a880;'>HOMESTAYSAFE RESORT</b><br>" +
                "<span style='color:white;font-size:10px;'>NOTA PEMBAYARAN CHECK-OUT • RSV-" + idRes + " (" + waktu + ")</span></center></html>", SwingConstants.CENTER);
        hdr.setOpaque(true);
        hdr.setBackground(COLOR_NAVY);
        hdr.setBorder(new EmptyBorder(14, 10, 14, 10));

        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
        grid.setBackground(new Color(248, 250, 252));
        grid.setBorder(BorderFactory.createCompoundBorder(new LineBorder(COLOR_BORDER), new EmptyBorder(14, 16, 14, 16)));

        addPair(grid, "Nama Tamu", nama, false);
        addPair(grid, "Nomor Kamar", "Kamar " + noKamar, false);
        addPair(grid, "Biaya Sewa Kamar", String.format("Rp %,.0f", kamar), true);

        for (ResepsionisDAO.RincianFBItem it : items) {
            addPair(grid, "  • " + it.getNamaMenu() + " (x" + it.getJumlah() + ")", String.format("Rp %,.0f", it.getSubtotal()), false);
        }
        addPair(grid, "Subtotal F&B", String.format("Rp %,.0f", fb), true);

        String labelTelat = jamTelat > 0
                ? "Denda Keterlambatan (Telat " + jamTelat + " Jam)"
                : "Denda Keterlambatan (Tepat Waktu)";
        addPair(grid, labelTelat, String.format("Rp %,.0f", dendaTelat), jamTelat > 0);

        if (dendaKerusakan > 0) {
            addPair(grid, "Denda Kerusakan Fasilitas", String.format("Rp %,.0f", dendaKerusakan), true);
        }

        addPair(grid, "TOTAL PEMBAYARAN LUNAS", String.format("Rp %,.0f", total), true);

        JPanel btns = new JPanel(new GridLayout(1, 3, 10, 0));
        btns.setOpaque(false);
        JButton btnPrint = makeBtn("Simpan / Cetak HTML", COLOR_NAVY, Color.WHITE, 160, 38);
        JButton btnTxt = makeBtn("Simpan Nota (.txt)", COLOR_NAVY, Color.WHITE, 160, 38);
        JButton btnClose = makeBtn("Selesai & Tutup", COLOR_GOLD, Color.WHITE, 160, 38);
        btnTxt.addActionListener(e -> simpanNotaTeks(dialog, reservasi));
        btnPrint.addActionListener(e -> simpanNotaHTML(dialog, idRes, nama, noKamar, waktu, kamar, fb, jamTelat, dendaTelat, dendaKerusakan, total, items));
        btnClose.addActionListener(e -> dialog.dispose());
        btns.add(btnPrint);
        btns.add(btnTxt);
        btns.add(btnClose);

        root.add(hdr, BorderLayout.NORTH);
        root.add(new JScrollPane(grid), BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        dialog.add(root);
        dialog.setVisible(true);
    }

    private void addPair(JPanel p, String left, String right, boolean bold) {
        JLabel l = new JLabel(left);
        JLabel r = new JLabel(right, SwingConstants.RIGHT);
        Font f = new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 12);
        l.setFont(f);
        r.setFont(f);
        l.setForeground(COLOR_NAVY);
        r.setForeground(bold ? new Color(5, 150, 105) : COLOR_NAVY);
        p.add(l);
        p.add(r);
    }

    private JButton makeBtn(String text, Color bg, Color fg, int w, int h) {
        Color hover = bg.equals(Color.WHITE) ? new Color(241, 245, 249) : bg.darker();
        JButton b = UIHelper.createRoundedButton(text, bg, hover, fg, 12);
        b.setPreferredSize(new Dimension(w, h));
        return b;
    }

    private JPanel makeCard(String title, JLabel val, Color accent) {
        JPanel c = UIHelper.createRoundedCard(14, 12, 16);
        c.setLayout(new GridLayout(2, 1, 2, 4));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(new Color(100, 116, 139));
        val.setFont(new Font("Segoe UI", Font.BOLD, 17));
        val.setForeground(accent);
        c.add(t);
        c.add(val);
        return c;
    }

    private void simpanNotaTeks(Component parent, Reservasi reservasi) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Nota_Checkout_RSV-" + reservasi.getIdReservasi() + ".txt"));
        if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(fc.getSelectedFile()), StandardCharsets.UTF_8))) {
                out.print(reservasi.cetakNota());
                JOptionPane.showMessageDialog(parent, "Nota berhasil disimpan:\n" + fc.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, "Gagal menyimpan: " + ex.getMessage());
            }
        }
    }

    private void simpanNotaHTML(Component parent, int idRes, String nama, String noKamar, String waktu,
                                double kamar, double fb, long jamTelat, double dendaTelat,
                                double dendaKerusakan, double total, List<ResepsionisDAO.RincianFBItem> items) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Nota_Checkout_RSV-" + idRes + ".html"));
        if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter out = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
                String infoTelat = jamTelat > 0 ? "Denda Keterlambatan (Telat " + jamTelat + " Jam)" : "Denda Keterlambatan (Tepat Waktu)";
                out.printf("<html><body style='font-family:Segoe UI,Arial;max-width:480px;margin:auto;padding:24px;border:1px solid #ccc;'>" +
                                "<div style='background:#0f172a;color:#c5a880;padding:14px;text-align:center;'><h3>HOMESTAYSAFE RESORT</h3><small style='color:#fff'>Nota RSV-%d • %s</small></div>" +
                                "<p><b>Tamu:</b> %s | <b>Kamar:</b> %s</p><hr>" +
                                "<p>Biaya Kamar: <b>Rp %,.0f</b><br>Tagihan F&B: <b>Rp %,.0f</b><br>%s: <b>Rp %,.0f</b><br>Denda Kerusakan: <b>Rp %,.0f</b></p>" +
                                "<h3 style='color:#059669;'>TOTAL LUNAS: Rp %,.0f</h3></body></html>",
                        idRes, waktu, nama, noKamar, kamar, fb, infoTelat, dendaTelat, dendaKerusakan, total);
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(fc.getSelectedFile());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, "Gagal menyimpan: " + ex.getMessage());
            }
        }
    }
}