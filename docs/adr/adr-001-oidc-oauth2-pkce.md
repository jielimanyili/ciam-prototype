# ADR-001 — OIDC/OAuth2 flows and RS256/JWKS validation

- **Status:** Accepted
- **Date:** 2026-10-21
- **Serves:** FR-1, FR-2, FR-4, FR-5, NFR-1, NFR-2, NFR-3, NFR-6

## Context

The platform centers on CIAM: OIDC/OAuth 2.0 (Authorization Code
with PKCE, Client Credentials), JWT validation, scoping, and claims handling.

## Decision

1. Use **Authorization Code + PKCE (S256)** for the SPA and **Client Credentials**
   for server-to-server — the two grants the OAuth2/OIDC standards name explicitly.
2. Validate access tokens as **RS256 (also 384/512) JWTs against the IdP's JWKS**
   (via OIDC discovery → `jwks_uri`), enforcing **issuer + audience**.
3. Authorize via the **`scope` claim** (`requireScope`) and expose **claims**
   (`roles`/`groups`) for finer-grained decisions.
4. Package all of it as a **reusable SDK** (`sdk-identity-jvm`) so product squads
   integrate identity "safely" — the developer-advocacy requirement.

## Consequences

- **Pros:** standards-based, provider-agnostic (Okta ↔ Keycloak), testable
  offline (local JWKS), and the SDK becomes the single integration point.
- **Cons:** PKCE + token exchange adds moving parts vs. a session cookie; JWKS
  adds a network hop (mitigated by caching in the SDK's remote JWK set).

## Alternatives considered

- **Implicit flow** — rejected: deprecated, tokens in the URL, no PKCE.
- **Symmetric (HS256) validation** — rejected: requires a shared secret on every
  service; RS256/JWKS keeps the key at the IdP and supports rotation.
