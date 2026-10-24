# CIAM Best Practices

The "do this" reference for the CIAM prototype — the counterpart to `anti-patterns.md`. Maps directly to the CIAM/security requirements.

## 1. Protocol selection
- OIDC/OAuth 2.0 **Authorization Code + PKCE (S256)** for SPAs and mobile (implicit is deprecated).
- **Client Credentials** for server-to-server / machine identity.
- **SAML 2.0** for B2B / enterprise SSO where the partner's IdP dictates the protocol.
- **FIDO2 / WebAuthn / passkeys** for passwordless where possible.
- Never: implicit grant, resource-owner password grant, hand-rolled crypto.

## 2. Token security
- **RS256 (asymmetric)** so services validate via JWKS; the IdP holds the key and can rotate it.
- **Short-lived access tokens** (minutes) + **refresh-token rotation with reuse detection**.
- **Scopes + claims** for least privilege; validate **issuer / audience / expiry / nbf** on every request.
- Secure storage: access token **in memory**; refresh token in **HttpOnly/Secure/SameSite** cookie; a **BFF** keeps tokens off the browser entirely.

## 3. Authentication hardening
- **Adaptive / step-up MFA** on risk signals (new device, geo, high-value action) — not every login.
- **Progressive profiling** — ask for data only when you need it.
- Bot detection, rate limiting, brute-force lockout.

## 4. Authorization
- **Least privilege**: scopes first, then claims/roles for finer decisions.
- RBAC → **ABAC / ReBAC** as entitlements grow.
- **Enforce at the resource server** — never trust the client.

## 5. Microservice / API security
- Validate JWT via **JWKS + issuer + audience + expiry** at every trust boundary.
- Centralize in a **reusable SDK with safe defaults** (the "developer advocacy" requirement).
- API gateway at the edge: routing, rate limits, token validation.

## 6. Privacy & consent

| GDPR/CCPA obligation | Where handled |
|---|---|
| Consent capture & management | IdP + `api-identity` (FR-13) |
| Right to erasure (RTBF) | account lifecycle (FR-15); delegated to IdP |
| Data minimization | no secrets/PII in tokens (signed ≠ encrypted); claims on demand |
| Encryption in transit / at rest | TLS (prod non-goal) · IdP-managed store |
| Access & portability | profile self-service (FR-14) |

## 7. Secrets management
- DB passwords / API keys → **secrets manager (Vault)**, never in CIAM, code, env, or tokens.
- **Workload identity** (OIDC / Client Credentials) → Vault → **short-lived dynamic DB credentials**.
- OAuth2 **client credentials** (`client_id` / `client_secret`) → Vault, not hardcoded per service — so IdP migration is a secret refresh, not a redeploy.

## 8. Observability & audit
- Log **authorization denials** and token-validation failures; monitor token issuance; audit the identity lifecycle.

## 9. Developer enablement
- Ship an SDK with **safe defaults** (PKCE on, validation on) + docs + a reference implementation → squads integrate correctly *by default*.

## 10. Scale & availability
- Internet-facing: DDoS defense, global low-latency, **stateless token validation** for horizontal scale.

## Capability mapping

| Capability | Where |
|---|---|
| CIAM architecture (Okta) | Okta / Keycloak as the IdP |
| OIDC/OAuth2 + PKCE, Client Credentials, SAML | §1 |
| JWT validation / scoping / claims | §2, §4, §5 |
| Okta Inline/Event hooks + Workflows | custom claims via Inline hooks; lifecycle via Workflows |
| Reusable SDKs + documentation | §9 |
| Security by design | §2–§8 |
| Cloud-native CI/CD | deployment phase |
