# ADR-011 — Python identity SDK + Python resource server (multi-runtime)

- **Status:** Accepted
- **Date:** 2026-10-29
- **Serves:** FR-6, FR-4, FR-5 · amends C-2

## Context

The reusable identity SDK (FR-6) is Java-only (`sdk-identity-jvm`, ADR-002), and the reference
resource server (`api-identity`) is Java/Spring Boot, per C-2 ("Backend = Java only"). But the
TriNet JD lists **Python**, and TriNet runs Python applications — so a Java-only reference leaves
a visible gap and undersells the identity platform as language-agnostic.

## Decision

1. Add **`sdk-identity-python`** — Python 3.12, a thin wrapper mirroring `sdk-identity-jvm`:
   `discover` (OIDC discovery), `generate_pkce` (S256), `fetch_client_credentials`,
   `scopes_of` / `require_scope` / `has_claim`. Dependency: **`httpx`** only (stdlib `json`).
   JWT validation stays in the resource server (mirrors the JVM SDK, where Spring Security does
   validation and the SDK standardizes around it).
2. Add **`api-identity-python`** — a FastAPI resource server mirroring `api-identity`: same
   endpoints (`/health`, `/api/me`, `/api/admin`, `/api/claims`), same JWT validation
   (RS256/JWKS via auto-discovery + issuer + audience + expiry), and scope enforcement via the
   SDK's `require_scope`. It consumes `sdk-identity-python`'s `discover()` to resolve the JWKS URI.
3. **Naming (extends ADR-009 rule 6):** the `-<runtime>` suffix applies to **any** `<layer>-<domain>`
   component that ships in multiple runtimes, not just SDKs. `api-identity` remains the reference
   (Java) runtime; `api-identity-python` is the alternate.
4. **Amend C-2:** backend is **Java (Spring Boot) primary**, with **Python (FastAPI) as a
   supported second runtime** for the reference/demo — motivated by TriNet's Python footprint.

## Consequences

- **Pros:** closes the Python gap in the JD; proves the SDK/validation is language-agnostic; gives
  a second resource server for the four-runtime (web/mobile/python/java) live demo.
- **Cons:** two runtimes to keep in parity (endpoints + validation semantics); C-2 is no longer
  "Java only".

## Alternatives considered

- **SDK only (no Python resource server)** — rejected: a library alone doesn't demonstrate the
  protection working in Python; the JD's "Python applications" call for a Python-protected API.
- **Authlib** — rejected: batteries-included but heavier; breaks the "thin wrapper" philosophy and
  parity with the JVM SDK's minimal surface.
- **Rename `api-identity` → `api-identity-jvm`** — rejected: churn (Docker image, README, Keycloak
  client IDs) for no added clarity; the unqualified name denotes the reference runtime.
