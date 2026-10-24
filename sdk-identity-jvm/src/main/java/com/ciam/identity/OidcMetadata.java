package com.ciam.identity;

/** OIDC discovery document (subset of RFC 8414). */
public record OidcMetadata(String issuer, String authorizationEndpoint,
                           String tokenEndpoint, String jwksUri, String endSessionEndpoint) {}
