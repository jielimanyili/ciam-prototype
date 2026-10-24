package com.ciam.identity;

/** OAuth2/OIDC token response. */
public record TokenResponse(String accessToken, String idToken, String refreshToken,
                            String tokenType, long expiresIn, String scope) {}
