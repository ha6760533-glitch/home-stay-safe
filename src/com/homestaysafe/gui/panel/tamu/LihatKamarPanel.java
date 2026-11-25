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
            int unitReady = jumlahTersedia.getOrDefault(tipe, 0);
            boolean isAvailable = unitReady > 0;

            JPanel card = UIHelper.createRoundedCard(18, 16, 16);
            card.setLayout(new BorderLayout(0, 12));

            // Foto Tipe Kamar di Bagian Atas Card
            JLabel lblFoto = new JLabel(UIHelper.getKamarImageIcon(tipe, 310, 155, 14), SwingConstants.CENTER);
            lblFoto.setPreferredSize(new Dimension(310, 155));

            JPanel topInfo = new JPanel(new BorderLayout());
            topInfo.setOpaque(false);

            JLabel lblTipe = new JLabel(tipe);
            lblTipe.setFont(new Font("Segoe UI", Font.BOLD, 17));
            lblTipe.setForeground(COLOR_NAVY);

            JLabel lblStatus = new JLabel(isAvailable ? "● TERSEDIA (" + unitReady + ")" : "● PENUH");
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblStatus.setForeground(isAvailable ? new Color(16, 185, 129) : new Color(239, 68, 68));

            topInfo.add(lblTipe, BorderLayout.WEST);
            topInfo.add(lblStatus, BorderLayout.EAST);

            JPanel northCard = new JPanel(new BorderLayout(0, 10));
            northCard.setOpaque(false);
            northCard.add(lblFoto, BorderLayout.NORTH);
            northCard.add(topInfo, BorderLayout.SOUTH);

            String fasilitas = k.getFasilitas();

            JPanel midCard = new JPanel();
            midCard.setLayout(new BoxLayout(midCard, BoxLayout.Y_AXIS));
            midCard.setOpaque(false);

            JLabel lblFasilitasTitle = new JLabel("Kapasitas " + k.getKapasitas() + " Orang • Fasilitas Utama:");
            lblFasilitasTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblFasilitasTitle.setForeground(new Color(100, 116, 139));
            lblFasilitasTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblFasilitas = new JLabel("<html><body style='width: 255px;'>" + fasilitas + "</body></html>");
            lblFasilitas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblFasilitas.setForeground(new Color(51, 65, 85));
            lblFasilitas.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblFasilitas.setBorder(new EmptyBorder(4, 0, 8, 0));

            JLabel lblHarga = new JLabel(String.format("Rp %,.0f / malam", k.getHargaPerMalam()));
            lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblHarga.setForeground(COLOR_GOLD);
            lblHarga.setAlignmentX(Component.LEFT_ALIGNMENT);

            midCard.add(lblFasilitasTitle);
            midCard.add(lblFasilitas);
            midCard.add(lblHarga);

            card.add(northCard, BorderLayout.NORTH);
            card.add(midCard, BorderLayout.CENTER);

            gridPanel.add(card);
        }

        mainContainer.add(gridPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(mainContainer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(COLOR_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        add(scrollPane, BorderLayout.CENTER);
    }
}