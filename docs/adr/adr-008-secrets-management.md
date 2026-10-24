# ADR-008 — Secrets management: AWS Secrets Manager (prod) + host secrets (demo)

- **Status:** Accepted
- **Date:** 2026-10-24
- **Serves:** C-5, NFR-1 (see also risk-register R-3)

## Context

C-5 requires **no secrets in tokens, env, or source** → a **secrets manager in prod**.
Earlier docs left the product unnamed or used **"Vault"** as a placeholder (ADR-007 §IdP
migration, risk-register R-3), but no product was actually chosen.

The prototype has exactly **one long-lived secret** today:

- the `ciam-service` client secret for the Client Credentials grant (hardcoded
  `service-secret` in `keycloak/ciam-realm.json` — a *local stand-in*).

A future BFF (ADR-006) adds an Okta **client secret**; a real system adds DB credentials.
The resource server itself holds **no secret at runtime** — RS256/JWKS validation is
public-key (ADR-001), so it needs no stored secret.

Phase 3's backend host is **Render** (ADR-005), which is **not AWS**.

## Decision

1. **Production** secrets store = **AWS Secrets Manager** (managed, IAM-gated, rotation +
   cross-account support). Access via IAM (IRSA/instance roles where the workload is on AWS,
   OIDC federation otherwise).
2. **Live demo (Phase 3)** = **Render's native secret env vars / secret files** — no external
   store — because the backend is on Render, not AWS.
3. **Local** = `keycloak/ciam-realm.json` + `.env.example` (non-sensitive stand-ins only).
4. **Scope:** only client secrets + (future) DB credentials live in the store. Tokens are
   **never stored** (signed ≠ encrypted — ADR-001).
5. **Runtime:** secrets are injected at boot as env/config; rotated centrally so IdP migration
   stays a config change, not a code change (ADR-007 §IdP migration).
6. This ADR **supersedes the generic "Vault" placeholder** in ADR-007 and risk-register R-3.

## Consequences

- **Pros:** names a concrete, interview-credible product; the resource server stays
  secretless; the demo path works on Render unchanged; IdP migration stays config-only.
- **Cons:** AWS Secrets Manager pays off once the workload is on AWS (or federated from Render);
  for the prototype it remains a documented **non-goal** (REQUIREMENTS §5).

## Alternatives considered

- **HashiCorp Vault** (the earlier placeholder) — rejected: self-hosted/heavier to run; AWS
  Secrets Manager is managed with IAM + automatic rotation and fits an AWS landing zone.
- **Render-native secrets only** — kept for the demo, but insufficient for prod (no cross-service
  rotation, audit, or programmatic access).
- **AWS SSM Parameter Store** — rejected as the primary store: fine for flat values, but
  Secrets Manager adds rotation + cross-account + lifecycle.
- **Hardcoded secrets** — rejected everywhere (C-5); local stand-in only.
