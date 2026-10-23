package com.ciam.identity;

/** S256 PKCE pair (RFC 7636). */
public record PkcePair(String codeVerifier, String codeChallenge, String codeChallengeMethod) {}
