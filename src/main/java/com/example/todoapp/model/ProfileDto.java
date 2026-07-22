package com.example.todoapp.model;

// Data transfer object for the profile feature. Basically the same fields as
// ProfileEntity but for the API layer, because you should never let your
// entity "leak" to the frontend (even though half the other controllers in
// this app do exactly that).
public class ProfileDto {

    private Long id;
    private String username;
    private String bio;

    // NOTE: called "avatar" here but "avatarUrl" on ProfileEntity and
    // "avatarUrl" again on ProfileVO. Yes, that's inconsistent. No, nobody
    // noticed until the mapper started dropping data.
    private String avatar;

    private String favoriteColor;

    public ProfileDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFavoriteColor() {
        return favoriteColor;
    }

    public void setFavoriteColor(String favoriteColor) {
        this.favoriteColor = favoriteColor;
    }
}
