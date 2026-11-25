package com.homestaysafe.gui.panel.resepsionis;

import com.homestaysafe.util.UIHelper;
import com.homestaysafe.dao.KamarDAO;
import com.homestaysafe.model.Kamar;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class CekStokKamarPanel extends JPanel {
    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
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

        cbTipe = new JComboBox<>(new String[]{
                "Semua Tipe", "Standard", "Deluxe", "Executive", "Junior Suite", "Presidential Suite"
        });
        cbTipe.setPreferredSize(new Dimension(170, 36));
        UIHelper.styleField(cbTipe);

        cbStatus = new JComboBox<>(new String[]{"Semua Status", "Tersedia", "Terisi", "Pemeliharaan"});
        cbStatus.setPreferredSize(new Dimension(155, 36));
        UIHelper.styleField(cbStatus);

        JLabel lblTipeFilter = new JLabel("Tipe:");
        lblTipeFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTipeFilter.setForeground(COLOR_NAVY);

        JLabel lblStatusFilter = new JLabel("Status:");
        lblStatusFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatusFilter.setForeground(COLOR_NAVY);

        filterPanel.add(lblTipeFilter);
        filterPanel.add(cbTipe);
        filterPanel.add(lblStatusFilter);
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
        scrollPane.getViewport().setBackground(COLOR_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

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

            JPanel card = UIHelper.createRoundedCard(18, 16, 18);
            card.setLayout(new BorderLayout(10, 10));

            JPanel topCard = new JPanel(new BorderLayout());
            topCard.setOpaque(false);

            JLabel lblTipe = new JLabel(k.getTipeKamar() + " (No. " + k.getNoKamar() + ")");
            lblTipe.setFont(new Font("Segoe UI", Font.BOLD, 14));
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

            JLabel lblThumb = new JLabel(UIHelper.getKamarImageIcon(k.getTipeKamar(), 84, 72, 10));
            lblThumb.setPreferredSize(new Dimension(84, 72));

            JPanel midInfo = new JPanel();
            midInfo.setLayout(new BoxLayout(midInfo, BoxLayout.Y_AXIS));
            midInfo.setOpaque(false);

            JLabel lblFasilitas = new JLabel("<html><body style='width: 170px;'>" + k.getFasilitas() + "</body></html>");
            lblFasilitas.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblFasilitas.setForeground(new Color(100, 116, 139));
            lblFasilitas.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblHarga = new JLabel(String.format("Rp %,.0f / malam", k.getHargaPerMalam()));
            lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblHarga.setForeground(COLOR_GOLD);
            lblHarga.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblHarga.setBorder(new EmptyBorder(6, 0, 0, 0));

            midInfo.add(lblFasilitas);
            midInfo.add(lblHarga);

            JPanel midCard = new JPanel(new BorderLayout(10, 0));
            midCard.setOpaque(false);
            midCard.add(lblThumb, BorderLayout.WEST);
            midCard.add(midInfo, BorderLayout.CENTER);

            JPanel bottomCard = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            bottomCard.setOpaque(false);

            if (isMaintenance) {
                JButton btnSelesaiBersih = UIHelper.createRoundedButton(
                        "✓ Selesai Dibersihkan → Tersedia",
                        UIHelper.COLOR_SUCCESS, UIHelper.COLOR_SUCCESS_HOVER, UIHelper.COLOR_SUCCESS.darker(),
                        Color.WHITE, 10, 11
                );
                btnSelesaiBersih.setBorder(new EmptyBorder(6, 12, 6, 12));
                btnSelesaiBersih.setPreferredSize(new Dimension(225, 34));

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