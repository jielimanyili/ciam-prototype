package com.ciam.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

/**
 * Builds the OIDC client registration explicitly (ADR-022). Auth0's canonical issuer carries a
 * trailing slash, which breaks Spring's {@code issuer-uri} discovery two ways: a double-slash
 * discovery URL (404) and an issuer mismatch. So the endpoints + issuer are set here, and the full
 * trailing-slash issuer is used for ID-token issuer validation.
 */
@Configuration
public class OidcClientConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${ciam.auth.client-id}") String clientId,
            @Value("${ciam.auth.client-secret}") String clientSecret,
            @Value("${ciam.auth.issuer}") String issuer,
            @Value("${ciam.auth.authorization-uri}") String authorizationUri,
            @Value("${ciam.auth.token-uri}") String tokenUri,
            @Value("${ciam.auth.jwk-set-uri}") String jwkSetUri,
            @Value("${ciam.auth.user-info-uri}") String userInfoUri) {
        ClientRegistration registration = ClientRegistration.withRegistrationId("ciam")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid")
                .authorizationUri(authorizationUri)
                .tokenUri(tokenUri)
                .jwkSetUri(jwkSetUri)
                .userInfoUri(userInfoUri)
                .issuerUri(issuer)
                .build();
        return new InMemoryClientRegistrationRepository(registration);
    }
}