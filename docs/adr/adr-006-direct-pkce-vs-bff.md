# ADR-006 — Web App authentication: direct PKCE vs. BFF

- **Status:** Accepted
- **Date:** 2026-10-22
- **Serves:** FR-1

## Context

The Web App (SPA) must authenticate users. Two patterns exist:

1. **Direct-to-IdP (Authorization Code + PKCE)** — the SPA is a *public client* that
   performs the OIDC handshake directly and holds the access token in memory.
2. **BFF (Backend-for-Frontend)** — a Java service acts as the OAuth2 *client*
   (`oauth2Login()`), performs the handshake server-side, and issues a **session cookie**,
   so tokens never reach the browser.

## Decision

Use **direct PKCE as the default** (implemented) and keep the **BFF as the documented
hardened variant** (planned — `api-auth/`). Direct PKCE stays the default
because it demonstrates the OIDC/PKCE requirement directly and keeps the system
**stateless**.

## Consequences

- **Pros (direct PKCE):** stateless + horizontally scalable (NFR-3); demonstrates the
  protocol end-to-end; no session management to build.
- **Pros (BFF):** tokens never in the browser (resists XSS token theft); clients simplify
  to a cookie; one place to swap IdPs.
- **Cons (BFF):** introduces stateful sessions (cookie expiry / revocation / scale); one
  more service to build and run.

## Alternatives considered

- **BFF-only** — rejected as the default: hides the very OIDC/PKCE flow the prototype
  must demonstrate, and adds session state before it's needed.
- **Implicit flow** — rejected (see ADR-001): deprecated, tokens in the URL.
