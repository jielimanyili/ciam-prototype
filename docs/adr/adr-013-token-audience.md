# ADR-013 — Token audience strategy (Keycloak audience mappers)

- **Status:** Accepted
- **Date:** 2026-10-29
- **Serves:** FR-4, FR-5 · A-2

## Context

Both resource servers validate `aud` strictly — the Java `AudienceValidator` requires `aud` to
contain the configured audience; the Python server uses PyJWT's `audience=` check. But Keycloak
does not emit the resource-server audience by default: a browser token's `aud` defaults to
`account` unless an audience mapper is configured. So the end-to-end demo would fail audience
validation.

## Decision

Mint the audience in the IdP, not by weakening the resource servers:

- Add **audience protocol mappers** (`oidc-audience-mapper`, `included.custom.audience`) to the
  realm clients so tokens carry the right `aud`:
  - `ui-identity` (SPA) → `api-identity-jvm` **and** `api-identity-python`
  - `ciam-service` (M2M) → `api-identity-jvm`
- Keep strict `aud` validation in both resource servers (defense in depth; no lenient fallback).
- Distinct audiences per resource server (`api-identity-jvm` vs `api-identity-python`) prevent
  replay of a token minted for one server against the other — unless the client explicitly requests
  both audiences (as `ui-identity` does, which is what makes the "one SPA token, two languages"
  demo possible).

## Consequences

- **Pros:** the demo works end-to-end; `aud` stays enforced; audiences are self-describing.
- **Cons:** audience config lives in the realm (one more thing to replicate when swapping
  Keycloak → Okta).

## Alternatives considered

- **Lenient audience check (log-only)** — rejected: weakens the security story.
- **Shared audience (`api-identity`)** — rejected: collapses the two resource servers into one
  audience, losing per-server replay protection.
