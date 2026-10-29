# ADR-018 — CI/CD tool: GitHub Actions (demo) + Harness (enterprise)

- **Status:** Accepted
- **Date:** 2026-10-29
- **Serves:** NFR-4 · supersedes ADR-005 (CI tool)

## Context

TriNet is migrating CI/CD from Jenkins to Harness. ADR-005 chose GitHub Actions for the reference.
GitHub Actions is public + free + verifiable (reviewers see the workflow YAML + green builds), but it
isn't TriNet's target tool. The pipeline's stages (typecheck/lint → test → parity → build/push →
publish → deploy) are tool-agnostic.

## Decision

- **Demo CI/CD = GitHub Actions** — public, zero-server, observable on the repo (the CI/CD analog of
  "Keycloak stands in for Okta").
- **Enterprise target = Harness** — document the stage-by-stage mapping (Harness CI pipelines +
  CD stages), and note Jenkins as the "from" state in TriNet's migration.
- **Portable pipeline:** the stage DAG is tool-agnostic; GitHub Actions YAML and Harness YAML are two
  renderings of the same pipeline.

## Consequences

- **Pros:** verifiable demo; demonstrates Harness/Jenkins knowledge without a Harness account; maps
  to TriNet's real toolchain.
- **Cons:** Harness itself isn't exercised in the demo (mitigated by the documented mapping).

## Alternatives considered

- **Harness for the demo** — rejected: requires a SaaS account + the pipeline isn't observable on the
  repo; defeats verifiability.
- **Jenkins for the demo** — rejected: legacy; TriNet is moving away from it.
