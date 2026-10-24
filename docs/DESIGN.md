# Design — CIAM Prototype (remaining components)

> Design-first spec for the components not yet built. This doc **proposes**;
> ADRs lock decisions **after** approval. Code is generated to this spec.

## Status

**Already built:** `api-identity` (Spring Boot), `sdk-identity-jvm` (Java SDK), `ui-identity` (Angular), Keycloak realm, and docs (requirements, architecture, C4, best-practices, anti-patterns, threat-model, risk register).

**Remaining (this design):** JVM SDK · React Native client · SAML 2.0 · CI/CD · live deployment.

---

## Naming conventions

> Standardizes component naming, mirroring TriNet's `api-<domain>` / `ui-<domain>` style.
> **Locked in [ADR-009](adr/adr-009-component-naming.md).**

**Rule:** `<layer>-<domain>` in kebab-case — the **layer** prefix says *what kind* of component,
the **domain** says *which business capability* it serves (here, **identity** = the CIAM domain).

| Layer | Prefix | Example |
|---|---|---|
| Resource server / REST API | `api-` | `api-identity` |
| Web UI (SPA) | `ui-` | `ui-identity` |
| Mobile client | `mobile-` | `mobile-identity` |
| Backend-for-frontend | `bff-` | `bff-identity` |
| Shared library / SDK | `sdk-` | `sdk-identity-jvm` |

TriNet's real form is **`<layer>-<org>-<domain>`** — e.g. `api-trinet-auth` (layer + org + domain).
The `-trinet-` org qualifier is optional; this repo uses the unqualified `<layer>-<domain>` form
(single org). TriNet's `auth` domain ≈ `identity`; we standardize on **`identity`**.

**Principles**

- Name by **domain capability** (bounded context), never by technology (`spring-`, `angular-` are out).
- Repo name = deployable/service name (1:1) so a service catalog (e.g., Backstage) can discover it.
- One convention, enforced by template/ADR — consistency beats any particular prefix.
- **SDK exception:** SDKs use `<layer>-<domain>-<runtime>` (`sdk-identity-jvm`) — the runtime is part of an SDK's identity; `-jvm` covers Java + the planned Kotlin DSL wrapper (ADR-002).

**Components** (renamed, ADR-009):

| Layer | Directory |
|---|---|
| Resource server / REST API | `api-identity/` |
| Web UI (SPA) | `ui-identity/` |
| Mobile client (planned) | `mobile-identity/` |
| Backend-for-frontend (planned) | `bff-identity/` |
| Shared library / SDK | `sdk-identity-jvm/` |

> Renaming **applied**: `resource-server/` → `api-identity/`, `frontend/` → `ui-identity/`,
> `identity-sdk-jvm/` → `sdk-identity-jvm/` (dirs, artifactIds, Angular project name, Docker image
> name, Keycloak client IDs, README). Planned `mobile-identity/` and `bff-identity/` land with their
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

- **Pipeline (on push/PR):** (1) typecheck/lint → (2) unit tests (Java via Docker) → (3) build images (`api-identity`, `ui-identity`) → (4) push to GHCR → (5) deploy.
- **Decision:** GitHub Actions (already used in the repo) · Docker for Java (JDK pinning) · platform deploy for the frontend.
- **Trade-off:** one workflow with a job matrix vs. many workflows — one workflow, simpler to reason about.

## 5. Live demo deployment

- **Frontend (Angular):** **Vercel** (already used for other projects) — static, env-var `authority`.
- **Backend:** **Render** (free tier, Dockerfile) — run the Java resource server; env `OIDC_ISSUER` / `OIDC_AUDIENCE`.
- **Mobile:** Expo (web preview) or Expo Go.
- **IdP (live):** **Okta free dev tenant** (Okta is the reference IdP) — you sign up; swap issuer + client config (already supported).
- **Decision:** Okta dev tenant over hosted Keycloak (Okta is the reference IdP); Render over Railway/Fly (simplest Dockerfile deploy).

---

## 6. BFF (Backend-for-Frontend) — `bff-identity/` (planned)

- **Purpose:** the *hardened* auth variant — keep the access token **server-side** and
  issue a **session cookie** to the browser, so tokens never reach the SPA.
- **Tech:** Java Spring Boot + `spring-boot-starter-oauth2-client` + `oauth2Login()`.
- **Flow:** Web App → BFF (login → session cookie) → BFF proxies API calls to the
  Resource Server with the server-side access token.
- **Decision:** offer it as an *alternative* to direct PKCE, not a replacement — direct
  PKCE stays the default because it demonstrates the OIDC/PKCE requirement directly.
- **Trade-off:** stateless JWT → stateful session (cookie expiry / revocation / scale).
- **Ownership:** platform (ADR-010) — `bff-identity` is the server-side half of authentication;
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
| 9 | BFF | Add as planned hardened variant (`oauth2Login()`) | tokens stay server-side |
| 10 | Component naming | `<layer>-<domain>` (api-/ui-/mobile-/bff-/sdk-) | mirrors TriNet convention; catalog-discoverable |
