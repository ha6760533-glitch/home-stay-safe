package com.homestaysafe.gui;

import com.homestaysafe.util.UIHelper;
import com.homestaysafe.gui.panel.tamu.BookingCardPanel;
import com.homestaysafe.gui.panel.tamu.FBCardPanel;
import com.homestaysafe.gui.panel.tamu.LihatKamarPanel;
import com.homestaysafe.gui.panel.tamu.RiwayatPanel;
import com.homestaysafe.model.User;

public class TamuFrame extends BaseDashboardFrame {

    public TamuFrame(User user) {
        super(user);
        initUI();
    }

    @Override protected String getPortalTitle() { return "Portal Tamu"; }
    @Override protected String getBrandText() { return "HomeStaySafe Resort"; }
    @Override protected String getUserPrefix() { return "Tamu"; }
    @Override protected String getProfileTooltip() { return "Profil Tamu & Logout"; }
    @Override protected String getMenuSectionTitle() { return "MENU LAYANAN TAMU"; }
    @Override protected String getProfileDialogTitle() { return "Profil Akun Tamu"; }
    @Override protected String getProfileHeader() { return "PROFIL AKUN TAMU"; }
    @Override protected String getProfileSubHeader() { return "HomeStaySafe Resort"; }

    @Override
    protected String[][] getProfileRows() {
        return new String[][]{
                {"Nama Lengkap", orDash(user.getNama())},
                {"No. Identitas", orDash(user.getNoIdentitas())},
                {"Username & Role", user.getUsername() + " (Tamu)"}
        };
    }

    @Override
    protected void buildMenu() {
        final String username = user.getUsername();
        addMenuItem("Lihat Tipe Kamar", "TipeKamar", LihatKamarPanel::new);
        addMenuItem("Booking Online", "Booking",
                () -> new BookingCardPanel(username, this, contentPanel, cardLayout));
        addMenuItem("Layanan Makanan", "FB", () -> new FBCardPanel(username));
        addMenuItem("Riwayat Pemesanan", "Riwayat", () -> new RiwayatPanel(username));
    }
}