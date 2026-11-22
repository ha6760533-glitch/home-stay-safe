package com.homestaysafe.gui.panel.manajer;

import com.homestaysafe.util.UIHelper;
import com.homestaysafe.dao.ManajerDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RiwayatTransaksiManajerPanel extends JPanel {

    private final Color COLOR_NAVY = new Color(15, 23, 42);
    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_GOLD = new Color(197, 168, 128);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    private final ManajerDAO dao = new ManajerDAO();
    private final DefaultTableModel model;

    public RiwayatTransaksiManajerPanel(JPanel contentPanel, CardLayout cardLayout) {
        setLayout(new BorderLayout(15, 16));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel topHeader = new JPanel(new BorderLayout(0, 10));
        topHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Riwayat Seluruh Transaksi Hotel");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_NAVY);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        Calendar calStart = Calendar.getInstance();
        calStart.add(Calendar.DAY_OF_MONTH, -30);
        JSpinner spinDari = createStyledDateSpinner(calStart.getTime());

        Calendar calEnd = Calendar.getInstance();
        calEnd.add(Calendar.DAY_OF_MONTH, 30);
        JSpinner spinSampai = createStyledDateSpinner(calEnd.getTime());

        JTextField txtKeyword = new JTextField(16);
        txtKeyword.setPreferredSize(new Dimension(190, 36));
        txtKeyword.setToolTipText("Cari nama tamu, NIK, nomor kamar, tipe kamar (Standard/Deluxe/Suite), status, atau ID");
        UIHelper.styleField(txtKeyword);

        JButton btnFilter = UIHelper.createRoundedButton("Cari / Tampilkan", COLOR_GOLD, UIHelper.COLOR_GOLD_HOVER, Color.WHITE, 12);
        btnFilter.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnFilter.setPreferredSize(new Dimension(150, 36));

        JLabel l1 = new JLabel("Dari:");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l1.setForeground(COLOR_NAVY);

        JLabel l2 = new JLabel("Sampai:");
        l2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l2.setForeground(COLOR_NAVY);

        JLabel l3 = new JLabel("Cari:");
        l3.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l3.setForeground(COLOR_NAVY);

        filterPanel.add(l1);
        filterPanel.add(spinDari);
        filterPanel.add(l2);
        filterPanel.add(spinSampai);
        filterPanel.add(l3);
        filterPanel.add(txtKeyword);
        filterPanel.add(btnFilter);

        topHeader.add(lblTitle, BorderLayout.NORTH);
        topHeader.add(filterPanel, BorderLayout.SOUTH);

        String[] cols = {"ID", "Nama Tamu", "No. Identitas", "Kamar", "Tipe", "Check-In", "Check-Out", "Denda", "Total Biaya", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
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
                if (!sel) {
                    lbl.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                lbl.setBorder(new EmptyBorder(0, 10, 0, 12));

                if (c == 7 || c == 8) {
                    lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                    lbl.setFont(new Font("Segoe UI", c == 8 ? Font.BOLD : Font.PLAIN, 13));
                    lbl.setForeground(c == 8 ? new Color(5, 150, 105) : COLOR_NAVY);
                } else if (c == 9) {
                    String st = val != null ? val.toString() : "";
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    if (st.equalsIgnoreCase("Selesai") || st.equalsIgnoreCase("Disetujui")) {
                        lbl.setForeground(new Color(5, 150, 105));
                    } else if (st.toLowerCase().contains("check-in")) {
                        lbl.setForeground(new Color(37, 99, 235));
                    } else if (st.equalsIgnoreCase("Pending")) {
                        lbl.setForeground(new Color(217, 119, 6));
                    } else {
                        lbl.setForeground(new Color(220, 38, 38));
                    }
                } else {
                    lbl.setHorizontalAlignment(c == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    lbl.setForeground(COLOR_NAVY);
                }
                return lbl;
            }
        };
        for (int i = 0; i < cols.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        scrollTable.getViewport().setBackground(Color.WHITE);

        add(topHeader, BorderLayout.NORTH);
        add(scrollTable, BorderLayout.CENTER);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Runnable reload = () -> {
            try {
                Date dDari = (Date) spinDari.getValue();
                Date dSampai = (Date) spinSampai.getValue();

                if (dDari.after(dSampai)) {
                    JOptionPane.showMessageDialog(this,
                            "Tanggal 'Dari' tidak boleh lebih besar dari tanggal 'Sampai'!",
                            "Validasi Tanggal",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String tglDari = sdf.format(dDari);
                String tglSampai = sdf.format(dSampai);
                loadData(tglDari, tglSampai, txtKeyword.getText());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Gagal memuat data transaksi:\n" + ex.getMessage(),
                        "Error Filter Tanggal",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        };

        btnFilter.addActionListener(e -> reload.run());
        txtKeyword.addActionListener(e -> reload.run());
        reload.run();
    }

    private JSpinner createStyledDateSpinner(Date initialDate) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(initialDate, null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(125, 36));
        UIHelper.styleField(spinner);
        spinner.setToolTipText("Klik angka tahun/bulan/tanggal untuk mengetik langsung atau gunakan panah atas/bawah");

        JFormattedTextField tf = editor.getTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setHorizontalAlignment(SwingConstants.CENTER);
        tf.setBackground(Color.WHITE);
        tf.setBorder(new EmptyBorder(4, 6, 4, 6));

        return spinner;
    }

    private void loadData(String tglDari, String tglSampai, String keyword) {
        model.setRowCount(0);
        List<ManajerDAO.TransaksiReadOnlyItem> list = dao.getRiwayatTransaksi(tglDari, tglSampai, keyword);

        for (ManajerDAO.TransaksiReadOnlyItem t : list) {
            model.addRow(new Object[]{
                    "RSV-" + t.getIdReservasi(),
                    t.getNamaTamu(),
                    t.getNoIdentitas(),
                    "No. " + t.getNoKamar(),
                    t.getTipeKamar(),
                    t.getTglCheckin(),
                    t.getTglCheckout(),
                    String.format("Rp %,.0f", t.getDenda()),
                    String.format("Rp %,.0f", t.getTotalBiaya()),
                    t.getStatusReservasi()
            });
        }
    }
}