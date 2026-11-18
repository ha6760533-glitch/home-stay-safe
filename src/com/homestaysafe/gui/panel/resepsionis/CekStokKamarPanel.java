package com.homestaysafe.gui.panel.resepsionis;

import com.homestaysafe.dao.KamarDAO;
import com.homestaysafe.model.Kamar;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class CekStokKamarPanel extends JPanel {
    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_BORDER = new Color(226, 232, 240);
    private final JPanel gridPanel;
    private List<Kamar> listKamar;
    private final JComboBox<String> cbTipe;
    private final JComboBox<String> cbStatus;

    public CekStokKamarPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Monitoring & Filter Ketersediaan Kamar");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_NAVY);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        cbTipe = new JComboBox<>(new String[]{"Semua Tipe", "Standard", "Deluxe", "Suite"});
        cbStatus = new JComboBox<>(new String[]{"Semua Status", "Tersedia", "Terisi", "Pemeliharaan"});

        filterPanel.add(new JLabel("Tipe:"));
        filterPanel.add(cbTipe);
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(cbStatus);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(filterPanel, BorderLayout.EAST);

        refreshData();

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(COLOR_BG);

        gridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        gridPanel.setBackground(COLOR_BG);

        Runnable applyFilter = () -> renderCards(cbTipe.getSelectedItem().toString(), cbStatus.getSelectedItem().toString());
        cbTipe.addActionListener(e -> applyFilter.run());
        cbStatus.addActionListener(e -> applyFilter.run());

        renderCards("Semua Tipe", "Semua Status");

        mainContainer.add(gridPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(mainContainer);
        scrollPane.setBorder(null);

        add(topBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void refreshData() {
        KamarDAO kamarDAO = new KamarDAO();
        listKamar = kamarDAO.getAllKamar();
    }

    private void renderCards(String filterTipe, String filterStatus) {
        gridPanel.removeAll();

        for (Kamar k : listKamar) {
            String statusDb = k.getStatusKamar() != null ? k.getStatusKamar().trim() : "Tersedia";
            boolean matchTipe = filterTipe.equals("Semua Tipe") || k.getTipeKamar().equalsIgnoreCase(filterTipe);
            boolean matchStatus = filterStatus.equals("Semua Status") || statusDb.equalsIgnoreCase(filterStatus);

            if (!matchTipe || !matchStatus) continue;

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

            String fasilitas = k.getFasilitas(); // Polymorphism: tiap subclass Kamar punya fasilitas sendiri

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

            JPanel bottomCard = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            bottomCard.setOpaque(false);

            if (isMaintenance) {
                JButton btnSelesaiBersih = new JButton("✓ Selesai Dibersihkan → Tersedia");
                btnSelesaiBersih.setFont(new Font("Segoe UI", Font.BOLD, 11));
                btnSelesaiBersih.setBackground(new Color(16, 185, 129));
                btnSelesaiBersih.setForeground(Color.WHITE);
                btnSelesaiBersih.setFocusPainted(false);
                btnSelesaiBersih.setBorderPainted(false);
                btnSelesaiBersih.setOpaque(true);
                btnSelesaiBersih.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnSelesaiBersih.setPreferredSize(new Dimension(210, 32));

                btnSelesaiBersih.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "Apakah kamar No. " + k.getNoKamar() + " sudah selesai dibersihkan?\nStatus akan diubah menjadi TERSEDIA.",
                            "Konfirmasi Selesai Pemeliharaan",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (confirm == JOptionPane.YES_OPTION) {
                        KamarDAO dao = new KamarDAO();
                        boolean ok = dao.updateStatusKamar(k.getIdKamar(), "Tersedia");
                        if (ok) {
                            JOptionPane.showMessageDialog(this,
                                    "Kamar No. " + k.getNoKamar() + " berhasil diubah menjadi TERSEDIA.",
                                    "Berhasil",
                                    JOptionPane.INFORMATION_MESSAGE);
                            refreshData();
                            renderCards(cbTipe.getSelectedItem().toString(), cbStatus.getSelectedItem().toString());
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Gagal mengubah status kamar.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                bottomCard.add(btnSelesaiBersih);
            }

            card.add(topCard, BorderLayout.NORTH);
            card.add(midCard, BorderLayout.CENTER);
            card.add(bottomCard, BorderLayout.SOUTH);

            gridPanel.add(card);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }
}