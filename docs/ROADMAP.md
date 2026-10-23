# Roadmap — CIAM Prototype

## Now (Phase 2 — complete the reference stack)
1. **JVM SDK** (`identity-sdk-jvm/`) — Java, the canonical identity library (C-2). ✅ done (6 tests)
2. **React Native client** (`mobile/`) — Expo, Authorization Code + PKCE.
3. **SAML 2.0** — SP-initiated on the Java server + a Keycloak SAML client.
4. **CI/CD** — GitHub Actions: build → test → containerize → deploy.

## Next (Phase 3 — live demo)
5. Deploy **frontend (Vercel) + backend (Render) + mobile (Expo)** against an **Okta dev tenant**.

## Later (enhancements)
- Refresh-token rotation + reuse detection demo.
- Okta Inline/Event hooks + Workflows (documented, with a mock).
- BFF (Client Auth Service) — hardened auth variant with `oauth2Login()` (DESIGN §6).
- Audience validator wired into the Java server.
- Identity SDK **Spring Boot starter** — auto-configured RS validation (ADR-007).
- Kotlin DSL wrapper for the SDK.
- E2E tests (Playwright) against Keycloak.
