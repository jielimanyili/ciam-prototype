# Requirements & Constraints — CIAM Prototype

> The single source of truth for **what** the system must do and **under what
> constraints** — scoped to a Principal-level CIAM stack (Auth0, OIDC/OAuth2 + SAML, JWT validation, Angular/React Native, Java/TypeScript).
> This is step 1 of the process; `ARCHITECTURE.md` (structure), ADRs (decisions),
> and C4 (diagrams) build on it.

## 1. Functional requirements (FR)

| ID | Domain | Requirement | ADR | Design |
|---|---|---|---|---|
| FR-1 | Authentication | OIDC/OAuth 2.0 **Authorization Code + PKCE** (S256) login | [adr-001](adr/adr-001-oidc-oauth2-pkce.md) · [adr-006](adr/adr-006-direct-pkce-vs-bff.md) | [DESIGN §1](DESIGN.md) · §2 |
| FR-8 | Authentication | Self-service **registration / sign-up** | — | [best-practices §3](best-practices.md) |
| FR-9 | Authentication | **Social / federated login** (Google, Apple, …) | — | [best-practices §1](best-practices.md) |
| FR-10 | Authentication | **Passwordless / passkeys** (WebAuthn / FIDO2) | — | [best-practices §1](best-practices.md) |
| FR-11 | Authentication | Self-service **password reset / account recovery** | — | [best-practices §3](best-practices.md) |
| FR-12 | Authentication | **Adaptive / step-up MFA** | — | [best-practices §3](best-practices.md) |
| FR-16 | Authentication | **Refresh-token rotation + revocation / sign-out** | [adr-001](adr/adr-001-oidc-oauth2-pkce.md) | [best-practices §2](best-practices.md) |
| FR-4 | Authorization | **JWT validation** — signature (JWKS), issuer, audience, expiry | [adr-001](adr/adr-001-oidc-oauth2-pkce.md) · [adr-007](adr/adr-007-rs-validation-library.md) | [ARCHITECTURE §4](ARCHITECTURE.md) |
| FR-5 | Authorization | **Scoping + claims** authorization (least privilege) | [adr-001](adr/adr-001-oidc-oauth2-pkce.md) · [adr-007](adr/adr-007-rs-validation-library.md) | [ARCHITECTURE §4](ARCHITECTURE.md) |
| FR-2 | Integration | OAuth 2.0 **Client Credentials** (server-to-server) | [adr-001](adr/adr-001-oidc-oauth2-pkce.md) | [DESIGN §1](DESIGN.md) |
| FR-3 | Integration | **SAML 2.0** SSO (SP-initiated, then IdP-initiated) | [adr-004](adr/adr-004-saml-spring-security.md) | [DESIGN §3](DESIGN.md) |
| FR-13 | Privacy & Lifecycle | **Consent capture & management** (GDPR/CCPA) | — | [best-practices §6](best-practices.md) |
| FR-14 | Privacy & Lifecycle | **Profile self-service** (view/edit own attributes) | — | [best-practices §3](best-practices.md) |
| FR-15 | Privacy & Lifecycle | **Account lifecycle** (disable/enable/delete) + **right-to-be-forgotten** | — | [best-practices §6](best-practices.md) |
| FR-17 | Privacy & Lifecycle | **Account linking** (social + email → one profile) | — | [best-practices §1](best-practices.md) |
| FR-6 | Developer Experience | **Reusable SDK** for product squads (developer advocacy) | [adr-002](adr/adr-002-jvm-sdk-java.md) | [DESIGN §1](DESIGN.md) |
| FR-7 | Developer Experience | **IdP hooks/Actions** for custom claims + lifecycle | — | [best-practices mapping](best-practices.md) · [ROADMAP "Later"](ROADMAP.md) |

> **Design note:** FR-8 … FR-15 and FR-17 are **delegated to the IdP's built-in features** (Auth0 / Keycloak) — documented, not hand-built.

## 2. Non-functional requirements (NFR)

| ID | Requirement | ADR | Design |
|---|---|---|---|
| NFR-1 | **Security by design** | [adr-001](adr/adr-001-oidc-oauth2-pkce.md) | [threat-model.md](threat-model.md) · [best-practices.md](best-practices.md) |
| NFR-2 | **Least privilege** | [adr-001](adr/adr-001-oidc-oauth2-pkce.md) | [best-practices §4](best-practices.md) |
| NFR-3 | **Stateless + horizontally scalable** | [adr-001](adr/adr-001-oidc-oauth2-pkce.md) | [ARCHITECTURE §3](ARCHITECTURE.md) |
| NFR-4 | **Maintainability** | — | [best-practices.md](best-practices.md) |
| NFR-5 | **Compliance-ready** | — | [best-practices §6](best-practices.md) |
| NFR-6 | **Testability without an IdP** | [adr-001](adr/adr-001-oidc-oauth2-pkce.md) | [DESIGN.md](DESIGN.md) |
| NFR-7 | **Observability & audit** — log authN/authZ events + denials for audit & incident detection | — | [best-practices §8](best-practices.md) · [threat-model.md](threat-model.md) |
| NFR-8 | **Abuse protection** — rate limiting, bot / credential-stuffing defense (delegated to IdP) | — | [best-practices §3](best-practices.md) · [risk-register R-10](risk-register.md) |
| NFR-9 | **Session security** (BFF variant) — HttpOnly/Secure/SameSite cookie, session expiry + idle timeout, CSRF + session-fixation prevention | [adr-006](adr/adr-006-direct-pkce-vs-bff.md) | [DESIGN §6](DESIGN.md) · [best-practices §2](best-practices.md) |

## 3. Constraints (C)

| ID | Constraint |
|---|---|
| C-1 | **IdP = Auth0** (CIAM); Keycloak is the local stand-in |
| C-2 | **Backend = Java (Spring Boot)** primary · **Python (FastAPI)** secondary ([adr-011](adr/adr-011-python-sdk-and-resource-server.md)) |
| C-3 | **Frontend = Angular** (web) **+ React Native** (mobile) |
| C-4 | **Protocols = OIDC/OAuth2 + SAML 2.0** — no custom auth |
| C-5 | **No secrets** in tokens, env, or source → secrets manager in prod (AWS Secrets Manager, [adr-008](adr/adr-008-secrets-management.md)) |
| C-6 | **JDK 21 (LTS, N-1) / Spring Boot 3.3.2** baseline |

## 4. Assumptions (A)

| ID | Assumption |
|---|---|
| A-1 | Keycloak locally; **Auth0 tenant** for the live demo |
| A-2 | Audience validation via a custom `OAuth2TokenValidator` + Keycloak audience mappers ([adr-013](adr/adr-013-token-audience.md)) |
| A-3 | SAML scope = SP-initiated first, IdP-initiated second |

## 5. Non-goals (out of scope)

- Live IdP hooks/Actions + Workflows (documented pattern only)
- Production hardening: secrets manager, TLS, observability, rate limiting

## 6. Traceability

Rules:
- Every requirement links **forward** to what resolves it — a **decision** (ADR) and/or a **design** (DESIGN / ARCHITECTURE / C4 / guidance). Requirements stay implementation-agnostic (what + why, not where-in-code).
- Every ADR / design links **back** to the requirement(s) it serves (see each ADR's `Serves` line).
- Maturity + sequencing (**implemented / planned / documented**, Now / Next / Later) lives in [`ROADMAP.md`](ROADMAP.md) — requirements stay a stable what/why contract.
- Cells marked `(planned)` point at decisions not yet locked as ADRs.
