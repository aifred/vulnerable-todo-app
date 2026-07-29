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
import java.util.Set;

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

    @PostMapping("/restore")
    @SuppressWarnings("unchecked")
    public ResponseEntity<String> restore(@RequestParam MultipartFile file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new SafeObjectInputStream(file.getInputStream())) {
            List<Todo> restored = (List<Todo>) in.readObject();
            for (Todo todo : restored) {
                todoRepository.save(todo);
            }
        }
        return ResponseEntity.ok("restored");
    }

    private static class SafeObjectInputStream extends ObjectInputStream {

        private static final Set<String> ALLOWED_CLASSES = Set.of(
                java.util.ArrayList.class.getName(),
                Todo.class.getName(),
                java.lang.String.class.getName(),
                java.lang.Long.class.getName(),
                java.lang.Number.class.getName(),
                java.lang.Boolean.class.getName()
        );

        SafeObjectInputStream(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass osc) throws IOException, ClassNotFoundException {
            if (!ALLOWED_CLASSES.contains(osc.getName())) {
                throw new InvalidClassException("Unauthorized deserialization", osc.getName());
            }
            return super.resolveClass(osc);
        }
    }
}
