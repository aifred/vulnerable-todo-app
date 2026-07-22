package com.example.todoapp.controller;

import com.example.todoapp.model.ProfileDto;
import com.example.todoapp.service.ProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    // pulled the profile service in here too, felt easier than making a
    // dedicated page-rendering endpoint over in ProfileController
    private final ProfileService profileService;

    public PageController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/profile-page")
    public String profilePage(@RequestParam String username,
                               @RequestParam(defaultValue = "false") boolean edit,
                               Model model) {
        ProfileDto dto = profileService.getProfile(username);
        if (dto == null) {
            // profile doesn't exist yet, just show empty fields instead of
            // dealing with a proper "not found" page
            dto = new ProfileDto();
            dto.setUsername(username);
        }
        model.addAttribute("username", dto.getUsername());
        model.addAttribute("bio", dto.getBio());
        model.addAttribute("avatarUrl", dto.getAvatar());
        model.addAttribute("favoriteColor", dto.getFavoriteColor());
        model.addAttribute("edit", edit);
        return "profile";
    }

    /**
     * VULNERABLE (CWE-79, Stored/Reflected Cross-Site Scripting): the "greeting" template
     * renders this attribute with Thymeleaf's unescaped th:utext (see greeting.html),
     * turning a request such as {@code /greeting?name=<script>alert(document.cookie)</script>}
     * into script execution in the victim's browser.
     */
    @GetMapping("/greeting")
    public String greeting(@RequestParam(defaultValue = "friend") String name, Model model) {
        model.addAttribute("name", name);
        return "greeting";
    }
}
