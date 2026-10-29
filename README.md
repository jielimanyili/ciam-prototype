# CIAM Prototype

A working **customer identity & access management (CIAM)** reference implementation —
covering a Principal-level CIAM stack:
OIDC/OAuth 2.0 flows, JWT validation, scope/claim-based authorization, and a
reusable internal SDK — runnable end-to-end with a local Keycloak IdP (stand-in
for Okta).

## What it demonstrates (stack → code)

| Capability | Where it lives |
|---|---|
| CIAM architecture (Okta APIs/SDKs) | Keycloak stands in for Okta; swap issuer + client to go live (see below) |
| OIDC / OAuth 2.0 — Authorization Code + PKCE | `ui-identity/` (Angular + `oidc-client-ts`) + `api-identity-jvm/` (Spring Security OAuth2) |
| OAuth 2.0 — Client Credentials | `api-identity-jvm/` (Spring Security) · `sdk-identity-jvm` (built) |
| API & microservice security — JWT validation, scoping, claims | `api-identity-jvm/` (Spring Security) + `api-identity-python/` (FastAPI) |
| Developer advocacy — reusable libraries / SDK wrappers + docs | `sdk-identity-jvm` + `sdk-identity-python` (built) + this README + `docs/` |
| JavaScript (UI) frameworks + microservices + REST | Angular 22 (SPA) + Java/Spring Boot microservice |
| Security by design | RS256/JWKS + issuer/audience validation + scope enforcement, tested |

**SAML 2.0** (B2B SSO) — currently **planned in Phase 2** (see
[`docs/DESIGN.md`](docs/DESIGN.md) and [`docs/ROADMAP.md`](docs/ROADMAP.md)); the
prototype so far focuses on the OIDC/OAuth2 path. The `verify-then-authorize`
middleware shape in `api-identity-jvm/` (Spring Security filter chain) is the same pattern you'd use to
validate a SAML assertion before minting a session/token.

## Documentation

| Doc | Answers |
|---|---|
| [`docs/REQUIREMENTS.md`](docs/REQUIREMENTS.md) | What + constraints (step 1) |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Structure + data model |
| [`docs/DESIGN.md`](docs/DESIGN.md) | Component design + decisions (remaining pieces) |
| [`docs/openapi.yaml`](docs/openapi.yaml) | API contract (OpenAPI 3.0) |
| [`docs/adr/`](docs/adr/) | Locked decisions (immutable) |
| [`docs/C4.md`](docs/C4.md) | C4 diagrams (Context / Container / Component) |
| [`docs/best-practices.md`](docs/best-practices.md) · [`docs/anti-patterns.md`](docs/anti-patterns.md) · [`docs/threat-model.md`](docs/threat-model.md) | Guidance |
| [`docs/risk-register.md`](docs/risk-register.md) | Risks + mitigations |
| [`docs/ROADMAP.md`](docs/ROADMAP.md) | Now / Next / Later |
| [`docs/epics.md`](docs/epics.md) | Epics + stories (JIRA backlog · import `docs/jira-import.csv`) |
| [`docs/planning-checklist.md`](docs/planning-checklist.md) | Planning completeness (status + gaps) |
| [`docs/TESTING.md`](docs/TESTING.md) | Test strategy (pyramid, offline testing, gates) |
| [`docs/RELEASING.md`](docs/RELEASING.md) | Release management (branching, semver, environments) |
| [`docs/RUNBOOK.md`](docs/RUNBOOK.md) | Operations (run, troubleshoot, secrets, incident response) |

## Repo layout

```
api-identity-jvm/       Spring Boot (Java 21) resource server — protected API (Spring Security OAuth2)
api-identity-python/  FastAPI resource server — same endpoints + JWT validation as api-identity-jvm (uses sdk-identity-python)
ui-identity/        Angular 22 SPA — Authorization Code + PKCE login
sdk-identity-jvm/   Reusable Java identity SDK — OIDC discovery, PKCE (S256), client-credentials, scope assertions
sdk-identity-jvm-spring-boot-starter/  Spring Boot starter — auto-configures the resource server (JWKS/RS256 + issuer + audience)
sdk-identity-python/  Reusable Python identity SDK — mirrors sdk-identity-jvm
keycloak/           Local OIDC provider realm (stand-in for Okta)
docker-compose.yml  Keycloak (the IdP)
docs/               full doc set — see the Documentation section
```

## Quickstart

```bash
# 1. Start the local IdP (Keycloak)
docker compose up -d
# wait ~30s, then verify:
curl http://localhost:8080/realms/ciam-demo/.well-known/openid-configuration

# 2. Resource server (protected API) — port 4101 (needs JDK 21, or use Docker)
cd api-identity-jvm && docker build -t api-identity-jvm . && docker run -p 4101:4101 api-identity-jvm

# 3. Web client (Authorization Code + PKCE) — port 4200
cd ../ui-identity && npm install && npm start
# open http://localhost:4200 -> "Sign in with Keycloak" -> demo / demo
```

Flow summary: the SPA logs in with **Authorization Code + PKCE**, calls
`/api/me` with the access token; the resource server validates the RS256 JWT
against the IdP's JWKS, checks issuer + audience, then enforces scopes/claims.

## Shareable links

| What | URL |
|---|---|
| Repo (public) | https://github.com/jielimanyili/ciam-prototype |

- **Runs locally with no external accounts** — Keycloak stands in for Okta (see Quickstart above).
- **Live demo — planned (Phase 3, not yet deployed):** see [ADR-005](docs/adr/adr-005-cicd-deployment.md) · [DESIGN §5](docs/DESIGN.md) · [ROADMAP](docs/ROADMAP.md).

| Component | Host | Config |
|---|---|---|
| `ui-identity` (Angular SPA) | Vercel | static build (`npm run build`); env `authority` → Okta domain |
| `api-identity-jvm` (Spring Boot) | Render | Dockerfile already present; env `OIDC_ISSUER` / `OIDC_AUDIENCE=api-identity-jvm` |
| `api-identity-python` (FastAPI) | Render | Dockerfile already present; env `OIDC_ISSUER` / `OIDC_AUDIENCE=api-identity-python` |
| `mobile-identity` (planned) | Expo | `expo start --web` |
| `sdk-identity-jvm` / `sdk-identity-python` | Maven Central / PyPI | published libraries |
| IdP | Okta dev tenant | swap issuer + client + audience (below) |

> No `vercel.json` / `render.yaml` yet — the deploy is Epic 3 (CI/CD) + Epic 4 (live demo).

## Swap Keycloak → Okta

The SDK and resource server are provider-agnostic (standard OIDC discovery).
Only the issuer/client change:

1. Point the resource server at Okta: `OIDC_ISSUER=https://<your-okta-domain>/oauth2/default`
   and `OIDC_AUDIENCE=<your-api-audience>` (JWKS is auto-discovered). Use `api-identity-jvm` /
   `api-identity-python` and configure the authorization server's Audience to match (ADR-013).
2. In `ui-identity/src/app/auth.service.ts`, set `authority` to your Okta domain and
   `client_id` to your Okta SPA app.

## Tests

```bash
cd api-identity-jvm && docker build .   # runs unit/integration tests (JDK 21)
```

## Honesty boundary

- This is a **project-level prototype**, not production. No production CIAM is claimed.
- Keycloak is a **local stand-in** for Okta; the realm JSON is a starting point
  (see `keycloak/README.md` for the guaranteed manual setup).
- Backend is **Java (Spring Boot)**; the Angular frontend is TypeScript.
