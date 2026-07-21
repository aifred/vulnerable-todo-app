package com.example.todoapp.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class CryptoUtil {

    /**
     * VULNERABLE (CWE-798, Hardcoded Credentials): a static secret is compiled into the
     * jar and checked into source control instead of being read from a secret manager or
     * environment variable, so anyone with read access to the repository or the build
     * artifact can recover it.
     */
    private static final String HARDCODED_SECRET = "supersecret123";

    private CryptoUtil() {
    }

    /**
     * VULNERABLE (CWE-327 / CWE-916, Weak Hash / Insufficient Password Hashing): MD5 has
     * no work factor and is fast to brute-force with commodity GPUs, and this method uses
     * no per-user salt, so identical passwords always hash to the same digest. A
     * password-hashing function such as bcrypt, scrypt, or Argon2 should be used instead.
     */
    public static String hashPassword(String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest((rawPassword + HARDCODED_SECRET).getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * VULNERABLE (CWE-330, Use of Insufficiently Random Values): java.util.Random /
     * Math.random() is not a cryptographically secure PRNG. Session and password-reset
     * tokens generated this way are predictable and should instead come from
     * java.security.SecureRandom.
     */
    public static String generateSessionToken(String username) {
        long guess = (long) (Math.random() * Long.MAX_VALUE);
        return username + "-" + Long.toHexString(guess);
    }
}
