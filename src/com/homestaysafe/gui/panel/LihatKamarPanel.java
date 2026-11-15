package com.homestaysafe.gui.panel;

import com.homestaysafe.dao.KamarDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class LihatKamarPanel extends JPanel {

    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    public LihatKamarPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel lblTitle = new JLabel("Informasi Tipe Kamar & Fasilitas Homestay");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_NAVY);
        add(lblTitle, BorderLayout.NORTH);

        KamarDAO kamarDAO = new KamarDAO();
        List<KamarDAO.Kamar> listKamar = kamarDAO.getAllKamar();

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(COLOR_BG);

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

            JLabel lblTipe = new JLabel(k.getTipeKamar() + " (No. " + k.getNoKamar() + ")");
            lblTipe.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblTipe.setForeground(COLOR_NAVY);

            String statusDb = k.getStatusKamar() != null ? k.getStatusKamar().trim() : "Tersedia";
            boolean isAvailable = statusDb.equalsIgnoreCase("Tersedia");
            boolean isMaintenance = statusDb.equalsIgnoreCase("Pemeliharaan");

            JLabel lblStatus = new JLabel();
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));

            if (isAvailable) {
                lblStatus.setText("● TERSEDIA");
                lblStatus.setForeground(new Color(16, 185, 129));
            } else if (isMaintenance) {
                lblStatus.setText("● PEMELIHARAAN");
                lblStatus.setForeground(new Color(245, 158, 11));
            } else {
                lblStatus.setText("● TERISI");
                lblStatus.setForeground(new Color(239, 68, 68));
            }

            topCard.add(lblTipe, BorderLayout.WEST);
            topCard.add(lblStatus, BorderLayout.EAST);

            String fasilitas = k.getTipeKamar().equalsIgnoreCase("Suite") ? "AC, King Bed, Smart TV, Bathup, Breakfast" :
                    k.getTipeKamar().equalsIgnoreCase("Deluxe") ? "AC, Queen Bed, Wi-Fi, Water Heater" : "AC, Single Bed, Wi-Fi";

            JPanel midCard = new JPanel(new GridLayout(3, 1, 4, 4));
            midCard.setOpaque(false);

            JLabel lblFasilitasTitle = new JLabel("Fasilitas Utama:");
            lblFasilitasTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblFasilitasTitle.setForeground(new Color(100, 116, 139));

            JLabel lblFasilitas = new JLabel("<html><body style='width: 180px;'>" + fasilitas + "</body></html>");
            lblFasilitas.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            JLabel lblHarga = new JLabel(String.format("Rp %,.0f / malam", k.getHargaPerMalam()));
            lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblHarga.setForeground(COLOR_GOLD);

            midCard.add(lblFasilitasTitle);
            midCard.add(lblFasilitas);
            midCard.add(lblHarga);

            card.add(topCard, BorderLayout.NORTH);
            card.add(midCard, BorderLayout.CENTER);

            gridPanel.add(card);
        }

        mainContainer.add(gridPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(mainContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        add(scrollPane, BorderLayout.CENTER);
    }
}