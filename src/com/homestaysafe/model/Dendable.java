package com.homestaysafe.model;

import java.time.LocalDateTime;

public interface Dendable {
    double hitungDenda(LocalDateTime waktuCheckoutAktual);
}