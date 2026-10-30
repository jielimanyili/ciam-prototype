# Releasing — CIAM Prototype

> Release management: branching, versioning, environments, rollout/rollback. Complements `ADR-005` (CI/CD) and Epic 3.

## Branching strategy — trunk-based

- `main` is always deployable. No long-lived branches.
- Work happens on short-lived feature branches (`feat/…`, `fix/…`), merged via PR.
- A PR must pass CI (typecheck → tests → build) before merge (Epic 3).
- `main` is the single source of truth; releases are **tags on `main`**, not release branches.

## Versioning — SemVer

- `MAJOR.MINOR.PATCH` per component (`api-identity-jvm`, `ui-identity`, `sdk-identity-jvm`).
- `MAJOR` = breaking API change (the `openapi.yaml` contract); `MINOR` = new backward-compatible capability; `PATCH` = fix / security patch.
- A release = a git tag (`api-identity-jvm-v1.2.0`) + a matching image tag on GHCR.

## Environments

| Env | Purpose | Where | Config |
|---|---|---|---|
| **local** | development | docker-compose (Keycloak) + `ng serve` / `mvn` | `.env` (`OIDC_ISSUER`, `OIDC_AUDIENCE`) |
| **preview / staging** | PR review | Vercel preview + Render preview | platform secret store |
| **prod** | live demo | S3+CloudFront (`ui-identity`) · ECS EC2 (`api-identity-jvm`/`api-identity-python`/`api-auth`) · Expo | Auth0 tenant (ADR-021) |

- Env-specific config is **env vars only** (`OIDC_ISSUER`, `OIDC_AUDIENCE`, `PORT`) — no config in code.
- The IdP is per-env: Keycloak locally, Auth0 in prod (C-1, A-1).

## Rollout & rollback

- **Progressive**: merge → CI builds/tags → deploy job (Vercel + Render).
- **Rollback**: redeploy the previous image/tag (Render) or instant rollback (Vercel); mobile via Expo update.
- IdP/config changes must be **forward-compatible** (additive claims, no breaking scope renames) so a rollback never forces an IdP rollback.

## Release cadence

- Continuous — merge to `main` deploys (no fixed release train at prototype scale).
- A named release is cut only when a **tagged milestone** is worth it (Phase 2 / Phase 3 from `ROADMAP.md`).
