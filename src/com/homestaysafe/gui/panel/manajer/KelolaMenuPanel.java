package com.homestaysafe.gui.panel.manajer;

import com.homestaysafe.dao.MenuDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel Kelola Menu F&B untuk role Manager.
 * CRUD lengkap: Create, Read, Update, Delete.
 */
public class KelolaMenuPanel extends JPanel {

    private final Color COLOR_NAVY   = new Color(15, 23, 42);
    private final Color COLOR_BG     = new Color(245, 247, 250);
    private final Color COLOR_GOLD   = new Color(197, 168, 128);
    private final Color COLOR_BORDER = new Color(226, 232, 240);
    private final Color COLOR_RED    = new Color(220, 38, 38);
    private final Color COLOR_GREEN  = new Color(5, 150, 105);
    private final Color COLOR_MUTED  = new Color(100, 116, 139);

    private final MenuDAO dao = new MenuDAO();
    private final DefaultTableModel model;
    private final JTable table;

    private final JTextField txtNama  = new JTextField();
    private final JTextField txtHarga = new JTextField();
    private final JLabel lblMode = new JLabel("Tambah menu baru");
    private final JButton btnSimpan = new JButton("Simpan");

    private Integer selectedId = null; // null = tambah, ada isi = edit

    public KelolaMenuPanel() {
        setLayout(new BorderLayout(0, 18));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(25, 28, 25, 28));

        // ========== HEADER ==========
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Kelola Menu Makanan & Minuman");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_NAVY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Tambah, ubah, atau hapus item menu F&B Homestay");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(COLOR_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSub.setBorder(new EmptyBorder(4, 0, 0, 0));

        header.add(lblTitle);
        header.add(lblSub);

        // ========== FORM CARD ==========
        JPanel formCard = new JPanel(new BorderLayout(16, 0));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Kiri: mode label + field
        JPanel formLeft = new JPanel();
        formLeft.setLayout(new BoxLayout(formLeft, BoxLayout.Y_AXIS));
        formLeft.setOpaque(false);

        lblMode.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMode.setForeground(COLOR_GOLD);
        lblMode.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMode.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel fieldRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        fieldRow.setOpaque(false);
        fieldRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNama = makeLabel("Nama Menu");
        txtNama.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNama.setPreferredSize(new Dimension(260, 36));
        txtNama.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        JLabel lblHarga = makeLabel("Harga (Rp)");
        txtHarga.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtHarga.setPreferredSize(new Dimension(150, 36));
        txtHarga.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        fieldRow.add(lblNama);
        fieldRow.add(txtNama);
        fieldRow.add(Box.createHorizontalStrut(8));
        fieldRow.add(lblHarga);
        fieldRow.add(txtHarga);

        formLeft.add(lblMode);
        formLeft.add(fieldRow);

        // Kanan: tombol Simpan saja (sejajar dengan field)
        JPanel formRight = new JPanel(new GridBagLayout());
        formRight.setOpaque(false);

        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSimpan.setBackground(COLOR_GOLD);
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setPreferredSize(new Dimension(120, 36));
        btnSimpan.setFocusPainted(false);
        btnSimpan.setOpaque(true);
        btnSimpan.setBorderPainted(false);
        btnSimpan.setCursor(new Cursor(Cursor.HAND_CURSOR));

        formRight.add(btnSimpan);

        formCard.add(formLeft, BorderLayout.CENTER);
        formCard.add(formRight, BorderLayout.EAST);

        // ========== TABEL ==========
        String[] cols = {"ID", "Nama Menu", "Harga", "Aksi"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 3;
            }
        };

        table = new JTable(model);
        table.setRowHeight(44);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(226, 232, 240));
        table.setSelectionForeground(COLOR_NAVY);

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

        DefaultTableCellRenderer baseRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (!sel) lbl.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
                lbl.setHorizontalAlignment(c == 2 ? SwingConstants.RIGHT : SwingConstants.CENTER);
                lbl.setForeground(c == 2 ? COLOR_GREEN : COLOR_NAVY);
                lbl.setFont(new Font("Segoe UI", c == 2 ? Font.BOLD : Font.PLAIN, 13));
                return lbl;
            }
        };
        for (int i = 0; i < 3; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(baseRenderer);
        }

        table.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox()));

        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);

        // ========== SUSUN ==========
        JPanel topSection = new JPanel(new BorderLayout(0, 14));
        topSection.setOpaque(false);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(formCard, BorderLayout.CENTER);

        add(topSection, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // ========== EVENT ==========
        btnSimpan.addActionListener(e -> simpanData());
        // Double-click baris = edit
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) editRow(row);
                }
            }
        });

        loadData();
    }

    // ---------- LOGIC ----------

    private void loadData() {
        model.setRowCount(0);
        List<MenuDAO.Menu> list = dao.getAllMenu();
        for (MenuDAO.Menu m : list) {
            model.addRow(new Object[]{
                    m.getIdMenu(),
                    m.getNamaMenu(),
                    String.format("Rp %,d", (long) m.getHarga()),
                    "Edit | Hapus"
            });
        }
    }

    private void simpanData() {
        String nama = txtNama.getText().trim();
        String hargaStr = txtHarga.getText().trim().replace(".", "").replace(",", "");

        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama menu tidak boleh kosong.", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtNama.requestFocus();
            return;
        }
        double harga;
        try {
            harga = Double.parseDouble(hargaStr);
            if (harga <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Harga harus angka positif (contoh: 35000).", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtHarga.requestFocus();
            return;
        }

        boolean ok;
        if (selectedId == null) {
            ok = dao.tambahMenu(nama, harga);
            if (ok) JOptionPane.showMessageDialog(this, "Menu berhasil ditambahkan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } else {
            ok = dao.updateMenu(selectedId, nama, harga);
            if (ok) JOptionPane.showMessageDialog(this, "Menu berhasil diubah.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        }

        if (ok) {
            resetForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data. Coba lagi.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editRow(int row) {
        selectedId = (Integer) model.getValueAt(row, 0);
        txtNama.setText((String) model.getValueAt(row, 1));

        List<MenuDAO.Menu> list = dao.getAllMenu();
        for (MenuDAO.Menu m : list) {
            if (m.getIdMenu() == selectedId) {
                txtHarga.setText(String.valueOf((long) m.getHarga()));
                break;
            }
        }
        lblMode.setText("Edit menu  ·  ID " + selectedId);
        lblMode.setForeground(new Color(37, 99, 235));
        btnSimpan.setText("Update");
        txtNama.requestFocus();
    }

    private void hapusRow(int row) {
        int id = (Integer) model.getValueAt(row, 0);
        String nama = (String) model.getValueAt(row, 1);

        int terkait = dao.hitungPesananTerkait(id);
        String pesan = terkait > 0
                ? "Menu \"" + nama + "\" sudah pernah dipesan (" + terkait + " pesanan).\n"
                + "Menghapus menu ini juga akan menghapus data pesanan terkait.\n\nYakin hapus?"
                : "Yakin ingin menghapus menu \"" + nama + "\"?\nTindakan ini tidak bisa dibatalkan.";

        int confirm = JOptionPane.showConfirmDialog(
                this, pesan, "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = dao.hapusMenu(id);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Menu berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                if (selectedId != null && selectedId == id) resetForm();
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus menu.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetForm() {
        selectedId = null;
        txtNama.setText("");
        txtHarga.setText("");
        lblMode.setText("Tambah menu baru");
        lblMode.setForeground(COLOR_GOLD);
        btnSimpan.setText("Simpan");
        table.clearSelection();
    }

    // ---------- HELPER ----------

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_NAVY);
        return lbl;
    }

    // ---------- BUTTON RENDERER & EDITOR ----------

    class ButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JButton btnEdit  = new JButton("Edit");
        private final JButton btnHapus = new JButton("Hapus");

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 6));
            setOpaque(true);
            styleBtn(btnEdit, new Color(37, 99, 235));
            styleBtn(btnHapus, COLOR_RED);
            add(btnEdit);
            add(btnHapus);
        }

        private void styleBtn(JButton b, Color bg) {
            b.setFont(new Font("Segoe UI", Font.BOLD, 11));
            b.setBackground(bg);
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setOpaque(true);
            b.setBorderPainted(false);
            b.setPreferredSize(new Dimension(72, 28));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
            setBackground(sel ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        private final JButton btnEdit  = new JButton("Edit");
        private final JButton btnHapus = new JButton("Hapus");
        private int currentRow = -1;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel.setOpaque(true);
            styleBtn(btnEdit, new Color(37, 99, 235));
            styleBtn(btnHapus, COLOR_RED);

            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                if (currentRow >= 0) editRow(currentRow);
            });
            btnHapus.addActionListener(e -> {
                fireEditingStopped();
                if (currentRow >= 0) hapusRow(currentRow);
            });

            panel.add(btnEdit);
            panel.add(btnHapus);
        }

        private void styleBtn(JButton b, Color bg) {
            b.setFont(new Font("Segoe UI", Font.BOLD, 11));
            b.setBackground(bg);
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setOpaque(true);
            b.setBorderPainted(false);
            b.setPreferredSize(new Dimension(72, 28));
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object val, boolean sel, int r, int c) {
            currentRow = r;
            panel.setBackground(t.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Edit | Hapus";
        }
    }
}
