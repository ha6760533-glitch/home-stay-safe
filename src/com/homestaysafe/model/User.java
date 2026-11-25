package com.homestaysafe.model;

import javax.swing.JFrame;

// ABSTRACTION: class abstract tidak bisa diinstansiasi langsung
// INHERITANCE: menjadi parent bagi Tamu, Resepsionis, Manager
public abstract class User {
    // ENCAPSULATION: atribut private, diakses lewat getter/setter
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

    public int getIdUser() { return idUser; }
    public String getNama() { return nama; }
    public String getUsername() { return username; }
    public String getNoIdentitas() { return noIdentitas; }
    public void setNama(String nama) { this.nama = nama; }
    public void setNoIdentitas(String noIdentitas) { this.noIdentitas = noIdentitas; }

    // ABSTRACTION + POLYMORPHISM: wajib di-override oleh subclass
    public abstract String getRole();
    public abstract String getDisplayRole();
    public abstract JFrame createDashboard();
}
