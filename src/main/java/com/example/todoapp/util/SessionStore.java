package com.example.todoapp.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal in-memory session token store for this demo app. Tokens are produced by
 * {@link CryptoUtil#generateSessionToken(String)}, which is itself intentionally weak
 * (CWE-330) -- see that class for details.
 */
@Component
public class SessionStore {

    private final Map<String, String> tokenToUsername = new ConcurrentHashMap<>();

    public void put(String token, String username) {
        tokenToUsername.put(token, username);
    }

    public String usernameFor(String token) {
        return tokenToUsername.get(token);
    }
}
