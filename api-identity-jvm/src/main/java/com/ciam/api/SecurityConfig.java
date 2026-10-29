package com.ciam.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Resource-server security. The {@code JwtDecoder} (JWKS/RS256 + issuer + audience) is
 * auto-configured by {@code sdk-identity-jvm-spring-boot-starter} (ADR-007) from
 * {@code ciam.identity.issuer-uri} / {@code ciam.identity.audience} — this class only owns the
 * route/scope policy:
 *  - /health is public
 *  - /api/admin requires the `admin` scope (JWT `scope` claim -> SCOPE_admin authority)
 *  - everything else requires a valid bearer token
 */
@Configuration
public class SecurityConfig {

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

