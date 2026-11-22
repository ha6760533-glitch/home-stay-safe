package com.homestaysafe.gui.panel.tamu;

import com.homestaysafe.util.UIHelper;
import com.homestaysafe.dao.KamarDAO;
import com.homestaysafe.model.Kamar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LihatKamarPanel extends JPanel {

    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);

    public LihatKamarPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel lblTitle = new JLabel("Informasi Tipe Kamar & Fasilitas Homestay");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_NAVY);
        add(lblTitle, BorderLayout.NORTH);

        KamarDAO kamarDAO = new KamarDAO();
        List<Kamar> listKamar = kamarDAO.getAllKamar();

        Map<String, Kamar> perwakilanTipe = new LinkedHashMap<>();
        Map<String, Integer> jumlahTersedia = new LinkedHashMap<>();

        for (Kamar k : listKamar) {
            String tipe = k.getTipeKamar() != null ? k.getTipeKamar().trim() : "Standard";
            perwakilanTipe.putIfAbsent(tipe, k);

            String statusDb = k.getStatusKamar() != null ? k.getStatusKamar().trim() : "Tersedia";
            int tambah = statusDb.equalsIgnoreCase("Tersedia") ? 1 : 0;
            jumlahTersedia.put(tipe, jumlahTersedia.getOrDefault(tipe, 0) + tambah);
        }

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(COLOR_BG);

        JPanel gridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        gridPanel.setBackground(COLOR_BG);

        for (Map.Entry<String, Kamar> entry : perwakilanTipe.entrySet()) {
            String tipe = entry.getKey();
            Kamar k = entry.getValue();
            boolean isAvailable = jumlahTersedia.getOrDefault(tipe, 0) > 0;

            JPanel card = UIHelper.createRoundedCard(18, 18, 18);
            card.setLayout(new BorderLayout(10, 10));

            JPanel topCard = new JPanel(new BorderLayout());
            topCard.setOpaque(false);

            JLabel lblTipe = new JLabel(tipe);
            lblTipe.setFont(new Font("Segoe UI", Font.BOLD, 17));
            lblTipe.setForeground(COLOR_NAVY);

            JLabel lblStatus = new JLabel(isAvailable ? "● TERSEDIA" : "● PENUH");
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblStatus.setForeground(isAvailable ? new Color(16, 185, 129) : new Color(239, 68, 68));

            topCard.add(lblTipe, BorderLayout.WEST);
            topCard.add(lblStatus, BorderLayout.EAST);

            String fasilitas = k.getFasilitas();

            JPanel midCard = new JPanel(new GridLayout(3, 1, 4, 6));
            midCard.setOpaque(false);

            JLabel lblFasilitasTitle = new JLabel("Fasilitas Utama:");
            lblFasilitasTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblFasilitasTitle.setForeground(new Color(100, 116, 139));

            JLabel lblFasilitas = new JLabel("<html><body style='width: 180px;'>" + fasilitas + "</body></html>");
            lblFasilitas.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            JLabel lblHarga = new JLabel(String.format("Rp %,.0f / malam", k.getHargaPerMalam()));
            lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 15));
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
        scrollPane.getViewport().setBackground(COLOR_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        add(scrollPane, BorderLayout.CENTER);
    }
}