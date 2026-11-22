package com.homestaysafe.gui;

import com.homestaysafe.util.UIHelper;

import com.homestaysafe.gui.panel.manajer.KelolaMenuPanel;
import com.homestaysafe.gui.panel.manajer.LaporanPendapatanPanel;
import com.homestaysafe.gui.panel.manajer.RiwayatTransaksiManajerPanel;
import com.homestaysafe.gui.panel.manajer.StatistikKamarPanel;
import com.homestaysafe.model.User;

public class ManajerFrame extends BaseDashboardFrame {

    public ManajerFrame(User user) {
        super(user);
        initUI();
    }

    @Override protected String getPortalTitle() { return "Portal Eksekutif Manajer"; }
    @Override protected String getBrandText() { return "HomeStaySafe - Portal Eksekutif Manajer"; }
    @Override protected String getUserPrefix() { return "Manajer"; }
    @Override protected String getProfileTooltip() { return "Profil Manajer & Logout"; }
    @Override protected String getMenuSectionTitle() { return "MENU EKSEKUTIF"; }
    @Override protected String getProfileDialogTitle() { return "Profil Akun Manajer"; }
    @Override protected String getProfileHeader() { return "PROFIL EKSEKUTIF MANAJER"; }
    @Override protected String getProfileSubHeader() { return "HomestaySafe Management System"; }

    @Override protected boolean isSidebarClosedAfterClick() { return false; }

    @Override
    protected String[][] getProfileRows() {
        return new String[][]{
                {"Nama Lengkap", orDash(user.getNama())},
                {"No. Identitas", orDash(user.getNoIdentitas())},
                {"Username & Role", user.getUsername() + " (Manajer)"}
        };
    }

    @Override
    protected void buildMenu() {

        addMenuItem("Laporan Pendapatan", "Pendapatan", LaporanPendapatanPanel::new);

        addMenuItem("Kelola Menu F&B", "KelolaMenu", KelolaMenuPanel::new);
        addMenuItem("Statistik Kamar Terfavorit", "Statistik", StatistikKamarPanel::new);
        addMenuItem("Riwayat Seluruh Transaksi", "Riwayat",
                () -> new RiwayatTransaksiManajerPanel(contentPanel, cardLayout));
    }
}
