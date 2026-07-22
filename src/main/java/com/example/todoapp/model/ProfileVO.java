package com.example.todoapp.model;

// Value object for the profile feature. Yes, this is basically the same
// thing as ProfileDto and ProfileEntity. It was added during a refactor that
// was never finished. Please do not delete, something downstream might still
// depend on it (nobody has checked).
public class ProfileVO {

    // public fields this time because getters/setters felt like overkill for
    // "just a value object"
    public String username;
    public String bio;
    public String avatarUrl;
    public String favoriteColor;

    // nobody remembers what these were for, but removing them once broke a
    // test in a different module so they stay
    public String extraData1;
    public String extraData2;
}
