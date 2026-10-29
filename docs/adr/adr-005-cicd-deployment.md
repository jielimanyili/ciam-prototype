# ADR-005 — CI/CD and live-demo deployment

- **Status:** Accepted
- **Date:** 2026-10-22
- **Serves:** NFR-4, NFR-6 (live demo)

## Context

The prototype needs automated build/test/containerize and a live demo across the
different clients.

## Decision

- **CI/CD:** GitHub Actions — typecheck/lint → tests (Java via Docker) → build
  images (`api-identity-jvm`, `ui-identity`) → push to GHCR → deploy.
- **Backend host:** Render (free tier, Dockerfile).
- **Frontend host:** Vercel (static).
- **Mobile:** Expo (web preview / Expo Go).
- **Live IdP:** Okta free dev tenant (the reference IdP).

## Consequences

- **Pros:** free tiers; the Dockerfile already exists; Okta matches the reference IdP.
- **Cons:** requires the author's accounts (Okta / Render / Vercel) to go live.

## Alternatives considered

- **Railway / Fly** — rejected: Render is the simplest Dockerfile deploy.
- **Hosted Keycloak** — rejected: Okta is the reference IdP.
