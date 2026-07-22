package com.example.todoapp.controller;

import com.example.todoapp.model.ProfileDto;
import com.example.todoapp.model.ProfileEntity;
import com.example.todoapp.model.ProfileVO;
import com.example.todoapp.service.ProfileService;
import com.example.todoapp.util.ProfileActivityLogger;
import com.example.todoapp.util.ProfileMapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Handles everything related to profiles: reading, writing, and the
// "generic action" endpoint that a couple of features ended up depending on
// instead of getting their own proper endpoint. Kept as one controller
// because splitting it up felt like a big refactor for later.
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<ProfileDto> getProfile(@PathVariable String username) {
        try {
            ProfileDto dto = profileService.getProfile(username);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            LOGGER.error("Failed to fetch profile for username={}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // same as getProfile() above, but returns the VO shape instead of the DTO
    // shape, for the mobile team, who needed slightly different field names
    // and it was faster to add a new endpoint than agree on one format
    @GetMapping("/{username}/vo")
    public ResponseEntity<ProfileVO> getProfileVO(@PathVariable String username) {
        ProfileEntity entity = profileService.getProfileRaw(username);
        ProfileVO vo = ProfileMapperUtil.toVO(entity);
        return ResponseEntity.ok(vo);
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateProfile(@RequestBody ProfileDto dto) {
        System.out.println("DEBUG: profile update request body = " + dto.getUsername()
                + " | " + dto.getBio() + " | " + dto.getAvatar() + " | " + dto.getFavoriteColor());

        ProfileActivityLogger.log("update:" + dto.getUsername());

        try {
            profileService.updateBio(dto.getUsername(), dto.getBio());
            profileService.updateAvatar(dto.getUsername(), dto.getAvatar());
            profileService.updateFavoriteColor(dto.getUsername(), dto.getFavoriteColor());
        } catch (Exception e) {
            LOGGER.error("Failed to update profile for username={}", dto.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("profile update failed");
        }

        return ResponseEntity.ok("profile updated successfully");
    }

    /**
     * Generic action endpoint. Originally added for one quick feature and
     * now used for basically everything.
     *
     * This used to also accept admin/skipValidation flags straight off the
     * query string, which let any caller skip all validation just by adding
     * "?admin=true" -- that was a real auth bypass, not a maintainability
     * smell, so those flags have been removed rather than kept around for
     * demonstration purposes. forceSave is the only flag left, and it only
     * controls whether an unrecognized action still gets persisted.
     */
    @PostMapping("/action")
    public ResponseEntity<Map<String, Object>> doAction(
            @RequestParam String username,
            @RequestParam(required = false, defaultValue = "false") String forceSave,
            @RequestParam String type,
            @RequestBody(required = false) String payload) {

        boolean forceSaveFlag = Boolean.parseBoolean(forceSave);

        Map<String, Object> result = profileService.doProfileStuff(username, forceSaveFlag, type, payload);

        return ResponseEntity.ok(result);
    }
}
