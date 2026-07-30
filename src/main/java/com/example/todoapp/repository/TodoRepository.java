package com.example.todoapp.repository;

import com.example.todoapp.model.Todo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class TodoRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Todo> ROW_MAPPER = (rs, rowNum) -> {
        Todo todo = new Todo();
        todo.setId(rs.getLong("id"));
        todo.setTitle(rs.getString("title"));
        todo.setDescription(rs.getString("description"));
        todo.setDone(rs.getBoolean("done"));
        todo.setOwner(rs.getString("owner"));
        todo.setAttachmentPath(rs.getString("attachment_path"));
        return todo;
    };

    public TodoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Todo> findAllByOwner(String owner) {
        return jdbcTemplate.query(
                "SELECT * FROM todos WHERE owner = ?", ROW_MAPPER, owner);
    }

    public Todo findById(Long id) {
        List<Todo> results = jdbcTemplate.query(
                "SELECT * FROM todos WHERE id = ?", ROW_MAPPER, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public Todo save(Todo todo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator psc = connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO todos (title, description, done, owner, attachment_path) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, todo.getTitle());
            ps.setString(2, todo.getDescription());
            ps.setBoolean(3, todo.isDone());
            ps.setString(4, todo.getOwner());
            ps.setString(5, todo.getAttachmentPath());
            return ps;
        };

        jdbcTemplate.update(psc, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to retrieve generated id for todo");
        }
        todo.setId(key.longValue());
        return todo;
    }

    public void update(Todo todo) {
        jdbcTemplate.update(
                "UPDATE todos SET title = ?, description = ?, done = ?, attachment_path = ? WHERE id = ?",
                todo.getTitle(), todo.getDescription(), todo.isDone(), todo.getAttachmentPath(), todo.getId());
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM todos WHERE id = ?", id);
    }

    /**
     * VULNERABLE (CWE-89, SQL Injection): the keyword parameter is concatenated directly
     * into the query instead of using a bind parameter or an escaped LIKE pattern, so a
     * value such as "%' UNION SELECT id, username, password_hash, 1, 'x' FROM users -- "
     * lets an attacker read arbitrary tables through this "search my todos" feature.
     */
    public List<Todo> searchUnsafe(String owner, String keyword) {
        String sql = "SELECT * FROM todos WHERE owner = ? AND (title LIKE ? OR description LIKE ?)";
        String likeKeyword = "%" + keyword + "%";
        return jdbcTemplate.query(sql, ROW_MAPPER, owner, likeKeyword, likeKeyword);
    }
}
