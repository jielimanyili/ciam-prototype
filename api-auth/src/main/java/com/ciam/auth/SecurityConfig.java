package com.ciam.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * BFF security (ADR-006, DESIGN §6): server-side {@code oauth2Login()} (Authorization Code) with a
 * session cookie — the access token stays server-side, never in the browser. Spring Boot's defaults
 * provide the session-security surface (NFR-9): HttpOnly session cookie, CSRF on, session-fixation
 * protection, and logout that clears the session + the IdP token.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> {})
            // B2B SSO: SAML 2.0 SP (ADR-004). Spring Security validates the assertion
            // (signature, conditions, audience, destination, InResponseTo) — no hand-rolled XML.
            .saml2Login(saml2 -> saml2.defaultSuccessUrl("/"))
            .logout(logout -> logout.logoutSuccessUrl("/"));
        return http.build();
    }
}
