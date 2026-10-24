# Testing — CIAM Prototype

> Test strategy for the CIAM stack: **what to test, at which layer, for which component** — and how to keep every test **offline** (no running IdP), per NFR-6.

## Principles

1. **Testable without an IdP (NFR-6)** — no test requires a running Keycloak/Okta. The IdP is replaced by fakes: a mocked `JwtDecoder`, hand-built `Jwt`s, and embedded JWK sets.
2. **Verify-then-authorize is the core under test** — signature → issuer → audience → expiry, then scope/claims (ADR-001).
3. **Pyramid** — many unit tests, fewer slice/integration tests, fewest E2E.
4. **Negative paths first** — the interesting cases are *rejected* tokens (wrong audience, missing scope, expired), not just happy paths.

## The pyramid (this stack)

| Layer | Proves | Cost | Example |
|---|---|---|---|
| **Unit** | one class in isolation (no Spring, no network) | low | `Pkce`, `ScopeAssertions`, `AudienceValidator` |
| **Slice / integration** | Spring wiring + the security filter chain, decoder mocked | medium | `ClaimsController` via `@WebMvcTest` |
| **Contract** | the API matches `openapi.yaml` | medium | (planned) |
| **E2E** | full login → protected API against a real IdP | high | (planned, Playwright) |

## Per-component plan

| Component | Existing | Planned / gap |
|---|---|---|
| `sdk-identity-jvm` | 6 unit tests — `Pkce` (2), `ScopeAssertions` (4) | `OidcDiscovery` + `ClientCredentials` unit tests (mock HTTP) |
| `api-identity` | 7 tests — `AudienceValidator` (3) + `ClaimsController` (4, `@WebMvcTest`) | `JwtDecoder` validator-chain test · contract test vs `openapi.yaml` |
| `ui-identity` | — | unit (`auth.service`, components) · E2E (Playwright) |
| `api-auth` (planned) | — | `oauth2Login` slice + session/CSRF tests (NFR-9) |
| `mobile-identity` (planned) | — | unit (token store) · E2E |

## Offline techniques (NFR-6)

Already used in the repo — these are why the suite runs with no IdP:

```java
// 1. Replace the decoder entirely
@MockBean JwtDecoder jwtDecoder;

// 2. Build a token in the test (no signing, no network)
Jwt.withTokenValue("t").header("alg", "RS256").claim("aud", "api-identity").build();

// 3. Authenticate a request with a fabricated JWT + authorities
mvc.perform(get("/api/admin").with(
    SecurityMockMvcRequestPostProcessors.jwt()
        .jwt(j -> j.claim("scope", "openid admin"))
        .authorities(new SimpleGrantedAuthority("SCOPE_admin"))));
```

## Quality gates (CI)

- Tests run on **JDK 21 in Docker** — `api-identity/Dockerfile` runs `mvn package` (which runs tests) at build time (ADR-005 · Epic 3).
- Pipeline order: typecheck/lint → **unit/slice tests** → build images → contract → E2E (planned).
- **Tests are a hard gate** — a failing test blocks the build, never advisory.

## Coverage & conventions

- JUnit 5 + AssertJ; test classes named `XxxTest`, mirroring the main package layout.
- No vanity percentage target. Require coverage of **security-critical paths**: JWT validation chain, scope enforcement, audience rejection, session handling.

## Gaps → epics

- **Contract tests** (checklist 6.4) — new story under Epic 3 (CI/CD).
- **E2E tests** (checklist 6.5) — Later backlog (Playwright).
- **`ui-identity` unit tests** — Epic 1 / Epic 3.
- **`api-auth` tests** — Epic 5.
