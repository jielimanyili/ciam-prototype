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

- **Platform — Identity/Auth squad (single central owner):** the identity capability end-to-end —
  the artifacts below, plus auth standards (ADRs, threat model, risk register) and secrets (ADR-008).
- **Product — domain squads (each owns its own):** `api-<domain>` / `ui-<domain>` /
  `mobile-<domain>` services (plus any domain BFF, `bff-<domain>`) and their domain data. They
  **consume** identity via the SDK/starter — they never re-own it.

**Platform artifacts** (responsibility + state):

| Artifact | Provides | State |
|---|---|---|
| IdP (Okta/Keycloak) | identity store, credential verification, token issuance, MFA/consent | stateful |
| `api-auth` | the AuthN flow server-side — OIDC handshake → session cookie, token server-side | stateful (session) |
| `api-identity-jvm` | reference resource server (prototype) · identity data + IdP admin ops (target) | stateless (prototype) → stateful data slice (target) |
| `sdk-identity-jvm` (+ starter) | safe defaults at build time for product services | — (compile-time) |

> **Library vs. service boundary:** validation + claims = **library** (`sdk-identity-jvm` + starter;
> embedded, stateless, build-time — ADR-007). Shared identity **data** + privileged IdP **operations** =
> **service** (`api-identity-jvm`; stateful, platform-owned, server-side secrets). A library holds no
> shared state and no privileged secrets.
>
> `api-identity-jvm` is a thin **reference resource server** in the prototype (stateless validation); in
> the target it takes on the stateful **identity data** slice (profile FR-14, consent, MFA) + IdP
> admin ops.

**Rules:**

- The **`identity` and `auth` domains are the platform's domains**; every other `-<domain>` is product-owned
  (ties ADR-009 naming to ownership).
- The platform *builds safe defaults*; product squads *consume* them (FR-6 developer advocacy).
- **Authentication = platform** (who you are + session + token issuance + the verification
  library); **authorization = product** (what you can do in *their* domain), enforced via the
  platform SDK — the `verify-then-authorize` split.

## Consequences

- **Pros:** one owner for the security-critical identity surface (NFR-1); squads integrate
  correctly by default; a clear escalation path for identity incidents.
- **Cons:** a platform team can become a bottleneck if it owns too much — mitigated by the
  SDK + starter (self-service) and by scoping `api-auth` to auth + session + proxy (no domain
  logic).

## Alternatives considered

- **No boundary (every squad owns its own auth)** — rejected: drift + inconsistent security posture.
- **Central runtime auth service (validation as a call)** — rejected in ADR-007: adds a hop + SPOF;
  validation stays embedded/stateless via the library.
- **Full "you build it, you run it" with no platform** — rejected: identity is too security-critical
  (and cross-cutting) to distribute.
