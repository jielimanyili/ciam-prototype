# ADR-012 — Symmetric runtime suffixes (`api-identity` → `api-identity-jvm`)

- **Status:** Accepted
- **Date:** 2026-10-29
- **Serves:** ADR-009 (naming) · ADR-011 (multi-runtime) · C-3

## Context

ADR-011 added `api-identity-python` but kept `api-identity` as the unqualified Java reference.
Now that two resource servers exist, the unqualified name is ambiguous — "api-identity" reads as
"the reference one," but a service catalog (and a reviewer) can't tell its runtime at a glance.

## Decision

Apply the `-<runtime>` suffix **symmetrically** to every component that ships in multiple runtimes:

- Rename `api-identity` → `api-identity-jvm` (directory, Maven `artifactId`, Spring app name,
  Docker image tag, audience default, docs).
- Result: `api-identity-jvm` + `api-identity-python`; `sdk-identity-jvm` + `sdk-identity-python`.
- `-jvm` (not `-java`) covers Java + the planned Kotlin DSL wrapper (ADR-002), consistent with
  ADR-009 rule 6.
- **Distinct audiences** — `api-identity-jvm` vs `api-identity-python` — the secure default (a token
  minted for one resource server can't be replayed to the other).

## Consequences

- **Pros:** symmetric, self-describing, catalog-discoverable; distinct audiences prevent cross-server replay.
- **Cons:** one-time mechanical rename (dirs, artifactId, app name, Docker tag, docs).

## Alternatives considered

- **Keep `api-identity` unqualified** (ADR-011's choice) — superseded: ambiguous once a second
  resource server exists.
- **`-java` instead of `-jvm`** — rejected: `-jvm` is the established runtime marker and covers
  Kotlin (ADR-002, ADR-009).
