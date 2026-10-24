# Epics & Stories — CIAM Prototype

> Work breakdown for the CIAM roadmap ([`ROADMAP.md`](ROADMAP.md)). Each roadmap item is an **epic**; each epic splits into **stories** (vertical, independently shippable slices). This file mirrors the JIRA backlog — import [`jira-import.csv`](jira-import.csv) into JIRA to recreate it.

## How this maps to JIRA

| This doc | JIRA field |
|---|---|
| Epic heading | Issue Type = **Epic** · Epic Name · Summary |
| **Why / Scope / Success criteria** | Description + Acceptance Criteria (epic level) |
| Story **"As a … I want … so that …"** | Description |
| **Given / When / Then** | Acceptance Criteria (custom field) |
| **DoD** | Definition of Done |
| **Links** | Issue Links → ADR / design doc |
| `[component]` summary prefix | Component/s (ADR-009) |

## Conventions

- **Summary prefix** = owning component (ADR-009): `[api-identity]` `[api-auth]` `[ui-identity]` `[mobile-identity]` `[sdk-identity-jvm]` `[ci/cd]`.
- **Squad:** the `identity` + `auth` domains are platform-owned (ADR-010); everything else → the owning domain squad.
- **A story is a vertical slice** (testable end-to-end), never a layer ("build the UI", "build the API").

---

## Epic 0 — JVM SDK (`sdk-identity-jvm`) · ✅ Done

- **Links:** ADR-002 · FR-6 · DESIGN §1
- Delivered: `Pkce`, `OidcDiscovery`, `ClientCredentials`, `ScopeAssertions` + 6 tests. Closed; listed for completeness.

---

## Epic 1 — Mobile customer sign-in (`mobile-identity`)

- **Status:** To Do · **Priority:** High · **Squad:** Identity/Auth platform · **Links:** ADR-003, ADR-001, DESIGN §2, C-3, FR-1
- **Why:** customers authenticate on mobile via the same OIDC/PKCE flow as the web app.
- **Scope:** Expo (managed) + `expo-auth-session` (PKCE) + `expo-secure-store`; calls `api-identity`.
- **Out of scope:** SAML, passkeys/biometrics (Later).
- **Success criteria:** sign-in → claims · refresh token in secure store · sign-out clears session · works against Keycloak (and later Okta).

**Stories**

1. `[mobile-identity]` Complete Authorization Code + PKCE login
   - **As a** customer **I want** to sign in with my CIAM account **so that** I can reach my data.
   - **AC:** Given unauthenticated → When I tap "Sign in" → Then `expo-auth-session` runs PKCE and I land on Home with a valid access token.
   - **AC (negative):** Given a cancelled session → Then I return to Sign In without an error.
   - **DoD:** access token in memory only · unit-tested offline (no IdP).

2. `[mobile-identity]` Store tokens securely
   - **As a** customer **I want** my session to survive restart **so that** I'm not signed out unexpectedly.
   - **AC:** Given a login → Then the refresh token is in `expo-secure-store` and the access token stays in memory.
   - **AC:** Given an app relaunch → Then the session restores from the refresh token.
   - **DoD:** no token in AsyncStorage / plain files.

3. `[mobile-identity]` Call the protected API
   - **As a** customer **I want** to see my claims **so that** I know the flow works end-to-end.
   - **AC:** Given signed in → When I tap "Call /api/me" → Then `api-identity` returns verified claims and the app renders them.
   - **AC (negative):** Given an expired token → Then a clear "session expired" state, not a raw error.
   - **DoD:** proves the `aud` check in `api-identity` end-to-end.

4. `[mobile-identity]` Sign out
   - **As a** customer **I want** to sign out **so that** my session is cleared on this device.
   - **AC:** Given signed in → When I tap "Sign out" → Then local tokens are cleared and the IdP session ends.
   - **DoD:** post-logout redirect returns to Sign In.

---

## Epic 2 — SAML 2.0 B2B SSO

- **Status:** To Do · **Priority:** High · **Squad:** Identity/Auth platform · **Links:** ADR-004, FR-3, C-4, DESIGN §3
- **Why:** enterprise/business partners require SAML 2.0 SSO.
- **Scope:** SP-initiated (then IdP-initiated) · Spring Security `saml2-service-provider` on the Java auth surface (ADR-004) · Keycloak SAML client.
- **Success criteria:** partner user completes SP-initiated SSO and gets a session · unsolicited assertions validated for audience/destination/InResponseTo.

**Stories**

1. `[api-auth]` SP-initiated SAML login
   - **As a** partner user **I want** to SSO via my corporate IdP **so that** I can access the app without a second password.
   - **AC:** Given unauthenticated → When the SP redirects to the IdP and the assertion returns → Then the SP validates the XML signature and issues a session.
   - **DoD:** assertion validation uses Spring Security (no hand-rolled XML).

2. `[api-auth]` IdP-initiated SAML login
   - **As a** partner user **I want** to start from my IdP portal **so that** I reach the app already signed in.
   - **AC:** Given an unsolicited assertion → Then `aud`/destination/InResponseTo are validated before a session is minted.
   - **DoD:** replay of a stale assertion is rejected.

3. `[api-auth]` Keycloak SAML client
   - **As a** developer **I want** a SAML client in the local realm **so that** the flow runs end-to-end locally.
   - **AC:** Given the realm import → Then SP-initiated SSO completes against Keycloak.
   - **DoD:** documented in `keycloak/README.md`.

---

## Epic 3 — CI/CD pipeline

- **Status:** To Do · **Priority:** High · **Squad:** Identity/Auth platform · **Links:** ADR-005, NFR-4, DESIGN §4
- **Why:** automated build → test → containerize so every change is proven.
- **Scope:** GitHub Actions; Java tests in Docker (JDK 21 pin); build `api-identity` + `ui-identity`; push GHCR.
- **Success criteria:** a push/PR runs the full pipeline green · images published to GHCR.

**Stories**

1. `[ci/cd]` Typecheck + lint job
   - **AC:** Given a PR → Then the workflow runs typecheck (frontend) and lint on every push.
2. `[ci/cd]` Unit-test job (Java via Docker)
   - **AC:** Given a push → Then `api-identity` + `sdk-identity-jvm` tests run on JDK 21 in Docker and block the build on failure.
3. `[ci/cd]` Build + push images to GHCR
   - **AC:** Given a green test job → Then `api-identity` (Dockerfile) and `ui-identity` images are built and pushed to GHCR with a version tag.
4. `[ci/cd]` Deploy job
   - **AC:** Given a tagged image → Then it deploys `ui-identity` (Vercel) and `api-identity` (Render) using `OIDC_ISSUER`/`OIDC_AUDIENCE` from secrets.

---

## Epic 4 — Live demo deployment

- **Status:** To Do · **Priority:** Medium · **Squad:** Identity/Auth platform · **Links:** ADR-005, DESIGN §5, A-1
- **Why:** a running demo against a real IdP proves the stack outside localhost.
- **Scope:** `ui-identity` → Vercel · `api-identity` → Render · mobile via Expo · IdP = Okta dev tenant.
- **Success criteria:** a public URL signs in via Okta and calls `/api/me`.

**Stories**

1. `[ui-identity]` Deploy to Vercel — AC: static build with `authority` env var; loads at a public URL.
2. `[api-identity]` Deploy to Render — AC: Dockerfile runs on Render with `OIDC_ISSUER`/`OIDC_AUDIENCE`; `/health` is public.
3. `[mobile-identity]` Expo web preview — AC: `expo start --web` serves the app for the demo.
4. `[api-identity]` Swap to Okta — AC: set issuer + audience to the Okta dev tenant; discovery + JWKS resolve; login still works.

---

## Epic 5 — Auth service (`api-auth`) · BFF pattern

- **Status:** To Do · **Priority:** Medium · **Squad:** Identity/Auth platform · **Links:** ADR-006, ADR-010, DESIGN §6, NFR-9
- **Why:** the hardened variant — tokens stay server-side; the browser gets a session cookie.
- **Scope:** Spring Boot `oauth2Login()`; session cookie; proxy `/api/me` → `api-identity`.
- **Success criteria:** browser authenticates via `api-auth` and reaches `/api/me` with no token ever in the SPA.

**Stories**

1. `[api-auth]` Scaffold oauth2Login service — AC: a confidential OAuth2 client performs the code exchange server-side.
2. `[api-auth]` Session cookie + CSRF — AC: HttpOnly/Secure/SameSite cookie; CSRF + session-fixation protection on (NFR-9).
3. `[api-auth]` Proxy protected API — AC: `/api/me` is proxied to `api-identity` with the server-side access token.
4. `[api-auth]` Session lifecycle — AC: expiry + idle timeout + revocation; sign-out ends the session.

---

## Epic 6 — SDK Spring Boot starter

- **Status:** To Do · **Priority:** Medium · **Squad:** Identity/Auth platform · **Links:** ADR-007, ADR-009, FR-6
- **Why:** every resource server gets validation secure-by-default instead of re-wiring it.
- **Success criteria:** a RS depends on the starter and gets JWKS/RS256 + issuer + audience + scope enforcement with zero code.

**Stories**

1. `[sdk-identity-jvm]` Starter auto-config — AC: `issuer-uri` → JWKS/RS256 + issuer validation auto-configured.
2. `[sdk-identity-jvm]` Audience validator wired — AC: reuses `AudienceValidator` (from `api-identity`) via `audiences` config.
3. `[sdk-identity-jvm]` Scope enforcement helpers — AC: `requireScope` integrates with the RS security filter.

---

## Later backlog (enhancements)

- **Refresh-token rotation + reuse detection** — new refresh token per use; replay revokes the grant (R-1/R-2).
- **Okta Inline/Event hooks + Workflows (mock)** — documented pattern + mock for custom claims and lifecycle.
- **Kotlin DSL wrapper** — thin Kotlin wrapper over `sdk-identity-jvm` (ADR-002).
- **E2E tests (Playwright)** — login → `/api/me` end-to-end against Keycloak.

## Traceability

Epics map 1:1 to `ROADMAP.md`; stories link to ADRs and `REQUIREMENTS.md` FR/NFR ids. JIRA is the system of record for *work*; this file + the ADRs are the system of record for *decisions*. Don't duplicate epic text in both — link.
