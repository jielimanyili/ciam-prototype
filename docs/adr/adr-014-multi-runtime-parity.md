# ADR-014 — Multi-runtime parity (shared OpenAPI contract + fixtures)

- **Status:** Accepted
- **Date:** 2026-10-29
- **Serves:** FR-4, FR-5 · NFR-6

## Context

Two resource servers — `api-identity-jvm` (Spring Boot) and `api-identity-python` (FastAPI) —
expose the same endpoints and must enforce identical JWT validation (signature/issuer/audience/
expiry) and scope rules. Left unmanaged, the two implementations will drift.

## Decision

Enforce parity with a **contract-first, fixture-shared** strategy:

1. **Single source of truth** — `docs/openapi.yaml` is the contract for both resource servers.
   Any endpoint, scope, or response change lands in the contract first.
2. **Language-agnostic parity fixtures** — a `test-fixtures/` directory holds:
   - a **parity matrix** (endpoint × auth/scope → expected status),
   - shared **JWT vectors** (test keypair + pre-signed tokens for valid / bad-signature / wrong
     issuer / wrong audience / expired),
   - expected responses.
   Both the JUnit and pytest suites read the *same* fixtures and assert the *same* outcomes.
3. **CI gate** — a single job runs both suites against the fixtures; drift fails the build (Epic 3).
4. **Contract tests** — a later enhancement that validates responses against `openapi.yaml`.

## Consequences

- **Pros:** drift-proof; one contract to review; NFR-6 (test offline, no IdP) satisfied identically.
- **Cons:** fixtures must stay language-agnostic (JSON, no runtime-specific types); contract-first
  discipline required.

## Alternatives considered

- **Duplicate docs/tests per runtime** — rejected: drift by construction.
- **OpenAPI codegen (Java + Python clients/servers)** — deferred to Later: overkill for four
  endpoints, but the contract is kept codegen-ready.
- **Schemathesis / contract-test framework** — deferred to the contract-test enhancement.
