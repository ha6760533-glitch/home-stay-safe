package com.homestaysafe.gui.panel.tamu;

import com.homestaysafe.util.UIHelper;
import com.homestaysafe.dao.KamarDAO;
import com.homestaysafe.exception.JadwalBentrokException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
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
        List<KamarDAO.TipeKamarSummary> listTipe = kamarDAO.getRingkasanTipeKamar();

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setBackground(COLOR_BG);

        JPanel gridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        gridPanel.setBackground(COLOR_BG);

        for (KamarDAO.TipeKamarSummary tipe : listTipe) {
            JPanel card = UIHelper.createRoundedCard(18, 20, 20);
            card.setLayout(new BorderLayout(10, 14));

            JPanel topCard = new JPanel(new BorderLayout());
            topCard.setOpaque(false);

            JLabel lblTipe = new JLabel(tipe.getTipeKamar());
            lblTipe.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblTipe.setForeground(COLOR_NAVY);

            boolean isAvailable = tipe.getUnitTersedia() > 0;
            JLabel lblStatus = new JLabel(isAvailable ? "● TERSEDIA" : "● PENUH");
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblStatus.setForeground(isAvailable ? new Color(16, 185, 129) : new Color(239, 68, 68));

            topCard.add(lblTipe, BorderLayout.WEST);
            topCard.add(lblStatus, BorderLayout.EAST);

            JPanel midCard = new JPanel(new GridLayout(1, 1));
            midCard.setOpaque(false);

            JLabel lblHarga = new JLabel(String.format("Rp %,.0f / malam", tipe.getHargaPerMalam()));
            lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblHarga.setForeground(COLOR_GOLD);

            midCard.add(lblHarga);

            JButton btnAction = isAvailable
                    ? UIHelper.goldButton("Pesan Kamar Ini")
                    : UIHelper.softButton("Kamar Penuh");
            btnAction.setPreferredSize(new Dimension(0, 40));
            btnAction.setEnabled(isAvailable);

            if (isAvailable) {
                btnAction.addActionListener(e -> showBookingDialog(tipe));
            }

            card.add(topCard, BorderLayout.NORTH);
            card.add(midCard, BorderLayout.CENTER);
            card.add(btnAction, BorderLayout.SOUTH);

            gridPanel.add(card);
        }

        topWrapper.add(gridPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(topWrapper);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(COLOR_BG);

        add(lblTitle, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void showBookingDialog(KamarDAO.TipeKamarSummary tipe) {
        JDialog dialog = new JDialog(parentFrame, "Konfirmasi Reservasi Tipe " + tipe.getTipeKamar(), true);
        dialog.setSize(440, 355);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(22, 26, 22, 26));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        JLabel lblHeader = new JLabel("RESERVASI KAMAR " + tipe.getTipeKamar().toUpperCase(), SwingConstants.CENTER);
        lblHeader.setFont(new Font("Serif", Font.BOLD, 18));
        lblHeader.setForeground(COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 4, 14, 4);
        panel.add(lblHeader, gbc);

        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.gridwidth = 1;

        // Label rata kiri dengan lebar seragam
        JLabel lblIn = new JLabel("Tgl Check-in:");
        lblIn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblIn.setForeground(COLOR_NAVY);
        lblIn.setPreferredSize(new Dimension(115, 36));
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panel.add(lblIn, gbc);

        SpinnerDateModel modelIn = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinCheckIn = new JSpinner(modelIn);
        spinCheckIn.setEditor(new JSpinner.DateEditor(spinCheckIn, "yyyy-MM-dd"));
        spinCheckIn.setPreferredSize(new Dimension(220, 38));
        UIHelper.styleField(spinCheckIn);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        panel.add(spinCheckIn, gbc);

        JLabel lblOut = new JLabel("Tgl Check-out:");
        lblOut.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblOut.setForeground(COLOR_NAVY);
        lblOut.setPreferredSize(new Dimension(115, 36));
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panel.add(lblOut, gbc);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        SpinnerDateModel modelOut = new SpinnerDateModel(cal.getTime(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinCheckOut = new JSpinner(modelOut);
        spinCheckOut.setEditor(new JSpinner.DateEditor(spinCheckOut, "yyyy-MM-dd"));
        spinCheckOut.setPreferredSize(new Dimension(220, 38));
        UIHelper.styleField(spinCheckOut);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        panel.add(spinCheckOut, gbc);

        JLabel lblHarga = new JLabel("Harga per Malam: Rp " + String.format("%,.0f", tipe.getHargaPerMalam()));
        lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHarga.setForeground(COLOR_GOLD);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 4, 8, 4);
        panel.add(lblHarga, gbc);

        JButton btnConfirm = UIHelper.goldButton("Konfirmasi & Simpan Reservasi");
        btnConfirm.setPreferredSize(new Dimension(0, 44));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(8, 4, 4, 4);
        panel.add(btnConfirm, gbc);

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
            double totalBayar = tipe.getHargaPerMalam() * days;

            KamarDAO dao = new KamarDAO();
            try {
                int idReservasi = dao.simpanReservasiByTipe(usernameTamu, tipe.getTipeKamar(), checkIn, checkOut, totalBayar);
                String kodeBooking = "RSV-" + idReservasi;

                JOptionPane.showMessageDialog(dialog,
                        "Booking Berhasil!\n\nKode Booking: " + kodeBooking +
                                "\nTipe Kamar: " + tipe.getTipeKamar() +
                                "\nTotal: Rp " + String.format("%,.0f", totalBayar) +
                                "\n\nSilakan cek di Riwayat Pemesanan.\nMenunggu verifikasi Resepsionis.",
                        "Sukses Booking", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                contentPanel.add(new BookingCardPanel(usernameTamu, parentFrame, contentPanel, cardLayout), "Booking");
                cardLayout.show(contentPanel, "Booking");
            } catch (JadwalBentrokException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Jadwal Bentrok!\n\n" + ex.getMessage() + "\n" +
                                "Silakan ubah tanggal atau pilih tipe kamar lain.",
                        "Jadwal Bentrok", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog,
                        "Gagal menyimpan ke database:\n" + ex.getMessage(),
                        "Error Database", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }
}