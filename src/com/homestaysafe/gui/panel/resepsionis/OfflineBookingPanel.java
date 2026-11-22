package com.homestaysafe.gui.panel.resepsionis;

import com.homestaysafe.util.UIHelper;
import com.homestaysafe.dao.KamarDAO;
import com.homestaysafe.model.Kamar;
import com.homestaysafe.dao.ResepsionisDAO;
import com.homestaysafe.exception.JadwalBentrokException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OfflineBookingPanel extends JPanel {

    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    private final JLabel lblRingkasanKamar = new JLabel("-");
    private final JLabel lblRingkasanHargaMalam = new JLabel("Rp 0");
    private final JLabel lblRingkasanDurasi = new JLabel("1 Malam");
    private final JLabel lblRingkasanTotal = new JLabel("Rp 0");

    public OfflineBookingPanel(JPanel contentPanel, CardLayout cardLayout) {
        setLayout(new BorderLayout(15, 18));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel lblTitle = new JLabel("Pemesanan Kamar Offline (Walk-In)");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_NAVY);

        JPanel mainGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        mainGrid.setOpaque(false);

        JPanel leftCard = new JPanel(new BorderLayout());
        leftCard.setBackground(Color.WHITE);
        leftCard.setBorder(new LineBorder(COLOR_BORDER, 1, true));

        JLabel leftHeader = new JLabel("  1. Data Identitas Tamu & Jadwal Menginap");
        leftHeader.setOpaque(true);
        leftHeader.setBackground(COLOR_NAVY);
        leftHeader.setForeground(Color.WHITE);
        leftHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        leftHeader.setPreferredSize(new Dimension(0, 42));

        JPanel formBody = new JPanel(new GridBagLayout());
        formBody.setBackground(Color.WHITE);
        formBody.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JTextField txtNama = createStyledField();
        JTextField txtNIK = createStyledField();

        KamarDAO kamarDAO = new KamarDAO();
        List<Kamar> listKamar = kamarDAO.getAllKamar();
        JComboBox<String> cbKamar = new JComboBox<>();
        cbKamar.setPreferredSize(new Dimension(0, 38));
        UIHelper.styleField(cbKamar);

        for (Kamar k : listKamar) {
            if (k.getStatusKamar().equalsIgnoreCase("Tersedia")) {
                cbKamar.addItem(k.getIdKamar() + " - No " + k.getNoKamar() + " (" + k.getTipeKamar() + " - Rp " + String.format("%,.0f", k.getHargaPerMalam()) + ")");
            }
        }

        SpinnerDateModel modelIn = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinIn = new JSpinner(modelIn);
        spinIn.setEditor(new JSpinner.DateEditor(spinIn, "yyyy-MM-dd"));
        spinIn.setPreferredSize(new Dimension(0, 38));
        UIHelper.styleField(spinIn);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        SpinnerDateModel modelOut = new SpinnerDateModel(cal.getTime(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinOut = new JSpinner(modelOut);
        spinOut.setEditor(new JSpinner.DateEditor(spinOut, "yyyy-MM-dd"));
        spinOut.setPreferredSize(new Dimension(0, 38));
        UIHelper.styleField(spinOut);

        addFormField(formBody, gbc, 0, "Nama Lengkap Tamu", txtNama);
        addFormField(formBody, gbc, 2, "No. Identitas (NIK / KTP - 16 Digit)", txtNIK);
        addFormField(formBody, gbc, 4, "Pilih Kamar Tersedia", cbKamar);
        addFormField(formBody, gbc, 6, "Tanggal Check-In", spinIn);
        addFormField(formBody, gbc, 8, "Tanggal Check-Out", spinOut);

        leftCard.add(leftHeader, BorderLayout.NORTH);
        leftCard.add(formBody, BorderLayout.CENTER);

        JPanel rightCard = new JPanel(new BorderLayout());
        rightCard.setBackground(Color.WHITE);
        rightCard.setBorder(new LineBorder(COLOR_BORDER, 1, true));

        JLabel rightHeader = new JLabel("  2. Ringkasan Tagihan & Aktivasi Kamar");
        rightHeader.setOpaque(true);
        rightHeader.setBackground(COLOR_NAVY);
        rightHeader.setForeground(COLOR_GOLD);
        rightHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rightHeader.setPreferredSize(new Dimension(0, 42));

        JPanel summaryBody = new JPanel(new BorderLayout(0, 16));
        summaryBody.setBackground(Color.WHITE);
        summaryBody.setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel detailGrid = UIHelper.createRoundedCard(14, 16, 18);
        detailGrid.setBackground(new Color(248, 250, 252));
        detailGrid.setLayout(new GridLayout(5, 2, 10, 12));

        addSummaryRow(detailGrid, "Kamar Dipilih", lblRingkasanKamar, false);
        addSummaryRow(detailGrid, "Tarif per Malam", lblRingkasanHargaMalam, false);
        addSummaryRow(detailGrid, "Durasi Menginap", lblRingkasanDurasi, false);
        addSummaryRow(detailGrid, "Status Kamar Awal", new JLabel("Langsung Check-In (Terisi)", SwingConstants.RIGHT), false);
        addSummaryRow(detailGrid, "TOTAL BIAYA KAMAR", lblRingkasanTotal, true);

        JPanel infoBox = new JPanel(new BorderLayout());
        infoBox.setBackground(new Color(254, 252, 232));
        infoBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(253, 224, 71), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        JLabel lblInfo = new JLabel("<html><body style='width:310px;color:#854d0e;font-size:11px;'>" +
                "<b>Info Akun Tamu Walk-In:</b><br>" +
                "Sistem otomatis membuatkan akun akses Portal Tamu (<i>katapertama + 123</i>) agar tamu dapat memesan layanan makanan (F&B) langsung dari kamar.</body></html>");
        infoBox.add(lblInfo, BorderLayout.CENTER);

        JButton btnSimpan = UIHelper.goldButton("Proses & Simpan Pesanan Walk-In");
        btnSimpan.setPreferredSize(new Dimension(0, 44));

        JPanel centerSummary = new JPanel(new BorderLayout(0, 14));
        centerSummary.setOpaque(false);
        centerSummary.add(detailGrid, BorderLayout.NORTH);
        centerSummary.add(infoBox, BorderLayout.CENTER);

        summaryBody.add(centerSummary, BorderLayout.NORTH);
        summaryBody.add(btnSimpan, BorderLayout.SOUTH);

        rightCard.add(rightHeader, BorderLayout.NORTH);
        rightCard.add(summaryBody, BorderLayout.CENTER);

        mainGrid.add(leftCard);
        mainGrid.add(rightCard);

        Runnable updateRingkasan = () -> {
            if (cbKamar.getSelectedItem() == null) return;
            int idKamar = Integer.parseInt(cbKamar.getSelectedItem().toString().split(" - ")[0].trim());
            Kamar target = listKamar.stream().filter(k -> k.getIdKamar() == idKamar).findFirst().orElse(null);
            if (target == null) return;

            long days = 1;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dIn = sdf.parse(sdf.format((Date) spinIn.getValue()));
                Date dOut = sdf.parse(sdf.format((Date) spinOut.getValue()));
                days = TimeUnit.DAYS.convert(dOut.getTime() - dIn.getTime(), TimeUnit.MILLISECONDS);
                if (days < 1) days = 1;
            } catch (Exception ignored) {}

            double total = target.getHargaPerMalam() * days;
            lblRingkasanKamar.setText("No. " + target.getNoKamar() + " (" + target.getTipeKamar() + ")");
            lblRingkasanHargaMalam.setText(String.format("Rp %,.0f", target.getHargaPerMalam()));
            lblRingkasanDurasi.setText(days + " Malam");
            lblRingkasanTotal.setText(String.format("Rp %,.0f", total));
        };

        cbKamar.addActionListener(e -> updateRingkasan.run());
        spinIn.addChangeListener(e -> updateRingkasan.run());
        spinOut.addChangeListener(e -> updateRingkasan.run());
        updateRingkasan.run();

        btnSimpan.addActionListener(e -> {
            String nama = txtNama.getText().trim();
            String nik = txtNIK.getText().trim();

            if (nama.isEmpty() || nik.isEmpty() || cbKamar.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Lengkapi nama, NIK (16 digit), dan pilihan kamar!", "Validasi Form", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!nik.matches("\\d{16}")) {
                JOptionPane.showMessageDialog(this, "No. Identitas (NIK) wajib tepat 16 digit angka!", "Validasi NIK Gagal", JOptionPane.WARNING_MESSAGE);
                txtNIK.requestFocus();
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String checkIn = sdf.format((Date) spinIn.getValue());
            String checkOut = sdf.format((Date) spinOut.getValue());

            if (checkIn.compareTo(checkOut) >= 0) {
                JOptionPane.showMessageDialog(this, "Tanggal Check-Out harus setelah Check-In!", "Validasi Tanggal", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int idKamar = Integer.parseInt(cbKamar.getSelectedItem().toString().split(" - ")[0].trim());
            Kamar targetKamar = listKamar.stream().filter(k -> k.getIdKamar() == idKamar).findFirst().orElse(null);
            double totalHarga = targetKamar != null ? targetKamar.getHargaPerMalam() : 0;
            try {
                Date dIn = sdf.parse(checkIn);
                Date dOut = sdf.parse(checkOut);
                long days = Math.max(1, TimeUnit.DAYS.convert(dOut.getTime() - dIn.getTime(), TimeUnit.MILLISECONDS));
                totalHarga *= days;
            } catch (Exception ignored) {}

            ResepsionisDAO dao = new ResepsionisDAO();
            String[] kredensial;
            try {
                kredensial = dao.simpanPemesananOffline(nama, nik, idKamar, checkIn, checkOut, totalHarga);
            } catch (JadwalBentrokException ex) {
                JOptionPane.showMessageDialog(this, "Jadwal Bentrok!\n" + ex.getMessage(), "Jadwal Bentrok", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (kredensial != null) {
                String noKamarStr = targetKamar != null ? targetKamar.getNoKamar() : "-";
                showKartuAksesDialog(nama, noKamarStr, kredensial[0], kredensial[1]);
                contentPanel.add(new OfflineBookingPanel(contentPanel, cardLayout), "Offline");
                cardLayout.show(contentPanel, "Offline");
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan pemesanan walk-in ke database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(lblTitle, BorderLayout.NORTH);
        add(mainGrid, BorderLayout.CENTER);
    }

    private void showKartuAksesDialog(String nama, String noKamar, String username, String password) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Kartu Akses Tamu Walk-In", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(430, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel hdr = new JLabel("<html><center><b style='color:#c5a880;font-size:14px;'>CHECK-IN WALK-IN BERHASIL</b><br>" +
                "<span style='color:white;font-size:11px;'>Kamar No. " + noKamar + " • " + nama + "</span></center></html>", SwingConstants.CENTER);
        hdr.setOpaque(true);
        hdr.setBackground(COLOR_NAVY);
        hdr.setBorder(new EmptyBorder(14, 10, 14, 10));

        JPanel credBox = UIHelper.createRoundedCard(14, 16, 18);
        credBox.setBackground(new Color(248, 250, 252));
        credBox.setLayout(new GridLayout(3, 2, 8, 10));

        JLabel lUser = new JLabel("Username Login:");
        lUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel vUser = new JLabel(username, SwingConstants.RIGHT);
        vUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        vUser.setForeground(COLOR_NAVY);

        JLabel lPass = new JLabel("Password Akun:");
        lPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel vPass = new JLabel(password, SwingConstants.RIGHT);
        vPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        vPass.setForeground(new Color(5, 150, 105));

        JLabel lInfo = new JLabel("Akses Fitur:");
        lInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel vInfo = new JLabel("Pesan F&B & Riwayat", SwingConstants.RIGHT);
        vInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        credBox.add(lUser); credBox.add(vUser);
        credBox.add(lPass); credBox.add(vPass);
        credBox.add(lInfo); credBox.add(vInfo);

        JButton btnOk = UIHelper.primaryButton("Selesai & Serahkan Kunci");
        btnOk.setPreferredSize(new Dimension(0, 42));
        btnOk.addActionListener(e -> dialog.dispose());

        root.add(hdr, BorderLayout.NORTH);
        root.add(credBox, BorderLayout.CENTER);
        root.add(btnOk, BorderLayout.SOUTH);

        dialog.add(root);
        dialog.setVisible(true);
    }

    private JTextField createStyledField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(0, 38));
        UIHelper.styleField(tf);
        return tf;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridy = row;
        gbc.insets = new Insets(row == 0 ? 0 : 10, 0, 4, 0);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_NAVY);
        panel.add(lbl, gbc);

        gbc.gridy = row + 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(field, gbc);
    }

    private void addSummaryRow(JPanel grid, String title, JLabel valLabel, boolean highlight) {
        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", highlight ? Font.BOLD : Font.PLAIN, highlight ? 13 : 12));
        l.setForeground(COLOR_NAVY);

        valLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, highlight ? 15 : 12));
        valLabel.setForeground(highlight ? new Color(5, 150, 105) : COLOR_NAVY);

        grid.add(l);
        grid.add(valLabel);
    }
}