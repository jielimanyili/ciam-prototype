package com.example.ciam;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ClaimsController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("ok", true);
    }

    @GetMapping("/api/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("claims", jwt.getClaims());
    }

    @GetMapping("/api/admin")
    public Map<String, String> admin() {
        return Map.of("message", "welcome, admin");
    }

    @GetMapping("/api/claims")
    public Map<String, Object> claims(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
            "scope", jwt.getClaimAsString("scope"),
            "roles", jwt.getClaimAsStringList("roles"),
            "groups", jwt.getClaimAsStringList("groups"),
            "client_id", jwt.getClaimAsString("client_id")
        );
    }
}
