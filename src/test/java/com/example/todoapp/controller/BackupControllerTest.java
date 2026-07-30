package com.example.todoapp.controller;

import com.example.todoapp.model.Todo;
import com.example.todoapp.repository.TodoRepository;
import com.example.todoapp.util.SessionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private SessionStore sessionStore;

    private String authToken;

    @BeforeEach
    void setUp() {
        todoRepository.deleteAll();
        authToken = "test-token-" + System.currentTimeMillis();
        sessionStore.storeToken(authToken, "testuser");
    }

    @Test
    void exportSerializesTodosSuccessfully() throws Exception {
        Todo todo = new Todo(1L, "Test Todo", "Description", false, "testuser");
        todoRepository.save(todo);

        mockMvc.perform(get("/api/backup/export")
                        .header("X-Auth-Token", authToken))
                .andExpect(status().isOk());
    }

    @Test
    void restoreDeserializesAllowedClassesSuccessfully() throws Exception {
        List<Todo> todos = new ArrayList<>();
        todos.add(new Todo(1L, "Task 1", "Description 1", false, "testuser"));
        todos.add(new Todo(2L, "Task 2", "Description 2", true, "testuser"));

        byte[] serialized = serializeTodos(todos);
        MockMultipartFile file = new MockMultipartFile("file", serialized);

        mockMvc.perform(multipart("/api/backup/restore")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("restored")));

        List<Todo> restored = todoRepository.findAllByOwner("testuser");
        assert restored.size() == 2;
    }

    @Test
    void restoreWithMaliciousClassThrowsException() throws Exception {
        byte[] maliciousPayload = createMaliciousPayload();
        MockMultipartFile file = new MockMultipartFile("file", maliciousPayload);

        mockMvc.perform(multipart("/api/backup/restore")
                        .file(file))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void restoreWithEmptyFileSucceeds() throws Exception {
        List<Todo> emptyList = new ArrayList<>();
        byte[] serialized = serializeTodos(emptyList);
        MockMultipartFile file = new MockMultipartFile("file", serialized);

        mockMvc.perform(multipart("/api/backup/restore")
                        .file(file))
                .andExpect(status().isOk());
    }

    private byte[] serializeTodos(List<Todo> todos) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(todos);
        }
        return bytes.toByteArray();
    }

    private byte[] createMaliciousPayload() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject("String is allowed, but let's test with a malicious object");
        }
        return bytes.toByteArray();
    }
}
