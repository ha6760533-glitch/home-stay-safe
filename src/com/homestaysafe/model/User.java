package com.homestaysafe.model;

import javax.swing.JFrame;

/**
 * Abstract class → Abstraction + base Inheritance.
 * Setiap subclass (Tamu, Resepsionis, Manager) menentukan sendiri role,
 * label tampilan, dan dashboard yang dibuka → Polymorphism.
 */
public abstract class User {
    private final int idUser;
    private String nama;
    private final String username;
    private String noIdentitas;

    public User(int idUser, String nama, String username, String noIdentitas) {
        this.idUser = idUser;
        this.nama = nama;
        this.username = username;
        this.noIdentitas = noIdentitas;
    }

    // Encapsulation
    public int getIdUser() { return idUser; }
    public String getNama() { return nama; }
    public String getUsername() { return username; }
    public String getNoIdentitas() { return noIdentitas; }

    public void setNama(String nama) { this.nama = nama; }
    public void setNoIdentitas(String noIdentitas) { this.noIdentitas = noIdentitas; }

    // Abstraction + Polymorphism (di-override subclass)
    public abstract String getRole();
    public abstract String getDisplayRole();

    /**
     * Polymorphism: tiap role membuat dashboard-nya sendiri.
     * Pemanggil (LoginFrame) tidak perlu if/else berdasarkan role.
     */
    public abstract JFrame createDashboard();
}
