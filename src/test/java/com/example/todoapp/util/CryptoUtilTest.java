package com.example.todoapp.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CryptoUtilTest {

    @Test
    void hashPasswordIsDeterministic() {
        String hash1 = CryptoUtil.hashPassword("hunter2");
        String hash2 = CryptoUtil.hashPassword("hunter2");
        assertEquals(hash1, hash2);
    }

    @Test
    void hashPasswordDiffersForDifferentInput() {
        assertNotEquals(CryptoUtil.hashPassword("hunter2"), CryptoUtil.hashPassword("hunter3"));
    }

    @Test
    void sessionTokenIsGenerated() {
        String token = CryptoUtil.generateSessionToken("alice");
        assertNotNull(token);
        assertEquals(true, token.startsWith("alice-"));
    }
}
