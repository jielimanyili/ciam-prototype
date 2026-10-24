# Roadmap — CIAM Prototype

> Each item maps to an epic + stories in [`epics.md`](epics.md) (import `jira-import.csv` into JIRA).

## Now (Phase 2 — complete the reference stack)
1. **JVM SDK** (`sdk-identity-jvm/`) — Java, the canonical identity library (C-2). ✅ done (6 tests) · [Epic 0](epics.md)
2. **React Native client** (`mobile-identity/`) — Expo, Authorization Code + PKCE. · [Epic 1](epics.md)
3. **SAML 2.0** — SP-initiated on the Java server + a Keycloak SAML client. · [Epic 2](epics.md)
4. **CI/CD** — GitHub Actions: build → test → containerize → deploy. · [Epic 3](epics.md)

## Next (Phase 3 — live demo)
5. Deploy **ui-identity (Vercel) + api-identity (Render) + mobile-identity (Expo)** against an **Okta dev tenant**. · [Epic 4](epics.md)

## Later (enhancements)
- Refresh-token rotation + reuse detection demo. · [backlog](epics.md#later-backlog-enhancements)
- Okta Inline/Event hooks + Workflows (documented, with a mock). · [backlog](epics.md#later-backlog-enhancements)
- Auth Service (`api-auth`) — hardened auth variant with `oauth2Login()` (DESIGN §6). · [Epic 5](epics.md)
- Identity SDK **Spring Boot starter** — auto-configured RS validation (ADR-007). · [Epic 6](epics.md)
- Kotlin DSL wrapper for the SDK. · [backlog](epics.md#later-backlog-enhancements)
- E2E tests (Playwright) against Keycloak. · [backlog](epics.md#later-backlog-enhancements)

## Success metrics

- **Squad integration time** — a new service wires identity via the SDK/starter in < 1 day.
- **Security** — 0 open high-severity auth findings; all security-critical paths tested (signature/issuer/audience/expiry/scope).
- **Protocol coverage** — PKCE + Client Credentials + SAML all runnable end-to-end.
- **Live demo** — a public URL signs in via Okta and calls `/api/me`.
- **NFR compliance** — stateless (NFR-3), least privilege (NFR-2), testable offline (NFR-6) hold in code, not just docs.

