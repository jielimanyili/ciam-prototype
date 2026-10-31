package com.ciam.identity.autoconfigure;

import com.ciam.identity.OidcDiscovery;
import com.ciam.identity.OidcMetadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Secure-by-default resource-server JWT validation (ADR-007, DESIGN §1): with just
 * {@code ciam.identity.issuer-uri} (+ optional {@code ciam.identity.audience}), a resource server
 * gets a {@link JwtDecoder} that verifies RS256/JWKS + issuer + expiry — and, when an audience is
 * configured, the {@code aud} claim that Spring does not check by default.
 *
 * <p>Scope enforcement then works out of the box via Spring Security's default mapping of the
 * {@code scope} claim to {@code SCOPE_*} authorities — e.g. {@code .hasAuthority("SCOPE_admin")} —
 * with {@code sdk-identity-jvm}'s {@code ScopeAssertions} for programmatic checks.
 */
@AutoConfiguration
@EnableConfigurationProperties(IdentityProperties.class)
@ConditionalOnClass({ JwtDecoder.class, NimbusJwtDecoder.class })
@ConditionalOnProperty(prefix = "ciam.identity", name = "issuer-uri")
public class IdentityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(IdentityProperties properties) {
        // Auth0's canonical issuer carries a trailing slash, which breaks Spring's
        // JwtDecoders.fromIssuerLocation two ways: (1) a trailing slash makes Spring build a
        // double-slash discovery URL (404), and (2) it validates that the discovery doc's `issuer`
        // equals the requested one — Auth0's keeps the slash, so a stripped issuer mismatches.
        // Discover the JWKS via the SDK (which strips the slash) and validate issuer/audience
        // ourselves; the full issuer (trailing slash) is used for the strict `iss` claim check.
        OidcMetadata metadata;
        try {
            metadata = OidcDiscovery.discover(properties.getIssuerUri());
        }
        catch (IOException | InterruptedException e) {
            throw new IllegalStateException("OIDC discovery failed for issuer " + properties.getIssuerUri(), e);
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(metadata.jwksUri()).build();

        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(JwtValidators.createDefaultWithIssuer(properties.getIssuerUri()));
        if (properties.getAudience() != null && !properties.getAudience().isBlank()) {
            validators.add(new AudienceValidator(properties.getAudience()));
        }
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));

        return decoder;
    }
}
