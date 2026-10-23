# ADR-007 — Resource Server validation: library vs. sidecar vs. central service

- **Status:** Accepted
- **Date:** 2026-10-22
- **Serves:** FR-4, FR-5, FR-6

## Context

Every Resource Server must validate access tokens (JWT) and enforce scopes/claims.
There are three ways to centralize that validation:

1. **Library / Spring Boot starter** — compiled into the RS; validation stays embedded + stateless.
2. **Sidecar** — a helper process alongside each RS (same pod) validates and injects trusted headers; the RS stays auth-free.
3. **Central service** — a runtime validation endpoint the RS calls per request.

## Decision

Use a **shared library / Spring Boot starter** (`identity-sdk-jvm-spring-boot-starter`) as
the canonical validation mechanism. Each RS depends on it; validation stays **embedded and
stateless**. (Extends the SDK per ADR-002.)

## Consequences

- **Pros:** stateless + horizontally scalable (NFR-3); no extra hop; no runtime coupling;
  one place for validation conventions (FR-6 developer advocacy).
- **Cons:** language-specific (Java-only — acceptable under C-2).

## Alternatives considered

- **Sidecar** — rejected *for now*: language-agnostic and keeps the RS auth-free, but adds
  service-mesh operational overhead not justified for a single-stack (Java-only) system.
  Revisit if the org becomes polyglot or adopts a mesh.
- **Central service** — rejected: adds a network hop + single point of failure, and defeats
  the point of stateless JWT validation (only valid for opaque tokens via introspection).

## IdP migration

Migrating the IdP (Okta → Keycloak → Auth0) is a **config change, not a code change**:
- token / JWKS endpoints derive from `issuer` via OIDC discovery;
- `client_id` / `client_secret` are **Vault-managed** and refreshed centrally;
- service code is unchanged (SDK per ADR-002 + this library).
