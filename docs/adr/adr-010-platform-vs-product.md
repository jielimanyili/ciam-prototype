# ADR-010 — Platform vs product ownership (team topology)

- **Status:** Accepted
- **Date:** 2026-10-24
- **Serves:** FR-6, NFR-1

## Context

CIAM spans two concerns: the **identity platform capability** (IdP, identity API, SDK, auth
standards) and the **product domains** (benefits, payroll, HR) that consume it. Without an explicit
boundary, every squad re-implements auth → drift, inconsistent security, and no single owner for
the security-critical surface. The C4 model shows a "Product Squad Engineer" as a *consumer* but
never states who owns the identity surface.

## Decision

Split ownership into **platform** and **product**:

- **Platform — Identity/Auth squad (single central owner):** the IdP (Okta/Keycloak),
  `api-identity`, `sdk-identity-jvm` (+ Spring Boot starter), auth standards (ADRs, threat model,
  risk register), and secrets (ADR-008).
- **Product — domain squads (each owns its own):** `api-<domain>` / `ui-<domain>` /
  `mobile-<domain>` services and their domain data. They **consume** identity via the SDK/starter —
  they never re-own it.

**Rule:** the platform *builds safe defaults*; product squads *consume* them (FR-6 developer
advocacy). `api-identity` is a **platform** service, not a product service.

## Consequences

- **Pros:** one owner for the security-critical identity surface (NFR-1); squads integrate
  correctly by default; a clear escalation path for identity incidents.
- **Cons:** a platform team can become a bottleneck if it owns too much — mitigated by the
  SDK + starter, which make identity self-service.

## Alternatives considered

- **No boundary (every squad owns its own auth)** — rejected: drift + inconsistent security posture.
- **Central runtime auth service (validation as a call)** — rejected in ADR-007: adds a hop + SPOF;
  validation stays embedded/stateless via the library.
- **Full "you build it, you run it" with no platform** — rejected: identity is too security-critical
  (and cross-cutting) to distribute.
