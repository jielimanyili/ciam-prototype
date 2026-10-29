# Design — CIAM Prototype (remaining components)

> Design-first spec for the components not yet built. This doc **proposes**;
> ADRs lock decisions **after** approval. Code is generated to this spec.

## Status

**Already built:** `api-identity-jvm` (Spring Boot), `sdk-identity-jvm` (Java SDK), `ui-identity` (Angular), Keycloak realm, and docs (requirements, architecture, C4, best-practices, anti-patterns, threat-model, risk register).

**Remaining (this design):** JVM SDK · React Native client · SAML 2.0 · CI/CD · live deployment.

---

## Naming conventions

> Standardizes component naming, mirroring TriNet's `api-<domain>` / `ui-<domain>` style.
> **Locked in [ADR-009](adr/adr-009-component-naming.md).**

**Rule:** `<layer>-<domain>` in kebab-case — the **layer** prefix says *what kind* of component,
the **domain** says *which business capability* it serves (**identity** = data/claims, **auth** = authentication).

| Layer | Prefix | Example |
|---|---|---|
| Resource server / REST API | `api-` | `api-identity-jvm`, `api-auth` |
| Web UI (SPA) | `ui-` | `ui-identity` |
| Mobile client | `mobile-` | `mobile-identity` |
| Backend-for-frontend (domain BFF) | `bff-` | `bff-<domain>` |
| Shared library / SDK | `sdk-` | `sdk-identity-jvm` |

TriNet's real form is **`<layer>-<org>-<domain>`** — e.g. `api-trinet-auth` (layer + org + domain).
The `-trinet-` org qualifier is optional; this repo uses the unqualified `<layer>-<domain>` form
(single org). `auth` and `identity` are **distinct domains** — `auth` = authentication, `identity` = data/claims.

**Principles**

- Name by **domain capability** (bounded context), never by technology (`spring-`, `angular-` are out).
- Repo name = deployable/service name (1:1) so a service catalog (e.g., Backstage) can discover it.
- One convention, enforced by template/ADR — consistency beats any particular prefix.
- **SDK exception:** SDKs use `<layer>-<domain>-<runtime>` (`sdk-identity-jvm`) — the runtime is part of an SDK's identity; `-jvm` covers Java + the planned Kotlin DSL wrapper (ADR-002).

**Components** (renamed, ADR-009):

| Layer | Directory |
|---|---|
| Resource server / identity API | `api-identity-jvm/` |
| Resource server / identity API (Python) | `api-identity-python/` |
| Auth service (login/session) | `api-auth/` (planned) |
| Web UI (SPA) | `ui-identity/` |
| Mobile client (planned) | `mobile-identity/` |
| Shared library / SDK | `sdk-identity-jvm/` |
| Shared library / SDK (Python) | `sdk-identity-python/` |

> Renaming **applied**: `resource-server/` → `api-identity-jvm/`, `frontend/` → `ui-identity/`,
> `identity-sdk-jvm/` → `sdk-identity-jvm/` (dirs, artifactIds, Angular project name, Docker image
> name, Keycloak client IDs, README). Planned `mobile-identity/` and `api-auth/` land with their
> phases.

---

## 1. JVM SDK — `sdk-identity-jvm/`

- **Purpose:** reusable library so product squads integrate identity safely (the "developer advocacy" requirement).
- **Language:** **Java (primary)** with Kotlin interop (Java classes are Kotlin-callable); optional Kotlin DSL wrapper later.
- **Scope — a thin wrapper, not a reimplementation.** Spring Security already validates JWTs; the SDK standardizes everything *around* it so squads get safe defaults without re-deriving them:
  - `OidcDiscovery` — issuer → authorization / token / jwks endpoints
  - `Pkce` — S256 code_verifier / code_challenge
  - `ClientCredentials` — token fetch
  - `ScopeAssertions` — `requireScope`, `scopesOf`, `hasClaim`
- **Interface sketch** (canonical Java API):
  ```java
  PkcePair pkce = Pkce.generate();                                       // FR-1
  OidcMetadata meta = OidcDiscovery.discover(issuer);                    // discovery
  TokenResponse tokens = ClientCredentials.fetch(tokenEndpoint, id, secret, "admin"); // FR-2
  ScopeAssertions.requireScope(jwt.getClaims(), "admin");                // FR-5
  ```
- **Decision:** Java is the canonical backend language (C-2); this SDK is the single reusable identity library.
- **Trade-off:** Spring Security already does JWT validation → the SDK avoids duplicating it, providing the helpers + opinionated defaults instead.
- **Extension (planned):** a Spring Boot starter (`sdk-identity-jvm-spring-boot-starter`) that auto-configures the Resource Server's validation — `issuer-uri` → JWKS/RS256, audience validator, scope enforcement — so each RS is secure by default (ADR-007).

## 1b. Python SDK + Python resource server — multi-runtime (ADR-011)

> The identity platform is language-agnostic; TriNet runs Python, so the reference ships a
> Python counterpart of the SDK and the resource server.

- **SDK (`sdk-identity-python/`):** Python 3.12, thin wrapper mirroring `sdk-identity-jvm` —
  `discover`, `generate_pkce`, `fetch_client_credentials`, `scopes_of`/`require_scope`/`has_claim`.
  Dependency: `httpx` only. JWT validation stays in the resource server.
- **Resource server (`api-identity-python/`):** FastAPI, mirrors `api-identity-jvm` — same endpoints
  (`/health`, `/api/me`, `/api/admin`, `/api/claims`), same JWT validation (RS256/JWKS + issuer +
  audience + expiry), scope enforcement via the SDK. Consumes `sdk-identity-python` for discovery.
- **Decision:** Python 3.12 + FastAPI + PyJWT (validation) + httpx; `-python` marks the alternate
  runtime of an existing component (ADR-009 rule 6 extension).
- **Trade-off:** two runtimes to keep in parity (endpoints + validation semantics).

## 2. React Native client — `mobile-identity/`

- **Framework:** **Expo (managed)** — fastest to build; `expo start --web` gives a browser preview for the live demo.
- **Auth:** `expo-auth-session` (built-in PKCE) → Authorization Code + PKCE.
- **Token storage:** `expo-secure-store` (Keychain/Keystore) for the refresh token; access token in memory.
- **Screens:** SignIn → (auth session) → Home ("Call /api/me") → SignOut.
- **Decision:** Expo over bare React Native for speed + demo portability.
- **Trade-off:** managed Expo limits custom native modules — irrelevant for this prototype.

## 3. SAML 2.0 (B2B SSO)

- **Where:** add a **SAML SP to the Java resource server** (Spring Security `saml2-service-provider` is first-class).
- **Flows:** SP-initiated (redirect → assert → validate → issue session) and IdP-initiated (validate audience / destination / InResponseTo on unsolicited assertions).
- **Config:** add a SAML client to the Keycloak realm; wire the SP to the IdP metadata.
- **Decision:** Java/Spring Security SAML (mature).
- **Trade-off:** SAML is a separate protocol + IdP config — scope it to SP-initiated first, IdP-initiated second.

## 4. CI/CD — GitHub Actions

- **Pipeline (on push/PR):** (1) typecheck/lint → (2) unit tests (Java via Docker) → (3) build images (`api-identity-jvm`, `ui-identity`) → (4) push to GHCR → (5) deploy.
- **Decision:** GitHub Actions (already used in the repo) · Docker for Java (JDK pinning) · platform deploy for the frontend.
- **Trade-off:** one workflow with a job matrix vs. many workflows — one workflow, simpler to reason about.

## 5. Live demo deployment — four runtimes

| Component | Host | Config |
|---|---|---|
| `ui-identity` (Angular SPA) | Vercel | static build; env `authority` → Okta issuer |
| `mobile-identity` (Expo/RN) | Expo Go / EAS (web preview) | `expo-auth-session` PKCE against Okta |
| `api-identity-jvm` (Spring Boot) | Render (Dockerfile) | env `OIDC_ISSUER` · `OIDC_AUDIENCE=api-identity-jvm` |
| `api-identity-python` (FastAPI) | Render (Dockerfile) | env `OIDC_ISSUER` · `OIDC_AUDIENCE=api-identity-python` |
| `sdk-identity-jvm` | Maven Central (or GitHub Packages) | published artifact |
| `sdk-identity-python` | PyPI (or GitHub Packages) | `pip install` |
| IdP | Okta free dev tenant | clients + audience config (below) |

**Decisions:**
- **Okta dev tenant** over hosted Keycloak — Okta is the reference IdP (ADR-005).
- **Render** over Railway/Fly — simplest Dockerfile deploy; both resource servers deploy identically.
- **Both SDKs are published**, not vendored — the developer-advocacy story is "a squad adds a
  Maven dep / `pip install`s it," in either language.

**Audience in Okta (mirrors ADR-013):** Keycloak mints `aud` via audience mappers; Okta emits `aud`
from the authorization server's **Audience** setting. Point `OIDC_AUDIENCE` at the value Okta emits
(e.g. `api://default`) or configure the authorization server to emit `api-identity-jvm` /
`api-identity-python`. The SPA/mobile clients carry both audiences; the M2M client carries its
server's audience — same shape as the Keycloak realm.

---

## 6. Auth Service (`api-auth/`) — BFF pattern (planned)

- **Purpose:** the *hardened* auth variant — keep the access token **server-side** and
  issue a **session cookie** to the browser, so tokens never reach the SPA.
- **Tech:** Java Spring Boot + `spring-boot-starter-oauth2-client` + `oauth2Login()`.
- **Flow:** Web App → `api-auth` (login → session cookie) → `api-auth` proxies API calls to the
  Resource Server with the server-side access token.
- **Decision:** offer it as an *alternative* to direct PKCE, not a replacement — direct
  PKCE stays the default because it demonstrates the OIDC/PKCE requirement directly.
- **Trade-off:** stateless JWT → stateful session (cookie expiry / revocation / scale).
- **Ownership:** platform (ADR-010) — `api-auth` is the server-side half of authentication;
  keep it scoped to auth + session + proxy.

## Key decisions (summary)

| # | Decision | Choice | Rationale |
|---|---|---|---|
| 1 | JVM SDK language | Java (Kotlin-interop) | reference stack targets Java; Kotlin-callable |
| 2 | Mobile framework | Expo (managed) | speed + web preview |
| 3 | Mobile auth | expo-auth-session | built-in PKCE |
| 4 | SAML implementation | Spring Security SAML (Java) | first-class SP support |
| 5 | CI/CD | GitHub Actions | already in repo |
| 6 | Backend host | Render | free + Dockerfile |
| 7 | Frontend host | Vercel | already used |
| 8 | Live IdP | Okta dev tenant | Okta is the reference IdP |
| 9 | Auth service | `api-auth` — hardened variant (`oauth2Login()`) | tokens stay server-side |
| 10 | Component naming | `<layer>-<domain>` (api-/ui-/mobile-/bff-/sdk-); domains: identity, auth | mirrors TriNet convention; catalog-discoverable |
| 11 | Python SDK | `sdk-identity-python` (thin, httpx-only) | mirrors `sdk-identity-jvm`; FR-6 across languages |
| 12 | Python resource server | `api-identity-python` (FastAPI + PyJWT) | same endpoints/validation as `api-identity-jvm` |
| 13 | Token audience | Keycloak audience mappers (ADR-013) | strict `aud` stays; audiences minted in the IdP |
