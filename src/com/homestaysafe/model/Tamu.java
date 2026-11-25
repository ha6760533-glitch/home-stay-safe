package com.homestaysafe.model;

import com.homestaysafe.gui.TamuFrame;
import javax.swing.JFrame;

// INHERITANCE: Tamu extends User
public class Tamu extends User {
    public Tamu(int idUser, String nama, String username, String noIdentitas) {
        super(idUser, nama, username, noIdentitas);
    }

    // POLYMORPHISM: implementasi method abstract dari User
    @Override
    public String getRole() { return "Tamu"; }

    @Override
    public String getDisplayRole() { return "Tamu / Guest"; }

    @Override
    public JFrame createDashboard() { return new TamuFrame(this); }
}
