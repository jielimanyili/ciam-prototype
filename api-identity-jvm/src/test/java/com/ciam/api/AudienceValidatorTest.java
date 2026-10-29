package com.ciam.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

class AudienceValidatorTest {

    private final AudienceValidator validator = new AudienceValidator("api-identity-jvm");

    @Test
    void acceptsMatchingAudience() {
        OAuth2TokenValidatorResult result = validator.validate(jwtWithAudience("api-identity-jvm"));
        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void rejectsMissingAudience() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "demo")
                .build();
        assertThat(validator.validate(jwt).hasErrors()).isTrue();
    }

    @Test
    void rejectsWrongAudience() {
        assertThat(validator.validate(jwtWithAudience("other-api")).hasErrors()).isTrue();
    }

    private Jwt jwtWithAudience(String audience) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("aud", audience)
                .claim("sub", "demo")
                .build();
    }
}
