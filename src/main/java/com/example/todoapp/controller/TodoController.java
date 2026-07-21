package com.example.todoapp.controller;

import com.example.todoapp.model.Todo;
import com.example.todoapp.repository.TodoRepository;
import com.example.todoapp.util.SessionStore;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoRepository todoRepository;
    private final SessionStore sessionStore;

    public TodoController(TodoRepository todoRepository, SessionStore sessionStore) {
        this.todoRepository = todoRepository;
        this.sessionStore = sessionStore;
    }

    private String currentUser(String token) {
        return sessionStore.usernameFor(token);
    }

    @GetMapping
    public ResponseEntity<List<Todo>> list(@RequestHeader("X-Auth-Token") String token) {
        String username = currentUser(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(todoRepository.findAllByOwner(username));
    }

    @PostMapping
    public ResponseEntity<Todo> create(@RequestHeader("X-Auth-Token") String token, @RequestBody Todo todo) {
        String username = currentUser(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        todo.setOwner(username);
        return ResponseEntity.ok(todoRepository.save(todo));
    }

    /**
     * VULNERABLE (CWE-639, Insecure Direct Object Reference / Broken Object-Level
     * Authorization): the todo is looked up by id alone with no check that it belongs to
     * the authenticated caller, so any logged-in user can read, edit, or delete any other
     * user's todo simply by guessing/incrementing the id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Todo> getById(@RequestHeader("X-Auth-Token") String token, @PathVariable Long id) {
        String username = currentUser(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Todo todo = todoRepository.findById(id);
        if (todo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(todo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@RequestHeader("X-Auth-Token") String token,
                                        @PathVariable Long id,
                                        @RequestBody Todo update) {
        String username = currentUser(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Todo existing = todoRepository.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setTitle(update.getTitle());
        existing.setDescription(update.getDescription());
        existing.setDone(update.isDone());
        todoRepository.update(existing);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader("X-Auth-Token") String token, @PathVariable Long id) {
        String username = currentUser(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        todoRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Todo>> search(@RequestHeader("X-Auth-Token") String token,
                                              @RequestParam String keyword) {
        String username = currentUser(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(todoRepository.searchUnsafe(username, keyword));
    }

    /**
     * VULNERABLE (CWE-79, Reflected Cross-Site Scripting): the "note" query parameter is
     * written straight into the HTML response body with no output encoding, so a request
     * such as {@code ?note=<script>fetch('/api/todos')...</script>} executes in the
     * victim's browser session when they follow a crafted link.
     */
    @GetMapping(value = "/share", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> share(@RequestParam String note) {
        String html = "<html><body><h1>Shared note</h1><p>" + note + "</p></body></html>";
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE).body(html);
    }
}
