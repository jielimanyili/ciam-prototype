# ADR-021 — Live-demo IdP → Auth0 (Okta free plan requires corporate email)

- **Status:** Accepted
- **Date:** 2026-10-29
- **Serves:** A-1, C-1 · supersedes the IdP choice in ADR-005 / ADR-016

## Context

The live demo's IdP was **Okta** (ADR-005 "Live IdP: Okta free dev tenant"; ADR-016 "IdP → Okta dev
tenant"). At account setup, Okta's free developer signup turned out to have been replaced by the
**Integrator Free Plan**, which now requires a **corporate (non-free) email** — the author's
personal Gmail is rejected. That blocks the planned Okta dev tenant.

## Decision

Use **Auth0** (Okta's Customer Identity Cloud) free tier as the live-demo IdP:

- Tenant `jie-ciam.us.auth0.com`; issuer `https://jie-ciam.us.auth0.com/` (trailing slash — Auth0's
  `iss` claim carries it).
- One Auth0 **API** (audience) `https://api.ciam-prototype`; `admin` permission for the M2M client.
- The same four OIDC clients Okta would have had: `ui-identity` (SPA), `mobile-identity` (Native),
  `api-auth` (Regular Web, confidential), `ciam-service` (Machine-to-Machine).

The rest of the architecture is unchanged — the stack is provider-agnostic (standard OIDC), so the
issuer + audience are the only config that moves (ADR-007).

## Consequences

- **Pros:** free + permanent (7,500 MAU); Gmail-friendly; Auth0 is CIAM-native, arguably a better
  fit than Okta Workforce for a customer-identity demo.
- **Cons:** three small Auth0-specific code changes — (1) strip the issuer's trailing slash for
  Spring discovery (it otherwise 404s), (2) send the API `audience` in the authorize request (SPA
  `extraQueryParams` + BFF `AuthorizationRequestResolver`), (3) BFF `client_secret_post` to match
  Auth0's Regular Web App default.
- Keycloak remains the local stand-in; SAML stays Keycloak-local (Auth0's SAML story differs).

## Alternatives considered

- **Okta Workforce trial (30-day)** — rejected: temporary, and may still require a corporate email.
- **Okta with a purchased domain email** — rejected: extra cost/effort for a demo.
- **Stay Keycloak-only (self-host)** — rejected: the demo should show a managed SaaS IdP.