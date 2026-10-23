package com.example.ciam;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * Resource-server security:
     *  - /health is public
     *  - /api/admin requires the `admin` scope (JWT -> SCOPE_admin authority)
     *  - everything else requires a valid bearer token
     *
     * JWT validation (RS256 signature via JWKS + issuer + expiry) is provided by
     * Spring Boot auto-config from `spring.security.oauth2.resourceserver.jwt.issuer-uri`.
     * To also enforce audience, add a custom OAuth2TokenValidator (see docs/best-practices.md §2).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health", "/actuator/health").permitAll()
                .requestMatchers("/api/admin").hasAuthority("SCOPE_admin")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
        return http.build();
    }
}
