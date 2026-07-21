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
     * VULNERABLE (CWE-502, Deserialization of Untrusted Data): ObjectInputStream.readObject
     * is called directly on attacker-controlled bytes. If any gadget class with a
     * dangerous readObject/readResolve is present on the classpath, this can be turned into
     * remote code execution -- restoring a backup should never deserialize raw Java
     * objects from an untrusted upload; use a safe data format (e.g. JSON) instead.
     */
    @PostMapping("/restore")
    @SuppressWarnings("unchecked")
    public ResponseEntity<String> restore(@RequestParam MultipartFile file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(file.getInputStream())) {
            List<Todo> restored = (List<Todo>) in.readObject();
            for (Todo todo : restored) {
                todoRepository.save(todo);
            }
        }
        return ResponseEntity.ok("restored");
    }
}
