# ADR-015 — Strangler-fig migration with auto-PR adoption

- **Status:** Accepted
- **Date:** 2026-10-29
- **Serves:** FR-6 (developer advocacy) · ADR-010 (platform vs product) · ADR-007 (library)

## Context

TriNet already runs Okta, so the reference is not a greenfield. Adoption across existing product
teams is where the risk lives: a big-bang cutover of authentication would be high-risk and hard to
roll back. A Principal-level plan must show how the new auth rolls out safely, one service at a time.

## Decision

1. **Strangler-fig migration** (incremental, not big-bang): keep legacy Okta auth running; run the
   new pattern (SDK/starter + audience-mapped tokens, ADR-013) alongside; migrate one service at a time.
2. **Snapshot first** — inventory the as-is estate (clients, grants, scopes/audiences, per-app
   validation) and publish an **old→new mapping** so entitlements survive the move.
3. **Dual-validation window** — during transition, a migrated service accepts *both* old and new
   tokens behind a flag; rollback is a config flip, not a redeploy.
4. **Auto-PR adoption** — a migration codemod detects legacy auth wiring, rewrites it to the
   SDK/starter + new audience, and opens a PR the owning squad reviews/merges. Guides + templates
   cover the manual path.
5. **Decommission last** — remove legacy audiences/validators only after the last service migrates.

## Consequences

- **Pros:** zero-downtime adoption; "migrating is reviewing a PR, not relearning auth"; demonstrates
  cross-organization influence (a JD signal).
- **Cons:** dual-validation is temporary complexity; the codemod must be maintained.

## Alternatives considered

- **Big-bang cutover** — rejected: no rollback, high blast radius.
- **Manual guides only (no auto-PR)** — rejected: slow, doesn't scale across many squads.
- **Rewrite all apps** — rejected: violates the strangler pattern, huge blast radius.
