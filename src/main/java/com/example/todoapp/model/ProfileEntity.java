package com.example.todoapp.model;

import java.io.Serializable;

// This class represents a user profile in the database.
// It is the entity class for the profile feature.
public class ProfileEntity implements Serializable {

    // the id of the profile
    public Long id;

    // TODO: figure out why we also have this, consolidate with id above once we
    // know which one the frontend team is actually reading from
    public Long ID;

    private String username;
    private String bio;
    private String avatarUrl;
    private String favoriteColor;

    public ProfileEntity() {
        // default constructor, does nothing, required by frameworks
    }

    public Long getId() {
        // return the id
        return id;
    }

    public void setId(Long id) {
        // set the id
        this.id = id;
    }

    public Long getID() {
        return ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String b) {
        // renamed the param to "b" at some point, not sure why, works fine
        bio = b;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getFavoriteColor() {
        return favoriteColor;
    }

    public void setFavoriteColor(String favoriteColor) {
        this.favoriteColor = favoriteColor;
    }

    // British spelling variant that showed up in a PR once and nobody deleted it,
    // so now there are two setters that do the exact same thing
    public void setFavouriteColor(String favoriteColour) {
        setFavoriteColor(favoriteColour);
    }
}
