# Risk Register — CIAM Prototype

> Likelihood (L) and Impact (I) are rated Low / Medium / High; severity = L × I.
> "Status" = where the mitigation stands: **Implemented** / **Planned** / **Documented** / **Deferred**.

| ID | Risk | L | I | Mitigation | Status |
|---|---|---|---|---|---|
| R-1 | Access-token theft (stolen bearer token) | M | H | Short-lived tokens + refresh rotation + reuse detection; token in memory, not localStorage | Implemented (short expiry, memory) / Planned (rotation) |
| R-2 | Refresh-token replay | M | H | Rotation + reuse detection → revoke the whole grant | Planned |
| R-3 | Secret leakage (client secret / DB password in source, env, or token) | M | H | No secrets in tokens (signed ≠ encrypted); AWS Secrets Manager in prod ([adr-008](adr/adr-008-secrets-management.md)) | Implemented (no secrets in tokens) / Planned (AWS Secrets Manager) |
| R-4 | Over-scoped token → privilege escalation | M | H | Least-privilege scopes; enforce at the resource server | Implemented |
| R-5 | SAML assertion mis-validation (XML signature, audience, InResponseTo) | L | H | Spring Security SAML SP; validate audience / destination / InResponseTo | Planned |
| R-6 | SPA token exposure via XSS (localStorage) | M | H | Access token in memory; refresh in HttpOnly cookie; CSP; BFF | Implemented |
| R-7 | IdP vendor lock-in (Okta) | L | M | Standard protocols (OIDC/SAML); Keycloak stand-in proves portability | Mitigated by design |
| R-8 | Build-env JDK/version drift (JDK 26 local vs. JDK 21 baseline) | M | L | Dockerfile pins `eclipse-temurin:21` | Mitigated |
| R-9 | Compliance (GDPR/CCPA) for customer PII | M | H | Consent capture, data minimization, right-to-be-forgotten | Documented |
| R-10 | Credential stuffing / brute force against the IdP | H | M | IdP rate limiting, bot detection, MFA | Deferred to IdP (Okta/Keycloak) |
