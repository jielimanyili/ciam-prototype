package com.ciam.identity;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ScopeAssertionsTest {

    @Test
    void parsesScopes() {
        Map<String, Object> claims = Map.of("scope", "openid profile admin");
        assertEquals(List.of("openid", "profile", "admin"), ScopeAssertions.scopesOf(claims));
    }

    @Test
    void requireScopeThrowsWhenMissing() {
        Map<String, Object> claims = Map.of("scope", "openid");
        assertThrows(ScopeAssertions.InsufficientScopeError.class,
                () -> ScopeAssertions.requireScope(claims, "admin"));
    }

    @Test
    void requireScopePassesWhenPresent() {
        Map<String, Object> claims = Map.of("scope", "openid admin");
        assertDoesNotThrow(() -> ScopeAssertions.requireScope(claims, "admin"));
    }

    @Test
    void hasClaimChecksValues() {
        Map<String, Object> claims = Map.of("groups", List.of("hr", "payroll"));
        assertTrue(ScopeAssertions.hasClaim(claims, "groups", "hr"));
        assertFalse(ScopeAssertions.hasClaim(claims, "groups", "legal"));
    }
}
