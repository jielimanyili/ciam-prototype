package com.ciam.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
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
        return request -> {
            OAuth2AuthorizationRequest authRequest = delegate.resolve(request);
            if (authRequest == null || audience == null || audience.isBlank()) {
                return authRequest;
            }
            Map<String, Object> additionalParameters = new HashMap<>(authRequest.getAdditionalParameters());
            additionalParameters.put("audience", audience);
            return OAuth2AuthorizationRequest.from(authRequest)
                    .additionalParameters(additionalParameters)
                    .build();
        };
    }
}
