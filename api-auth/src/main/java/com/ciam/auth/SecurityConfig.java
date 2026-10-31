package com.ciam.auth;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
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

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final String audience;

    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository,
                          @Value("${ciam.auth.audience:}") String audience) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.audience = audience;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // The health endpoint must be public for the ALB (AWS) / Render health checks.
                .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(aep -> aep.authorizationRequestResolver(authorizationRequestResolver()))
            )
            // B2B SSO: SAML 2.0 SP (ADR-004). Spring Security validates the assertion
            // (signature, conditions, audience, destination, InResponseTo) — no hand-rolled XML.
            .saml2Login(saml2 -> saml2.defaultSuccessUrl("/"))
            .logout(logout -> logout.logoutSuccessUrl("/"));
        return http.build();
    }

    /**
     * Adds the API {@code audience} to the authorize request. Auth0 mints the access token for the
     * audience named here (so the BFF's forwarded token passes the resource server's {@code aud}
     * check); Keycloak ignores the parameter (its audience comes from mappers) — ADR-007.
     */
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver delegate =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
        // OAuth2AuthorizationRequestResolver declares two abstract resolve(...) methods, so it is
        // not a functional interface — implement both, delegating and adding the audience.
        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                return withAudience(delegate.resolve(request));
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                return withAudience(delegate.resolve(request, clientRegistrationId));
            }
        };
    }

    private OAuth2AuthorizationRequest withAudience(OAuth2AuthorizationRequest request) {
        if (request == null || audience == null || audience.isBlank()) {
            return request;
        }
        Map<String, Object> additionalParameters = new HashMap<>(request.getAdditionalParameters());
        additionalParameters.put("audience", audience);
        return OAuth2AuthorizationRequest.from(request)
                .additionalParameters(additionalParameters)
                .build();
    }
}
