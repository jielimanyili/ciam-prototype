# ADR-009 — Component naming convention (`<layer>-<domain>`)

- **Status:** Accepted
- **Date:** 2026-10-24
- **Serves:** C-2, C-3, FR-6

## Context

Components were named by *role* (`api-identity-jvm/`, `ui-identity/`, `sdk-identity-jvm/`), which
says nothing about the business capability they serve and doesn't scale to a service catalog.
TriNet's convention is `<layer>-<domain>` — e.g. `api-trinet-auth` — where the **layer** says
*what kind* of component and the **domain** says *which capability*.

## Decision

Name every component **`<layer>-<domain>`** (kebab-case). The platform owns **two domains**:

- **`identity`** — identity data + claims (the CIAM bounded context; not `ciam`, the category)
- **`auth`** — authentication (login, session, token)

- `api-identity-jvm` (resource server) · `ui-identity` (SPA) · `mobile-identity` (React Native) ·
  `api-auth` (auth service) · `sdk-identity-jvm` (SDK)

Rules:

1. **Layer prefix** = component type (`api-`, `ui-`, `mobile-`, `bff-`, `sdk-`).
2. **Domain suffix** = business capability (`identity`, `auth`); never technology (`spring-`, `angular-`) — one exception: SDKs may append a `-<runtime>` suffix (rule 6).
3. **Repo name = deployable/service name** (1:1) for service-catalog discovery.
4. **Org namespace optional** — TriNet's `api-trinet-auth` is `<layer>-<org>-<domain>`; apply the
   `-trinet-` qualifier only in a multi-tenant catalog. This repo uses the unqualified form.
5. `auth` and `identity` are **distinct domains**: `auth` = authentication (login/session/token),
   `identity` = identity data + claims. TriNet's `api-trinet-auth` is the `auth` domain.
6. **SDK runtime suffix** — SDKs use `<layer>-<domain>-<runtime>`: `sdk-identity-jvm`. The runtime
   is part of an SDK's identity (precedent: `aws-sdk-java`, `stripe-java`); `-jvm` (not `-java`)
   covers Java *and* the planned Kotlin DSL wrapper (ADR-002).

## Consequences

- **Pros:** discoverable in a catalog; capability + type visible at a glance; matches TriNet.
- **Cons:** one-time mechanical rename of dirs, artifactIds, the Angular project name, Docker image
  names, Keycloak client IDs, and the README (tracked in DESIGN.md §Naming conventions).

## Alternatives considered

- **Role-only names** (`api-identity-jvm/`, `ui-identity/`) — rejected: no domain signal, doesn't scale.
- **`ciam` as the domain** — rejected: `ciam` is the category, not the bounded context.
- **A single `identity` domain (merging `auth`)** — rejected: conflates authentication with identity data; `auth` is its own concern (matches TriNet's `api-trinet-auth`).
- **No convention** — rejected: naming drift makes the catalog un-ownable.
