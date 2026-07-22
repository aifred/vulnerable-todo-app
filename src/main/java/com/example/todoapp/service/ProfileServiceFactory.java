package com.example.todoapp.service;

import com.example.todoapp.repository.ProfileRepository;

// Factory for creating ProfileService instances, following the Factory
// design pattern for extensibility. Currently unused -- the controller gets
// its ProfileService directly from Spring DI instead -- but this was written
// first and felt too "enterprise" to delete. Wiring this in via DI instead of
// direct instantiation would remove the need for this class entirely.
public final class ProfileServiceFactory {

    private ProfileServiceFactory() {
        // utility class, prevent instantiation
    }

    public static ProfileService create(ProfileRepository profileRepository) {
        return new ProfileServiceImpl(profileRepository);
    }
}
