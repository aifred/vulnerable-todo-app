package com.example.todoapp.repository;

import com.example.todoapp.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(String username, String passwordHash) {
        jdbcTemplate.update(
                "INSERT INTO users (username, password_hash) VALUES (?, ?)",
                username, passwordHash);
    }

    /**
     * VULNERABLE (CWE-89, SQL Injection): the username is concatenated directly into the
     * query string instead of using a bind parameter, so a value such as
     * "admin' -- " lets an attacker authenticate as another user without knowing the
     * password hash, and "' OR '1'='1" returns every row in the table.
     */
    public User findByCredentialsUnsafe(String username, String passwordHash) {
        String sql = "SELECT id, username, password_hash FROM users WHERE username = '"
                + username + "' AND password_hash = '" + passwordHash + "'";
        List<User> matches = jdbcTemplate.query(sql, (ResultSet rs, int rowNum) -> new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password_hash")));
        return matches.isEmpty() ? null : matches.get(0);
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?",
                Integer.class, username);
        return count != null && count > 0;
    }
}
