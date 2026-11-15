package com.homestaysafe.gui.panel;

import com.homestaysafe.dao.KamarDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BookingCardPanel extends JPanel {

    private final String usernameTamu;
    private final JFrame parentFrame;
    private final JPanel contentPanel;
    private final CardLayout cardLayout;

    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    public BookingCardPanel(String usernameTamu, JFrame parentFrame, JPanel contentPanel, CardLayout cardLayout) {
        this.usernameTamu = usernameTamu;
        this.parentFrame = parentFrame;
        this.contentPanel = contentPanel;
        this.cardLayout = cardLayout;

        setLayout(new BorderLayout(15, 15));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel lblTitle = new JLabel("Reservasi & Booking Kamar Online");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_NAVY);

        KamarDAO kamarDAO = new KamarDAO();
        List<KamarDAO.Kamar> listKamar = kamarDAO.getAllKamar();

        JPanel gridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        gridPanel.setBackground(COLOR_BG);

        for (KamarDAO.Kamar k : listKamar) {
            JPanel card = new JPanel(new BorderLayout(10, 10));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(15, 15, 15, 15)
            ));

            JPanel topCard = new JPanel(new BorderLayout());
            topCard.setOpaque(false);

            JLabel lblTipe = new JLabel(k.getTipeKamar());
            lblTipe.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblTipe.setForeground(COLOR_NAVY);

            String statusDb = k.getStatusKamar() != null ? k.getStatusKamar().trim() : "Tersedia";
            boolean isAvailable = statusDb.equalsIgnoreCase("Tersedia");
            boolean isMaintenance = statusDb.equalsIgnoreCase("Pemeliharaan");

            JLabel lblStatus = new JLabel();
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));

            JButton btnAction = new JButton();
            btnAction.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnAction.setFocusPainted(false);
            btnAction.setBorderPainted(false);
            btnAction.setOpaque(true);

            if (isAvailable) {
                lblStatus.setText("● TERSEDIA");
                lblStatus.setForeground(new Color(16, 185, 129));
                btnAction.setText("Pesan Kamar Ini");
                btnAction.setBackground(COLOR_GOLD);
                btnAction.setForeground(Color.WHITE);
                btnAction.setEnabled(true);
                btnAction.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else if (isMaintenance) {
                lblStatus.setText("● PEMELIHARAAN");
                lblStatus.setForeground(new Color(245, 158, 11));
                btnAction.setText("Sedang Pemeliharaan");
                btnAction.setBackground(new Color(203, 213, 225));
                btnAction.setForeground(new Color(100, 116, 139));
                btnAction.setEnabled(false);
            } else {
                lblStatus.setText("● TERISI");
                lblStatus.setForeground(new Color(239, 68, 68));
                btnAction.setText("Kamar Terisi");
                btnAction.setBackground(new Color(203, 213, 225));
                btnAction.setForeground(new Color(100, 116, 139));
                btnAction.setEnabled(false);
            }

            topCard.add(lblTipe, BorderLayout.WEST);
            topCard.add(lblStatus, BorderLayout.EAST);

            JPanel midCard = new JPanel(new GridLayout(2, 1, 4, 4));
            midCard.setOpaque(false);

            JLabel lblNoKamar = new JLabel("Nomor Kamar: " + k.getNoKamar());
            lblNoKamar.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            JLabel lblHarga = new JLabel(String.format("Rp %,.0f / malam", k.getHargaPerMalam()));
            lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblHarga.setForeground(COLOR_GOLD);

            midCard.add(lblNoKamar);
            midCard.add(lblHarga);

            btnAction.addActionListener(e -> showBookingDialog(k));

            card.add(topCard, BorderLayout.NORTH);
            card.add(midCard, BorderLayout.CENTER);
            card.add(btnAction, BorderLayout.SOUTH);

            gridPanel.add(card);
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);

        add(lblTitle, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void showBookingDialog(KamarDAO.Kamar kamar) {
        JDialog dialog = new JDialog(parentFrame, "Konfirmasi Reservasi Kamar " + kamar.getNoKamar(), true);
        dialog.setSize(420, 340);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel panel = new JPanel(new GridLayout(7, 1, 8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblHeader = new JLabel("RESERVASI KAMAR " + kamar.getNoKamar(), SwingConstants.CENTER);
        lblHeader.setFont(new Font("Serif", Font.BOLD, 18));
        lblHeader.setForeground(COLOR_NAVY);

        SpinnerDateModel modelIn = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinCheckIn = new JSpinner(modelIn);
        spinCheckIn.setEditor(new JSpinner.DateEditor(spinCheckIn, "yyyy-MM-dd"));

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        SpinnerDateModel modelOut = new SpinnerDateModel(cal.getTime(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinCheckOut = new JSpinner(modelOut);
        spinCheckOut.setEditor(new JSpinner.DateEditor(spinCheckOut, "yyyy-MM-dd"));

        JPanel pIn = new JPanel(new BorderLayout());
        pIn.setOpaque(false);
        pIn.add(new JLabel("Tgl Check-in: "), BorderLayout.WEST);
        pIn.add(spinCheckIn, BorderLayout.CENTER);

        JPanel pOut = new JPanel(new BorderLayout());
        pOut.setOpaque(false);
        pOut.add(new JLabel("Tgl Check-out: "), BorderLayout.WEST);
        pOut.add(spinCheckOut, BorderLayout.CENTER);

        JLabel lblHarga = new JLabel("Harga per Malam: Rp " + String.format("%,.0f", kamar.getHargaPerMalam()));
        lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnConfirm = new JButton("Konfirmasi & Simpan Reservasi");
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConfirm.setBackground(COLOR_GOLD);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setBorderPainted(false);
        btnConfirm.setOpaque(true);
        btnConfirm.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnConfirm.addActionListener(e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dIn = (Date) spinCheckIn.getValue();
            Date dOut = (Date) spinCheckOut.getValue();
            String checkIn = sdf.format(dIn);
            String checkOut = sdf.format(dOut);

            if (checkIn.compareTo(checkOut) >= 0) {
                JOptionPane.showMessageDialog(dialog,
                        "Jadwal Tidak Valid!\nCheck-out harus setelah Check-in.",
                        "Validasi Tanggal", JOptionPane.WARNING_MESSAGE);
                return;
            }

            long diffMillis = dOut.getTime() - dIn.getTime();
            long days = java.util.concurrent.TimeUnit.DAYS.convert(diffMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
            if (days < 1) days = 1;
            double totalBayar = kamar.getHargaPerMalam() * days;

            KamarDAO dao = new KamarDAO();
            String hasil = dao.simpanReservasi(usernameTamu, kamar.getIdKamar(), checkIn, checkOut, totalBayar);

            if (hasil != null && hasil.startsWith("OK|")) {
                String idStr = hasil.split("\\|")[1];
                String kodeBooking = "HS-" + String.format("%05d", Integer.parseInt(idStr));

                JOptionPane.showMessageDialog(dialog,
                        "Booking Berhasil!\n\nKode Booking: " + kodeBooking +
                                "\nTotal: Rp " + String.format("%,.0f", totalBayar) +
                                "\n\nSilakan cek di Riwayat Pemesanan.\nMenunggu verifikasi Resepsionis.",
                        "Sukses Booking", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                contentPanel.add(new BookingCardPanel(usernameTamu, parentFrame, contentPanel, cardLayout), "Booking");
                cardLayout.show(contentPanel, "Booking");
            } else if ("BENTROK".equals(hasil)) {
                JOptionPane.showMessageDialog(dialog,
                        "Jadwal Bentrok!\n\nKamar ini sudah dipesan pada rentang tanggal tersebut.\n" +
                                "Silakan ubah tanggal atau pilih kamar lain.",
                        "Jadwal Bentrok", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Gagal menyimpan ke database:\n" + hasil,
                        "Error Database", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(lblHeader);
        panel.add(pIn);
        panel.add(pOut);
        panel.add(lblHarga);
        panel.add(Box.createVerticalStrut(5));
        panel.add(btnConfirm);

        dialog.add(panel);
        dialog.setVisible(true);
    }
}