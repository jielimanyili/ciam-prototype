# ADR-009 — Component naming convention (`<layer>-<domain>`)

- **Status:** Accepted
- **Date:** 2026-10-24
- **Serves:** C-2, C-3, FR-6

## Context

Components were named by *role* (`resource-server/`, `frontend/`, `identity-sdk-jvm/`), which
says nothing about the business capability they serve and doesn't scale to a service catalog.
TriNet's convention is `<layer>-<domain>` — e.g. `api-trinet-auth` — where the **layer** says
*what kind* of component and the **domain** says *which capability*.

## Decision

Name every component **`<layer>-<domain>`** (kebab-case), with the domain fixed at
**`identity`** — the CIAM bounded context (not `ciam`, which is the category):

- `api-identity` (resource server) · `ui-identity` (SPA) · `mobile-identity` (React Native) ·
  `bff-identity` (BFF) · `sdk-identity-jvm` (SDK)

Rules:

1. **Layer prefix** = component type (`api-`, `ui-`, `mobile-`, `bff-`, `sdk-`).
2. **Domain suffix** = business capability (`identity`); never technology (`spring-`, `angular-`) — one exception: SDKs may append a `-<runtime>` suffix (rule 6).
3. **Repo name = deployable/service name** (1:1) for service-catalog discovery.
4. **Org namespace optional** — TriNet's `api-trinet-auth` is `<layer>-<org>-<domain>`; apply the
   `-trinet-` qualifier only in a multi-tenant catalog. This repo uses the unqualified form.
5. `auth` and `identity` name the same domain at TriNet; we standardize on **`identity`** (broader:
   covers authN *and* authZ / identity lifecycle).
6. **SDK runtime suffix** — SDKs use `<layer>-<domain>-<runtime>`: `sdk-identity-jvm`. The runtime
   is part of an SDK's identity (precedent: `aws-sdk-java`, `stripe-java`); `-jvm` (not `-java`)
   covers Java *and* the planned Kotlin DSL wrapper (ADR-002).

## Consequences

- **Pros:** discoverable in a catalog; capability + type visible at a glance; matches TriNet.
- **Cons:** one-time mechanical rename of dirs, artifactIds, the Angular project name, Docker image
  names, Keycloak client IDs, and the README (tracked in DESIGN.md §Naming conventions).

## Alternatives considered

- **Role-only names** (`resource-server/`, `frontend/`) — rejected: no domain signal, doesn't scale.
- **`ciam` as the domain** — rejected: `ciam` is the category, not the bounded context.
- **`auth` as the domain** — rejected for this repo: `identity` is broader and matches the CIAM scope.
- **No convention** — rejected: naming drift makes the catalog un-ownable.
