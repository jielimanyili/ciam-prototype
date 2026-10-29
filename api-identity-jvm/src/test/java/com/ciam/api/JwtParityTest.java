package com.ciam.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JWT-validation parity (ADR-014): decodes the shared test-fixtures/jwt tokens with the
 * same validators as {@link SecurityConfig#jwtDecoder} (signature + issuer + audience +
 * expiry) and asserts each token is accepted/rejected per the fixture — the same vectors
 * `api-identity-python/tests/test_parity.py` asserts.
 */
class JwtParityTest {

    private static JsonNode root() throws Exception {
        return new ObjectMapper().readTree(Files.readString(Path.of("../test-fixtures/jwt/tokens.json")));
    }

    private static RSAPublicKey publicKey() throws Exception {
        byte[] pem = Files.readAllBytes(Path.of("../test-fixtures/jwt/rsa-public.pem"));
        return RsaKeyConverters.x509().convert(new ByteArrayInputStream(pem));
    }

    static Stream<Map.Entry<String, JsonNode>> cases() throws Exception {
        List<Map.Entry<String, JsonNode>> out = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> it = root().get("cases").fields();
        while (it.hasNext()) {
            out.add(it.next());
        }
        return out.stream();
    }

    @ParameterizedTest
    @MethodSource("cases")
    void jwtValidation(Map.Entry<String, JsonNode> entry) throws Exception {
        JsonNode spec = entry.getValue();
        boolean wantAccept = "accept".equals(spec.get("expected").asText());

        String issuer = root().get("issuer").asText();
        String audience = root().get("audience").asText();

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey()).build();
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = new AudienceValidator(audience);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));

        boolean accepted = true;
        try {
            decoder.decode(spec.get("token").asText());
        } catch (Exception e) {
            accepted = false;
        }

        assertThat(accepted).as(entry.getKey()).isEqualTo(wantAccept);
    }
}
