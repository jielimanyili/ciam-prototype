# ADR-017 — JFrog Artifactory for artifacts (enterprise), public registries for the demo

- **Status:** Accepted
- **Date:** 2026-10-29
- **Serves:** FR-6 (developer advocacy) · NFR-4

## Context

TriNet uses **JFrog Artifactory** as its internal artifact store. A reference that publishes only
to public Maven Central / PyPI doesn't match the enterprise reality where product squads pull from
an internal registry (proxying + hosting + caching).

## Decision

- **Enterprise (TriNet):** all artifacts go to **JFrog Artifactory** — Docker images (Container
  Registry), `sdk-identity-jvm` (Maven repo), `sdk-identity-python` (PyPI repo). Product squads pull
  from JFrog, never from public registries.
- **Public demo:** publish to the public equivalents — Docker → **ECR** (or GHCR), Java → **Maven
  Central**, Python → **PyPI** — so a reviewer can verify the artifacts.
- **Registry-agnostic CI/CD:** the publish step takes the registry as a parameter (URL + creds), so
  "swap ECR/Central/PyPI → JFrog" is a config change, not a code change.

## Consequences

- **Pros:** matches TriNet; one internal artifact store (images + Java + Python); CI stays portable.
- **Cons:** the demo can't point at a private JFrog, hence the dual-path (demo vs enterprise).

## Alternatives considered

- **Public registries only** — rejected: doesn't reflect TriNet's internal JFrog flow.
- **JFrog only** — rejected: the public demo needs a reachable registry.
