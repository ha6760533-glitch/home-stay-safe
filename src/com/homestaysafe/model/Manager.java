package com.homestaysafe.model;

import com.homestaysafe.gui.ManajerFrame;
import javax.swing.JFrame;

// INHERITANCE: Manager extends User
public class Manager extends User {
    public Manager(int idUser, String nama, String username, String noIdentitas) {
        super(idUser, nama, username, noIdentitas);
    }

    // POLYMORPHISM
    @Override
    public String getRole() { return "Manager"; }

    @Override
    public String getDisplayRole() { return "Manajer Hotel"; }

    @Override
    public JFrame createDashboard() { return new ManajerFrame(this); }
}
