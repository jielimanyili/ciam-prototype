package com.ciam.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Endpoint parity (ADR-014): asserts the shared parity-matrix.json against the Spring
 * resource server — the same matrix `api-identity-python/tests/test_parity.py` asserts.
 */
@WebMvcTest(ClaimsController.class)
@Import(SecurityConfig.class)
class ParityTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    JwtDecoder jwtDecoder;

    static Stream<JsonNode> endpoints() throws Exception {
        JsonNode root = new ObjectMapper()
                .readTree(Files.readString(Path.of("../test-fixtures/parity-matrix.json")));
        List<JsonNode> out = new ArrayList<>();
        root.get("endpoints").forEach(out::add);
        return out.stream();
    }

    @ParameterizedTest
    @MethodSource("endpoints")
    void endpointMatrix(JsonNode c) throws Exception {
        String path = c.get("path").asText();
        int expected = c.get("expect").asInt();

        MockHttpServletRequestBuilder req = get(path);
        if ("bearer".equals(c.get("auth").asText())) {
            String scope = c.get("scope").asText();
            List<GrantedAuthority> authorities = new ArrayList<>();
            for (String s : scope.split("\\s+")) {
                authorities.add(new SimpleGrantedAuthority("SCOPE_" + s));
            }
            req = req.with(jwt().jwt(j -> j.claim("scope", scope)).authorities(authorities));
        }

        mvc.perform(req).andExpect(status().is(expected));
    }
}
