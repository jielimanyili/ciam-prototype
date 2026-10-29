package com.ciam.api;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Requires the JWT's {@code aud} claim to contain the expected audience, so a token
 * minted for a different resource server cannot be replayed here.
 *
 * <p>This is the one check Spring Boot's {@code issuer-uri} auto-config does NOT do by
 * default — signature (JWKS/RS256), issuer, and expiry are automatic; audience is not.
 * This validator is the pattern that moves into the SDK Spring Boot starter (ADR-007).
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final String audience;

    public AudienceValidator(String audience) {
        this.audience = audience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (jwt.getAudience() != null && jwt.getAudience().contains(audience)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error(
                        "invalid_token",
                        "The required audience '" + audience + "' is missing",
                        null));
    }
}
