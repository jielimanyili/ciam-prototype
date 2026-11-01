package com.ciam.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Resource-server security. The {@code JwtDecoder} (JWKS/RS256 + issuer + audience) is
 * auto-configured by {@code sdk-identity-jvm-spring-boot-starter} (ADR-007) from
 * {@code ciam.identity.issuer-uri} / {@code ciam.identity.audience} — this class only owns the
 * route/scope policy + CORS:
 *  - /health is public
 *  - /api/admin requires the `admin` scope (JWT `scope` claim -> SCOPE_admin authority)
 *  - everything else requires a valid bearer token
 *
 * CORS allows the SPA origin — on Render the SPA and API are different origins (ADR-022); on AWS
 * they shared one CloudFront domain so no CORS was needed.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health", "/actuator/health").permitAll()
                .requestMatchers("/api/admin").hasAuthority("SCOPE_admin")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${ciam.cors.allowed-origins:https://ui-identity.onrender.com,http://localhost:4200}") String allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        for (String origin : allowedOrigins.split(",")) {
            config.addAllowedOrigin(origin.trim());
        }
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

