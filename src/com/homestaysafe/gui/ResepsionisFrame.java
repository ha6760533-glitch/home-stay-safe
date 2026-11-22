package com.homestaysafe.gui;

import com.homestaysafe.util.UIHelper;
import com.homestaysafe.gui.panel.resepsionis.*;
import com.homestaysafe.model.User;

public class ResepsionisFrame extends BaseDashboardFrame {

    public ResepsionisFrame(User user) {
        super(user);
        initUI();
    }

    @Override protected String getPortalTitle() { return "Portal Resepsionis"; }
    @Override protected String getBrandText() { return "HomeStaySafe - Staff Resepsionis"; }
    @Override protected String getUserPrefix() { return "Staff"; }
    @Override protected String getProfileTooltip() { return "Profil Resepsionis & Logout"; }
    @Override protected String getMenuSectionTitle() { return "MENU RESEPSIONIS"; }
    @Override protected String getProfileDialogTitle() { return "Profil Staff Resepsionis"; }
    @Override protected String getProfileHeader() { return "PROFIL STAFF RESEPSIONIS"; }
    @Override protected String getProfileSubHeader() { return "HomeStaySafe Front Office"; }

    @Override
    protected String[][] getProfileRows() {
        return new String[][]{
                {"Username Akun", user.getUsername()},
                {"Hak Akses / Role", "Resepsionis / Front Office"}
        };
    }

    @Override
    protected void buildMenu() {
        addMenuItem("Pemesanan Offline", "Offline", () -> new OfflineBookingPanel(contentPanel, cardLayout));
        addMenuItem("Verifikasi Booking", "Verifikasi", () -> new VerifikasiBookingPanel(contentPanel, cardLayout));
        addMenuItem("Cek Stok Kamar", "CekKamar", CekStokKamarPanel::new);
        addMenuItem("Proses Check-in", "CheckIn", () -> new CheckInPanel(contentPanel, cardLayout));
        addMenuItem("Proses Check-out", "CheckOut", () -> new CheckOutPanel(contentPanel, cardLayout));
    }
}
