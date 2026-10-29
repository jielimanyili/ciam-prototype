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

- **Framework:** **Expo (managed)** — fastest to build; `expo start --web` gives a browser preview for the live demo (ADR-003).
- **Auth:** `expo-auth-session` (built-in PKCE) → Authorization Code + PKCE; issuer discovered from `/.well-known/openid-configuration`.
- **Redirect URI:** Expo Go dev = `exp://…/--/callback`; production = custom scheme / universal (App) link — both registered on the IdP client.
- **Token storage:** `expo-secure-store` (Keychain/Keystore) for the **refresh token**; access token in **memory** only (never persisted).
- **Token lifecycle:** access-token expiry → silent refresh via the stored refresh token; refresh failure → re-authenticate.
- **Screens:** SignIn → (auth session) → Home ("Call /api/me") → SignOut.
- **API target:** either resource server (`api-identity-jvm` or `api-identity-python`) — the mobile client's token carries both audiences (ADR-013).
- **SDK boundary:** the mobile client is TypeScript, so it uses `expo-auth-session`, **not** `sdk-identity-jvm`/`sdk-identity-python` — those SDKs cover server-side concerns (JWT validation, client-credentials).
- **Decision:** Expo over bare React Native for speed + demo portability (ADR-003).
- **Trade-off:** managed Expo limits custom native modules — irrelevant for this prototype.

## 3. SAML 2.0 (B2B SSO)

- **Where:** add a **SAML SP** to the Java resource server (Spring Security `saml2-service-provider`
  is first-class) — or to `api-auth` so a SAML assertion becomes a session the same way OIDC does.
- **Flows:**
  - **SP-initiated:** app → build `AuthnRequest` (+ `RelayState`) → redirect to IdP → login →
    `SAMLResponse` (assertion) → validate → issue session/token.
  - **IdP-initiated:** unsolicited `SAMLResponse` → validate `destination` + `audience` and, when
    `InResponseTo` is present, match an in-flight `AuthnRequest` — else reject.
- **Assertion validation checklist** (the SAML equivalent of the OIDC JWT checks): XML signature
  (XML DSig) · issuer · `AudienceRestriction` · destination · `NotBefore`/`NotOnOrAfter` ·
  `SubjectConfirmation` (bearer + `InResponseTo`/`Recipient`).
- **Identity mapping:** `NameID` + attributes map to the same `sub`/claims as OIDC, so the resource
  server's authorization layer stays protocol-agnostic.
- **Config:** add a SAML client to the Keycloak realm; wire the SP to IdP metadata (SSO + SLO URLs).
- **Decision:** Java/Spring Security SAML (mature) (ADR-004).
- **Trade-off:** a separate protocol + IdP config — scope to **SP-initiated first**, IdP-initiated
  second. SAML carries XML risks (XXE, signature wrapping) — use the framework's hardened parsers,
  never hand-rolled.

## 4. CI/CD — GitHub Actions (demo) · Harness (enterprise, ADR-018)

- **Pipeline (on push/PR), one workflow with a job matrix:**
  1. **typecheck + lint** — Angular `tsc` typecheck; Java + Python lint.
  2. **unit tests (per runtime)** — Java (`api-identity-jvm`, `sdk-identity-jvm`) on JDK 21 in Docker; Python (`api-identity-python`, `sdk-identity-python`) on 3.12.
  3. **parity gate (ADR-014)** — run both resource-server suites against the shared `test-fixtures/`; drift fails the build.
  4. **build + push** — `api-identity-jvm`, `api-identity-python`, `ui-identity` → **ECR** (demo) / **JFrog** (enterprise), version tag + `latest` on main.
  5. **publish SDKs** — `sdk-identity-jvm` → Maven Central/JFrog, `sdk-identity-python` → PyPI/JFrog (same semver).
  6. **deploy (main only)** — S3+CloudFront (`ui-identity`) + ECS Fargate (`api-identity-jvm`, `api-identity-python`) with host/IdP secrets.
- **Decision:** **GitHub Actions for the demo** (public, verifiable, zero-server — the CI/CD analog of
  "Keycloak stands in for Okta"); **Harness is the enterprise target** (TriNet is migrating Jenkins → Harness).
  Docker for Java (JDK 21 pin) · per-runtime test jobs.
- **Tool mapping (GitHub Actions → Harness):** workflow = Harness CI pipeline · `on: push/PR` =
  Git-event trigger · `jobs` = CI stages · deploy job = Harness CD pipeline (S3/ECS deploy with
  approval gates) · secrets = Harness secret manager.
- **Trade-off:** one workflow (job matrix) vs. many — one, simpler to reason about. Cache Maven/npm/pip; gate deploy + publish on a version tag.
- **Secrets:** `OIDC_ISSUER` / `OIDC_AUDIENCE` / host tokens in the tool's secret store — never committed (ADR-008).

## 5. Live demo deployment — four runtimes (AWS, ADR-016)

| Component | Host (demo) | Config |
|---|---|---|
| `ui-identity` (Angular SPA) | S3 + CloudFront | static build; env `authority` → Okta issuer |
| `mobile-identity` (Expo/RN) | Expo Go / EAS (web preview) | `expo-auth-session` PKCE against Okta |
| `api-identity-jvm` (Spring Boot) | ECS Fargate (image in ECR) | env `OIDC_ISSUER` · `OIDC_AUDIENCE=api-identity-jvm` |
| `api-identity-python` (FastAPI) | ECS Fargate (image in ECR) | env `OIDC_ISSUER` · `OIDC_AUDIENCE=api-identity-python` |
| `api-auth` (BFF) | ECS Fargate (image in ECR) | env `OIDC_ISSUER` · `CLIENT_SECRET` (Secrets Manager) · `RESOURCE_SERVER_URL` |
| `sdk-identity-jvm` | Maven Central (demo) · JFrog Maven (enterprise) | published artifact |
| `sdk-identity-python` | PyPI (demo) · JFrog PyPI (enterprise) | `pip install` |
| IdP | Okta free dev tenant | clients + audience config |

**Decisions:**
- **AWS** over Render/Vercel — production-real signal; matches TriNet's AWS/OCI (ADR-016).
- **ECS Fargate** over App Runner/EKS · **S3 + CloudFront** for the SPA · **Terraform** IaC ·
  **AWS Secrets Manager** (ADR-008).
- **Registry-agnostic CI/CD** — ECR/Central/PyPI for the public demo, **JFrog Artifactory** in
  TriNet's enterprise (ADR-017).
- **Both SDKs are published**, not vendored — the developer-advocacy story is "a squad adds a
  Maven dep / `pip install`s it," in either language.

**Audience in Okta (mirrors ADR-013):** Keycloak mints `aud` via audience mappers; Okta emits `aud`
from the authorization server's **Audience** setting. Point `OIDC_AUDIENCE` at the value Okta emits
(e.g. `api://default`) or configure the authorization server to emit `api-identity-jvm` /
`api-identity-python`. The SPA/mobile clients carry both audiences; the M2M client carries its
server's audience — same shape as the Keycloak realm.

**Secrets (ADR-019):** the demo deploys on AWS, so secrets use **AWS Secrets Manager** via ECS
injection, not Render env vars. Terraform defines the secret; the task definition injects it; the
app reads an env var (`CLIENT_SECRET`) and stays cloud-agnostic:

```hcl
resource "aws_secretsmanager_secret" "api_auth_client_secret" {
  name = "ciam/api-auth-client-secret"
}

resource "aws_secretsmanager_secret_version" "api_auth_client_secret" {
  secret_id     = aws_secretsmanager_secret.api_auth_client_secret.id
  secret_string = var.api_auth_client_secret   # supplied via env/var at apply time, never committed
}
```

```json
// ECS task definition — containerDefinitions[].secrets
"secrets": [
  { "name": "CLIENT_SECRET", "valueFrom": "<secret-arn>" }
]
```

The task **execution role** needs `secretsmanager:GetSecretValue` on that ARN (least privilege).
The only app change is `client-secret: ${CLIENT_SECRET:api-auth-secret}`.

---

## 6. Auth Service (`api-auth/`) — BFF pattern

- **Purpose:** the *hardened* auth variant — keep the access token **server-side** and issue a
  **session cookie** to the browser, so tokens never reach the SPA (ADR-006).
- **Client applicability (browser-only):** the Web App is the only client that routes through
  `api-auth` (cookie + server-side token). Mobile keeps **direct PKCE + secure storage** (native
  apps have Keychain/Keystore, no browser cookie jar — RFC 8252). The confidential service keeps
  **client-credentials** (no user, no session). The *all-client* edge is the **API Gateway**
  (rate limiting + JWT validation), not `api-auth`.
- **Tech:** Java Spring Boot + `spring-boot-starter-oauth2-client` + `oauth2Login()`.
- **Flow:** Web App → `api-auth` (login → session cookie) → `api-auth` proxies API calls to the
  Resource Server with the server-side access token.
- **Session security (NFR-9):** cookie `HttpOnly` + `Secure` + `SameSite=Lax` · session expiry +
  idle timeout · CSRF token on state-changing routes · session-fixation prevention (regenerate on
  login) · logout revokes the session *and* the IdP token (back-channel).
- **Server-side token store:** access token held in the session (or a short-lived cache); the
  refresh token rotates on use (reuse detection — a "Later" backlog item).
- **Proxy contract:** `/login`, `/logout`, `/me`, and `/api/**` passthrough with the bearer token —
  the SPA talks only to `api-auth`; it's the only component that ever holds tokens.
- **Decision:** an *alternative* to direct PKCE, not a replacement — direct PKCE stays the default
  because it demonstrates the OIDC/PKCE requirement directly (ADR-006).
- **Trade-off:** stateless JWT → stateful session (cookie expiry / revocation / scale — needs
  sticky sessions or a shared session store).
- **Ownership:** platform (ADR-010) — `api-auth` is the server-side half of authentication; keep it
  scoped to auth + session + proxy.

## 7. Multi-runtime parity (ADR-014)

> `api-identity-jvm` and `api-identity-python` expose the same API; they must stay identical.

**Single source of truth:** `docs/openapi.yaml`.

**Parity matrix** (the contract every suite asserts):

| Endpoint | Auth | Scope | 200 | 401 | 403 |
|---|---|---|---|---|---|
| `/health` | none | — | ✅ | | |
| `/api/me` | bearer | any | claims | no/invalid token | |
| `/api/admin` | bearer | `admin` | ✅ | no/invalid token | no `admin` scope |
| `/api/claims` | bearer | any | scope/roles/groups/client_id | no/invalid token | |

**JWT validation parity** (both reject): bad signature · wrong issuer · wrong/missing audience ·
expired token.

**Shared fixtures (`test-fixtures/`):**
- `parity-matrix.json` — the table above, machine-readable.
- `jwt/` — a test RSA keypair + pre-signed RS256 tokens for each valid/invalid case.
- Consumed by `api-identity-jvm` (JUnit) and `api-identity-python` (pytest) — same inputs, same
  expected outcomes.

**Enforcement:** a CI job runs both suites against the fixtures (Epic 3). A drift fails the build.

## 8. Migration & adoption — strangler-fig (ADR-015)

> A greenfield reference proves *how*; a migration plan proves *adoption across an enterprise*.
> TriNet already runs Okta, so the plan starts from the as-is estate and strangles it.

**As-is snapshot** (prerequisite):
- Inventory existing Okta clients (SPA / confidential / machine), grants, and per-app token validation.
- Publish an **old→new mapping** (audiences, scopes, roles/claims) so entitlements survive.

**Strangler phases:**

| Phase | What happens | Legacy auth | New auth |
|---|---|---|---|
| 0 · Parallel run | new authorization server + audiences live alongside legacy; nothing migrates yet | ✅ | ✅ (new services) |
| 1 · New-by-default | SDK/starter GA; greenfield services adopt the new pattern | ✅ | ✅ |
| 2 · Migrate one-by-one | each service moves via **auto-PR**; dual-validation flag accepts old + new tokens | ✅ (shrinking) | ✅ (growing) |
| 3 · Decommission | last service migrated → remove legacy audiences/validators | ❌ | ✅ |

**Adoption mechanics (product teams):**
- **Auto-PR codemod** — detects legacy auth wiring, rewrites it to the SDK/starter + new audience,
  opens a PR the owning squad reviews/merges (the developer-advocacy requirement, industrialized).
- **Guides + templates** — per client type (SPA PKCE / M2M client-credentials / BFF) for the manual path.

**Rollback:** the dual-validation flag makes rollback a config flip, not a redeploy.

**Metrics:** # services migrated · time-to-migrate per service · zero auth downtime · no scope/entitlement loss.

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
| 14 | Multi-runtime parity | shared `openapi.yaml` + shared fixtures + CI gate | drift-proof Java/Python resource servers |
| 15 | Migration | strangler-fig + auto-PR codemod + dual-validation | zero-downtime adoption across product teams |
