# Planning Completeness Checklist — CIAM Prototype

> A single review surface for the full development-planning cycle. Each row names a planning item, its **status**, and **where** it lives (or why it's missing). Use it to answer *"is the plan complete?"* and to drive what to add next.

## Status legend

| Mark | Meaning |
|---|---|
| ✅ | **Done** — artifact exists and is complete |
| 🟡 | **Partial** — exists but incomplete |
| ❌ | **Gap** — missing; worth adding |
| 🚫 | **Non-goal** — intentionally out of scope (documented) |

## Summary

- **Plan → build is complete**: requirements → architecture → design → decisions → security → roadmap → backlog → code, all traceable.
- **Remaining gaps are code/build only** (contract/integration/E2E tests, CI pipeline) — tracked in `epics.md`; all documentation gaps are now closed.
- **Intentional non-goals** (observability, TLS, rate limiting, prod secrets) are documented in `REQUIREMENTS.md §5` — not omissions, but they become the real work if this goes to production.

---

## 1. Requirements

| # | Item | Status | Where / note |
|---|---|---|---|
| 1.1 | Functional requirements (FR-1…17) | ✅ | `REQUIREMENTS.md §1` |
| 1.2 | Non-functional requirements (NFR-1…9) | ✅ | `REQUIREMENTS.md §2` |
| 1.3 | Constraints (C-1…6) | ✅ | `REQUIREMENTS.md §3` |
| 1.4 | Assumptions (A-1…3) | ✅ | `REQUIREMENTS.md §4` |
| 1.5 | Non-goals / scope boundary | ✅ | `REQUIREMENTS.md §5` |
| 1.6 | Traceability (req → ADR → design) | ✅ | `REQUIREMENTS.md §6` |

## 2. Architecture

| # | Item | Status | Where / note |
|---|---|---|---|
| 2.1 | System context (C4 L1) | ✅ | `C4.md` |
| 2.2 | Containers (C4 L2) | ✅ | `C4.md` |
| 2.3 | Components (C4 L3) | ✅ | `C4.md` |
| 2.4 | Auth flows (sequence diagrams) | ✅ | `ARCHITECTURE.md §3` |
| 2.5 | Data model | ✅ | `ARCHITECTURE.md §6` — token + identity/session/client models |
| 2.6 | Ownership / team topology | ✅ | `ADR-010` · `ARCHITECTURE.md §5` |

## 3. Design & decisions

| # | Item | Status | Where / note |
|---|---|---|---|
| 3.1 | Component design | ✅ | `DESIGN.md` |
| 3.2 | Decisions logged (ADRs) | ✅ | 11 ADRs (`docs/adr/`) |
| 3.3 | API contract | ✅ | `openapi.yaml` |
| 3.4 | Naming conventions | ✅ | `ADR-009` |

## 4. Security

| # | Item | Status | Where / note |
|---|---|---|---|
| 4.1 | Threat model | ✅ | `threat-model.md` |
| 4.2 | Risk register | ✅ | `risk-register.md` |
| 4.3 | Best practices / anti-patterns | ✅ | `best-practices.md` · `anti-patterns.md` |
| 4.4 | Secrets-management decision | ✅ | `ADR-008` |
| 4.5 | Controls implemented | ✅ | JWT (sig/issuer/audience/expiry) + scope done in `api-identity`; TLS + rate limiting = non-goal |

## 5. Planning & sequencing

| # | Item | Status | Where / note |
|---|---|---|---|
| 5.1 | Roadmap (Now / Next / Later) | ✅ | `ROADMAP.md` |
| 5.2 | Backlog (epics → stories + ACs) | ✅ | `epics.md` · `jira-import.csv` |
| 5.3 | Estimation (points) | ✅ | `epics.md` — relative (Fibonacci) sizing |
| 5.4 | Dependencies | ✅ | `epics.md` — epic sequence + IdP prerequisite |
| 5.5 | Shared Definition of Done / Ready | ✅ | `epics.md` — shared DoD/DoR |

## 6. Quality & testing

| # | Item | Status | Where / note |
|---|---|---|---|
| 6.1 | Test strategy / pyramid | ✅ | `TESTING.md` |
| 6.2 | Unit tests | ✅ | SDK (6) · `api-identity` (controller + audience) |
| 6.3 | Integration tests | 🟡 | `docker build` runs tests; no explicit integration suite |
| 6.4 | Contract tests | ❌ | `openapi.yaml` exists; no contract test |
| 6.5 | E2E tests | 🟡 | planned (`epics.md` backlog), not built |
| 6.6 | CI quality gates | 🟡 | defined in `ADR-005` / Epic 3, not built |

## 7. Release & deployment

| # | Item | Status | Where / note |
|---|---|---|---|
| 7.1 | CI/CD pipeline definition | ✅ | `ADR-005` · `DESIGN.md §4` (planned, not built) |
| 7.2 | Environments (dev / stage / prod) | ✅ | `RELEASING.md` |
| 7.3 | Branching strategy | ✅ | `RELEASING.md` — trunk-based |
| 7.4 | Versioning (semver) | ✅ | `RELEASING.md` |
| 7.5 | Rollout / rollback | ✅ | `RELEASING.md` |

## 8. Operations

| # | Item | Status | Where / note |
|---|---|---|---|
| 8.1 | Observability (logs/metrics/tracing) | 🚫 | non-goal; `NFR-7` documented |
| 8.2 | Alerting / monitoring | 🚫 | non-goal |
| 8.3 | Runbook / troubleshooting | ✅ | `RUNBOOK.md` |
| 8.4 | Incident response | ✅ | `RUNBOOK.md` |
| 8.5 | Disaster recovery / backup | 🚫 | non-goal |
| 8.6 | Secret-rotation runbook | ✅ | `RUNBOOK.md` (automation = non-goal) |

## 9. Team & ownership

| # | Item | Status | Where / note |
|---|---|---|---|
| 9.1 | Ownership model (platform vs product) | ✅ | `ADR-010` |
| 9.2 | RACI / escalation path | ✅ | `ARCHITECTURE.md §5` |

## 10. Success measurement

| # | Item | Status | Where / note |
|---|---|---|---|
| 10.1 | KPIs / success metrics | ✅ | `ROADMAP.md` — success metrics |

## 11. Compliance & privacy

| # | Item | Status | Where / note |
|---|---|---|---|
| 11.1 | GDPR/CCPA mapping | ✅ | `best-practices.md §6` — control matrix |
| 11.2 | Consent / lifecycle (FR-13…15) | 🚫 | delegated to IdP (documented) |

---

## Remaining gaps (code/build — not docs)

1. **Contract tests** (6.4) — validate `api-identity` against `openapi.yaml`.
2. **Integration / E2E tests** (6.3, 6.5) — explicit suites + Playwright (Epic 3, Later backlog).
3. **CI pipeline build-out** (6.6, 7.1) — implement Epic 3 (GitHub Actions).

## How to keep this alive

Update statuses here whenever an ADR, design, or roadmap item lands. This checklist is the **index**; the artifacts it points to are the source of truth.

