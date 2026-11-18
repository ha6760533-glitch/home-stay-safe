package com.homestaysafe.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility hashing password memakai PBKDF2-HMAC-SHA256 + salt acak (hanya library bawaan Java).
 *
 * Format yang disimpan di kolom users.password_hash:
 *     pbkdf2$<iterasi>$<salt base64>$<hash base64>
 *
 * Password asli tidak pernah disimpan; saat login password yang diketik di-hash ulang
 * dengan salt yang sama lalu dibandingkan.
 */
public final class PasswordUtil {

    private static final String ALGORITMA = "PBKDF2WithHmacSHA256";
    private static final int ITERASI = 65_536;
    private static final int PANJANG_SALT = 16;   // byte
    private static final int PANJANG_HASH = 256;  // bit
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    /** Menghasilkan string hash baru (salt acak setiap dipanggil). */
    public static String hash(String password) {
        byte[] salt = new byte[PANJANG_SALT];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERASI);
        return "pbkdf2$" + ITERASI + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /** Memeriksa apakah password cocok dengan string hash yang tersimpan di database. */
    public static boolean verify(String password, String tersimpan) {
        if (password == null || tersimpan == null) return false;
        String[] bagian = tersimpan.split("\\$");
        if (bagian.length != 4 || !bagian[0].equals("pbkdf2")) return false;
        try {
            int iterasi = Integer.parseInt(bagian[1]);
            byte[] salt = Base64.getDecoder().decode(bagian[2]);
            byte[] hashAsli = Base64.getDecoder().decode(bagian[3]);
            byte[] hashInput = pbkdf2(password.toCharArray(), salt, iterasi);
            return MessageDigest.isEqual(hashAsli, hashInput); // perbandingan waktu-konstan
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterasi) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterasi, PANJANG_HASH);
            return SecretKeyFactory.getInstance(ALGORITMA).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Algoritma hashing tidak tersedia", e);
        }
    }
}
