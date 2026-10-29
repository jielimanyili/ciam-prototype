# Roadmap — CIAM Prototype

> Each item maps to an epic + stories in [`epics.md`](epics.md) (import `jira-import.csv` into JIRA).

## Now (Phase 2 — complete the reference stack)
1. **JVM SDK** (`sdk-identity-jvm/`) — Java, the canonical identity library (C-2). ✅ done (6 tests) · [Epic 0](epics.md)
1a. **Python SDK** (`sdk-identity-python/`) — mirrors the JVM SDK (discovery, PKCE, client-credentials, scope). ✅ done (6 tests) · [ADR-011](adr/adr-011-python-sdk-and-resource-server.md)
1b. **Python resource server** (`api-identity-python/`) — FastAPI; same endpoints + JWT validation as `api-identity-jvm`. ✅ done (5 tests) · [ADR-011](adr/adr-011-python-sdk-and-resource-server.md)
2. **React Native client** (`mobile-identity/`) — Expo, Authorization Code + PKCE. · [Epic 1](epics.md)
3. **SAML 2.0** — SP-initiated on the Java server + a Keycloak SAML client. ✅ done (config + Keycloak client) · [Epic 2](epics.md)
4. **CI/CD** — GitHub Actions: build → test → containerize → deploy. · [Epic 3](epics.md)

## Next (Phase 3 — live demo)
5. Deploy **ui-identity (S3+CloudFront) + api-identity-jvm / api-identity-python / api-auth (ECS Fargate) + mobile-identity (Expo)** against an **Okta dev tenant**. 🔄 IaC + CI/CD written (`terraform/` + `.github/workflows/deploy.yml`); apply blocked on AWS + Okta accounts. · [Epic 4](epics.md)

## Later (enhancements)
- Refresh-token rotation + reuse detection demo. · [backlog](epics.md#later-backlog-enhancements)
- Okta Inline/Event hooks + Workflows (documented, with a mock). · [backlog](epics.md#later-backlog-enhancements)
- Auth Service (`api-auth`) — hardened auth variant with `oauth2Login()` (DESIGN §6). ✅ done · [Epic 5](epics.md)
- Identity SDK **Spring Boot starter** — auto-configured RS validation (ADR-007). ✅ done · [Epic 6](epics.md)
- Kotlin DSL wrapper for the SDK. · [backlog](epics.md#later-backlog-enhancements)
- E2E tests (Playwright) against Keycloak. · [backlog](epics.md#later-backlog-enhancements)
- **Migration & adoption** — strangler-fig + auto-PR codemod (ADR-015 · DESIGN §8). · [backlog](epics.md#later-backlog-enhancements)

## Success metrics

- **Squad integration time** — a new service wires identity via the SDK/starter in < 1 day.
- **Security** — 0 open high-severity auth findings; all security-critical paths tested (signature/issuer/audience/expiry/scope).
- **Protocol coverage** — PKCE + Client Credentials + SAML all runnable end-to-end.
- **Live demo** — a public URL signs in via Okta and calls `/api/me`.
- **NFR compliance** — stateless (NFR-3), least privilege (NFR-2), testable offline (NFR-6) hold in code, not just docs.

