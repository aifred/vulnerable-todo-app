package com.example.todoapp.controller;

import com.example.todoapp.model.Todo;
import com.example.todoapp.repository.TodoRepository;
import com.example.todoapp.util.SessionStore;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private final TodoRepository todoRepository;
    private final SessionStore sessionStore;

    public BackupController(TodoRepository todoRepository, SessionStore sessionStore) {
        this.todoRepository = todoRepository;
        this.sessionStore = sessionStore;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestHeader("X-Auth-Token") String token) throws IOException {
        String username = sessionStore.usernameFor(token);
        List<Todo> todos = todoRepository.findAllByOwner(username);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(todos);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes.toByteArray());
    }

    /**
     * FIXED (CWE-502, Deserialization of Untrusted Data): reject legacy Java object
     * deserialization for uploaded backups because ObjectInputStream on user-controlled
     * content can trigger gadget chains before application validation runs.
     */
    @PostMapping("/restore")
    public ResponseEntity<String> restore(@RequestParam MultipartFile file) {
        throw new UnsupportedOperationException("Restore only supports safe backup formats.");
    }
}
