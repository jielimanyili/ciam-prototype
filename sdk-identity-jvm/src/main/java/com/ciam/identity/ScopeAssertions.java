package com.ciam.identity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Scope + claim helpers for authorizing against a verified token's claims.
 * Operates on {@code Map<String, Object>} so it interops directly with Spring
 * Security's {@code Jwt.getClaims()}.
 */
public final class ScopeAssertions {

    private ScopeAssertions() {}

    public static final class InsufficientScopeError extends RuntimeException {
        public InsufficientScopeError(String scope) {
            super("missing required scope: " + scope);
        }
    }

    public static List<String> scopesOf(Map<String, Object> claims) {
        Object scope = claims.get("scope");
        if (!(scope instanceof String s) || s.isBlank()) {
            return List.of();
        }
        return Arrays.stream(s.trim().split("\\s+")).filter(x -> !x.isEmpty()).toList();
    }

    public static void requireScope(Map<String, Object> claims, String required) {
        if (!scopesOf(claims).contains(required)) {
            throw new InsufficientScopeError(required);
        }
    }

    public static boolean hasClaim(Map<String, Object> claims, String key, Object value) {
        Object v = claims.get(key);
        if (value == null) {
            return v != null;
        }
        if (v instanceof List<?> list) {
            return list.contains(value);
        }
        return value.equals(v);
    }
}
