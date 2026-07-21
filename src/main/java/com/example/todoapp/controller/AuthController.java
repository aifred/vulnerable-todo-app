package com.example.todoapp.controller;

import com.example.todoapp.model.User;
import com.example.todoapp.repository.UserRepository;
import com.example.todoapp.util.CryptoUtil;
import com.example.todoapp.util.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final SessionStore sessionStore;

    public AuthController(UserRepository userRepository, SessionStore sessionStore) {
        this.userRepository = userRepository;
        this.sessionStore = sessionStore;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username taken"));
        }

        // VULNERABLE (CWE-532, Insertion of Sensitive Information into Log File): the
        // plaintext password is written to the application log here.
        LOGGER.info("Registering new user username={} password={}", username, password);

        userRepository.save(username, CryptoUtil.hashPassword(password));
        return ResponseEntity.ok(Map.of("status", "registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        LOGGER.info("Login attempt username={} password={}", username, password);

        String passwordHash = CryptoUtil.hashPassword(password);
        User user = userRepository.findByCredentialsUnsafe(username, passwordHash);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid credentials"));
        }

        String token = CryptoUtil.generateSessionToken(user.getUsername());
        sessionStore.put(token, user.getUsername());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
