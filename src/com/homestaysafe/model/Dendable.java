package com.homestaysafe.model;

import java.time.LocalDateTime;

// INTERFACE: kontrak method hitungDenda (Polymorphism via interface)
public interface Dendable {
    double hitungDenda(LocalDateTime waktuCheckoutAktual);
}
