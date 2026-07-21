package com.example.todoapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * VULNERABLE (CWE-352, Cross-Site Request Forgery): CSRF protection is disabled
     * application-wide, and every request is permitted with no authentication check at
     * the framework level (authorization is left entirely to ad-hoc token lookups in each
     * controller, several of which -- see TodoController -- get it wrong).
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
