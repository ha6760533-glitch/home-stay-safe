package com.homestaysafe.exception;

/**
 * Checked exception yang dilempar saat kamar sudah dipesan pada rentang tanggal yang diminta.
 */
public class JadwalBentrokException extends Exception {
    private final String noKamar;
    private final String checkIn;
    private final String checkOut;

    public JadwalBentrokException(String message) {
        this(message, null, null, null);
    }

    public JadwalBentrokException(String noKamar, String checkIn, String checkOut) {
        this("Kamar " + noKamar + " sudah dipesan pada rentang " + checkIn + " s/d " + checkOut,
                noKamar, checkIn, checkOut);
    }

    private JadwalBentrokException(String message, String noKamar, String checkIn, String checkOut) {
        super(message);
        this.noKamar = noKamar;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public String getNoKamar() { return noKamar; }
    public String getCheckIn() { return checkIn; }
    public String getCheckOut() { return checkOut; }
}
