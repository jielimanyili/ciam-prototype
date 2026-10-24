# Roadmap — CIAM Prototype

## Now (Phase 2 — complete the reference stack)
1. **JVM SDK** (`sdk-identity-jvm/`) — Java, the canonical identity library (C-2). ✅ done (6 tests)
2. **React Native client** (`mobile-identity/`) — Expo, Authorization Code + PKCE.
3. **SAML 2.0** — SP-initiated on the Java server + a Keycloak SAML client.
4. **CI/CD** — GitHub Actions: build → test → containerize → deploy.

## Next (Phase 3 — live demo)
5. Deploy **ui-identity (Vercel) + api-identity (Render) + mobile-identity (Expo)** against an **Okta dev tenant**.

## Later (enhancements)
- Refresh-token rotation + reuse detection demo.
- Okta Inline/Event hooks + Workflows (documented, with a mock).
- Auth Service (`api-auth`) — hardened auth variant with `oauth2Login()` (DESIGN §6).
- Identity SDK **Spring Boot starter** — auto-configured RS validation (ADR-007).
- Kotlin DSL wrapper for the SDK.
- E2E tests (Playwright) against Keycloak.
