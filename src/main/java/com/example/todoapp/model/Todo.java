package com.example.todoapp.model;

import java.io.Serializable;

public class Todo implements Serializable {

    private Long id;
    private String title;
    private String description;
    private boolean done;
    private String owner;
    private String attachmentPath;

    public Todo() {
    }

    public Todo(Long id, String title, String description, boolean done, String owner) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.done = done;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }
}
