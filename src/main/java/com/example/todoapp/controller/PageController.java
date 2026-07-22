package com.example.todoapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
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
