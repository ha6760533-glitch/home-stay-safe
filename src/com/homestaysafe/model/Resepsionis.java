package com.homestaysafe.model;

import com.homestaysafe.gui.ResepsionisFrame;
import javax.swing.JFrame;

// INHERITANCE: Resepsionis extends User
public class Resepsionis extends User {
    public Resepsionis(int idUser, String nama, String username, String noIdentitas) {
        super(idUser, nama, username, noIdentitas);
    }

    // POLYMORPHISM
    @Override
    public String getRole() { return "Resepsionis"; }

    @Override
    public String getDisplayRole() { return "Staff Resepsionis"; }

    @Override
    public JFrame createDashboard() { return new ResepsionisFrame(this); }
}
