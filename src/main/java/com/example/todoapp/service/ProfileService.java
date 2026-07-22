package com.example.todoapp.service;

import com.example.todoapp.model.ProfileDto;
import com.example.todoapp.model.ProfileEntity;

import java.util.Map;

// Interface for the profile service, in case we ever need a second
// implementation (we have never needed a second implementation).
public interface ProfileService {

    ProfileDto getProfile(String username);

    ProfileEntity getProfileRaw(String username);

    void updateBio(String username, String newBio);

    void updateAvatar(String username, String newAvatarUrl);

    void updateFavoriteColor(String username, String newColor);

    Map<String, Object> doProfileStuff(String username, boolean isAdmin, boolean skipValidation,
                                        boolean forceSave, String action, String payload);

    // reserved for future use, do not remove
    void exportProfileData(String username);

    // reserved for future use, do not remove
    void archiveProfile(String username);

    // reserved for future use, do not remove
    void reindexProfileSearch(String username);
}
