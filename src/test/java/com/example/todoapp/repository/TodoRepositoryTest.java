package com.example.todoapp.repository;

import com.example.todoapp.model.Todo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Transactional
class TodoRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private TodoRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TodoRepository(jdbcTemplate);
        jdbcTemplate.update("DELETE FROM todos");
    }

    private Todo newTodo(String title, String description, String owner) {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setDone(false);
        todo.setOwner(owner);
        todo.setAttachmentPath(null);
        return todo;
    }

    @Test
    void saveAssignsGeneratedIdAndPersistsFields() {
        Todo todo = newTodo("Buy milk", "From the store", "alice");

        Todo saved = repository.save(todo);

        assertNotNull(saved.getId());

        Todo fetched = repository.findById(saved.getId());
        assertNotNull(fetched);
        assertEquals("Buy milk", fetched.getTitle());
        assertEquals("From the store", fetched.getDescription());
        assertEquals("alice", fetched.getOwner());
        assertFalse(fetched.isDone());
    }

    @Test
    void findByIdReturnsNullWhenNotFound() {
        assertNull(repository.findById(999L));
    }

    @Test
    void findAllByOwnerReturnsOnlyThatOwnersTodos() {
        repository.save(newTodo("Alice task 1", "d1", "alice"));
        repository.save(newTodo("Alice task 2", "d2", "alice"));
        repository.save(newTodo("Bob task", "d3", "bob"));

        List<Todo> aliceTodos = repository.findAllByOwner("alice");

        assertEquals(2, aliceTodos.size());
        assertFalse(aliceTodos.stream().anyMatch(t -> !"alice".equals(t.getOwner())));
    }

    @Test
    void updateModifiesTitleDescriptionDoneAndAttachmentPath() {
        Todo saved = repository.save(newTodo("Original", "Original desc", "alice"));

        saved.setTitle("Updated");
        saved.setDescription("Updated desc");
        saved.setDone(true);
        saved.setAttachmentPath("/uploads/file.txt");
        repository.update(saved);

        Todo fetched = repository.findById(saved.getId());
        assertNotNull(fetched);
        assertEquals("Updated", fetched.getTitle());
        assertEquals("Updated desc", fetched.getDescription());
        assertTrue(fetched.isDone());
        assertEquals("/uploads/file.txt", fetched.getAttachmentPath());
        assertEquals("alice", fetched.getOwner());
    }

    @Test
    void deleteByIdRemovesTheRow() {
        Todo saved = repository.save(newTodo("Temp", "desc", "alice"));

        repository.deleteById(saved.getId());

        assertNull(repository.findById(saved.getId()));
    }

    @Test
    void searchUnsafeMatchesTitleOrDescriptionForOwner() {
        repository.save(newTodo("Buy milk", "From the store", "alice"));
        repository.save(newTodo("Walk dog", "Around the block", "alice"));
        repository.save(newTodo("Buy bread", "Bakery", "bob"));

        List<Todo> results = repository.searchUnsafe("alice", "milk");

        assertEquals(1, results.size());
        assertEquals("Buy milk", results.get(0).getTitle());
    }

    @Test
    void searchUnsafeIsVulnerableToSqlInjectionViaKeyword() {
        // Documents the intentional CWE-89 vulnerability: a crafted "keyword" breaks out
        // of the LIKE clause and lets an attacker read another owner's todos.
        repository.save(newTodo("Secret plan", "for bob only", "bob"));
        repository.save(newTodo("Alice todo", "nothing interesting", "alice"));

        String maliciousKeyword = "') UNION SELECT * FROM todos WHERE owner='bob' -- ";

        List<Todo> results = repository.searchUnsafe("alice", maliciousKeyword);

        assertTrue(results.stream().anyMatch(t -> "bob".equals(t.getOwner())),
                "expected the injected keyword to leak todos belonging to another owner");
    }
}
