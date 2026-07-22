package com.example.todoapp.service;

import com.example.todoapp.repository.ProfileRepository;

// Factory for creating ProfileService instances, following the Factory
// design pattern for extensibility. Currently unused -- the controller gets
// its ProfileService directly from Spring DI instead -- but this was written
// first and felt too "enterprise" to delete.
//
// TODO: wire this in via DI instead of direct instantiation, at some point.
public final class ProfileServiceFactory {

    private ProfileServiceFactory() {
    }

    public static ProfileService create(ProfileRepository profileRepository) {
        return new ProfileServiceImpl(profileRepository);
    }
}
