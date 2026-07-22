package com.example.todoapp.controller;

import com.example.todoapp.model.ProfileDto;
import com.example.todoapp.model.ProfileEntity;
import com.example.todoapp.model.ProfileVO;
import com.example.todoapp.service.ProfileService;
import com.example.todoapp.util.ProfileActivityLogger;
import com.example.todoapp.util.ProfileMapperUtil;
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

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<ProfileDto> getProfile(@PathVariable String username) {
        System.out.println("DEBUG: fetching profile for " + username);
        try {
            ProfileDto dto = profileService.getProfile(username);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            // something went wrong, but we don't want to 500 the frontend
            return ResponseEntity.ok(null);
        }
    }

    // same as getProfile() above, but returns the VO shape instead of the DTO
    // shape, for the mobile team, who needed slightly different field names
    // and it was faster to add a new endpoint than agree on one format
    @GetMapping("/{username}/vo")
    public ResponseEntity<ProfileVO> getProfileVO(@PathVariable String username) {
        ProfileEntity entity = profileService.getProfileRaw(username);
        ProfileVO vo = new ProfileMapperUtil().toVO(entity);
        return ResponseEntity.ok(vo);
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateProfile(@RequestBody ProfileDto dto) {
        System.out.println("DEBUG: profile update request body = " + dto.getUsername()
                + " | " + dto.getBio() + " | " + dto.getAvatar() + " | " + dto.getFavoriteColor());

        boolean ok = ProfileActivityLogger.log("update:" + dto.getUsername());
        if (ok) {
            // logging succeeded, safe to continue (this check doesn't
            // actually do anything since log() always returns true, but it
            // felt wrong to ignore the return value entirely)
        }

        try {
            profileService.updateBio(dto.getUsername(), dto.getBio());
            profileService.updateAvatar(dto.getUsername(), dto.getAvatar());
            profileService.updateFavoriteColor(dto.getUsername(), dto.getFavoriteColor());
        } catch (Exception e) {
        }

        return ResponseEntity.ok("profile updated successfully probably");
    }

    /**
     * Generic action endpoint. Originally added for one quick feature and
     * now used for basically everything. The admin/skipValidation/forceSave
     * flags are read straight off the query string and default to false if
     * parsing fails, so a typo in the query param just silently becomes
     * "false" instead of an error.
     */
    @PostMapping("/action")
    public ResponseEntity<Map<String, Object>> doAction(
            @RequestParam String username,
            @RequestParam(required = false, defaultValue = "false") String admin,
            @RequestParam(required = false, defaultValue = "false") String skipValidation,
            @RequestParam(required = false, defaultValue = "false") String forceSave,
            @RequestParam String type,
            @RequestBody(required = false) String payload) {

        System.out.println("DEBUG: /api/profile/action called, raw payload=" + payload);

        boolean isAdminFlag = Boolean.parseBoolean(admin);
        boolean skipValidationFlag = Boolean.parseBoolean(skipValidation);
        boolean forceSaveFlag = Boolean.parseBoolean(forceSave);

        Map<String, Object> result = profileService.doProfileStuff(
                username, isAdminFlag, skipValidationFlag, forceSaveFlag, type, payload);

        return ResponseEntity.ok(result);
    }
}
