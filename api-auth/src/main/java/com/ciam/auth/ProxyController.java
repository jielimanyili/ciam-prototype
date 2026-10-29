package com.ciam.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/**
 * BFF proxy: forwards {@code /api/me} and {@code /api/claims} to the resource server using the
 * server-side access token — the token never reaches the browser (ADR-006). The SPA talks only to
 * this service with its session cookie.
 */
@RestController
public class ProxyController {

    private final RestClient client;

    public ProxyController(RestClient client) {
        this.client = client;
    }

    @GetMapping("/api/me")
    public ResponseEntity<String> me(
            @RegisteredOAuth2AuthorizedClient("ciam") OAuth2AuthorizedClient authorized) {
        return forward(authorized, "api/me");
    }

    @GetMapping("/api/claims")
    public ResponseEntity<String> claims(
            @RegisteredOAuth2AuthorizedClient("ciam") OAuth2AuthorizedClient authorized) {
        return forward(authorized, "api/claims");
    }

    private ResponseEntity<String> forward(OAuth2AuthorizedClient authorized, String path) {
        String token = authorized.getAccessToken().getTokenValue();
        return client.get()
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .toEntity(String.class);
    }
}
