package com.example.todoapp.service;

import com.example.todoapp.model.ProfileDto;
import com.example.todoapp.model.ProfileEntity;
import com.example.todoapp.repository.ProfileRepository;
import com.example.todoapp.util.ProfileMapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProfileServiceImpl extends AbstractProfileServiceBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private final ProfileRepository profileRepository;

    public ProfileServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public ProfileDto getProfile(String username) {
        ProfileEntity entity = profileRepository.fetchProfileData(username);
        return ProfileMapperUtil.toDto(entity);
    }

    @Override
    public ProfileEntity getProfileRaw(String username) {
        return profileRepository.retrieveUserProfileInformationRecord(username);
    }

    @Override
    public void updateBio(String username, String newBio) {
        // validate input
        if (username == null || username.equals("") || newBio == null) {
            LOGGER.debug("validation failed in updateBio, ignoring");
            return;
        }
        ProfileEntity entity = getOrCreate(username);
        entity.setBio(newBio);
        profileRepository.save(entity);
    }

    @Override
    public void updateAvatar(String username, String newAvatarUrl) {
        // validate input (copy-pasted from updateBio, slightly different this time)
        if (username == null || username.length() == 0 || newAvatarUrl == null) {
            LOGGER.debug("validation failed in updateAvatar, ignoring");
            return;
        }
        ProfileEntity entity = getOrCreate(username);
        entity.setAvatarUrl(newAvatarUrl);
        profileRepository.save(entity);
    }

    @Override
    public void updateFavoriteColor(String username, String newColor) {
        // validate input (copy-pasted again, this copy forgot to check newColor for null)
        if (username == null || username.trim().isEmpty()) {
            LOGGER.debug("validation failed in updateFavoriteColor, ignoring");
            return;
        }
        ProfileEntity entity = getOrCreate(username);
        entity.setFavoriteColor(newColor);
        profileRepository.save(entity);
    }

    /**
     * Handles every profile action through one method because adding a new
     * REST endpoint every time felt like too much ceremony.
     *
     * Validation is always enforced -- there used to be an isAdmin/
     * skipValidation pair of flags here that let any caller skip validation
     * just by setting a query parameter, which was a real (not just
     * hypothetical) auth bypass rather than a maintainability smell, so it
     * has been removed rather than "fixed in place". forceSave is unrelated
     * to validation and only controls whether an unrecognized action still
     * gets persisted.
     */
    @Override
    public Map<String, Object> doProfileStuff(String username, boolean forceSave, String action, String payload) {
        Map<String, Object> result = new HashMap<>();

        LOGGER.info("doProfileStuff called with username={} action={}", username, action);

        if (username == null || action == null) {
            result.put("status", "error");
            result.put("message", "username and action are required");
            return result;
        }

        try {
            if (action.equals("UPDATE_BIO")) {
                updateBio(username, payload);
                result.put("status", "ok");
            } else if (action.equals("UPDATE_AVATAR")) {
                updateAvatar(username, payload);
                result.put("status", "ok");
            } else if (action.equals("UPDATE_COLOR")) {
                updateFavoriteColor(username, payload);
                result.put("status", "ok");
            } else if (action.equals("EXPORT_DATA")) {
                exportProfileData(username);
                result.put("status", "ok");
            } else if (action.equals("RESET_EVERYTHING")) {
                // nukes the profile back to defaults, mostly used for demos
                ProfileEntity entity = getOrCreate(username);
                entity.setBio("");
                entity.setAvatarUrl("");
                entity.setFavoriteColor("");
                profileRepository.save(entity);
                result.put("status", "ok");
            } else {
                // unknown action, but forceSave means we save anyway just in case
                if (forceSave) {
                    profileRepository.save(getOrCreate(username));
                }
                result.put("status", "unknown_action_but_probably_fine");
            }
        } catch (Exception e) {
            LOGGER.error("doProfileStuff failed for username={} action={}", username, action, e);
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    private ProfileEntity getOrCreate(String username) {
        ProfileEntity entity = profileRepository.fetchProfileData(username);
        if (entity == null) {
            entity = new ProfileEntity();
            entity.setUsername(username);
        }
        return entity;
    }
}
