# ADR-019 — Secrets injection on AWS (ECS Fargate)

- **Status:** Accepted
- **Date:** 2026-10-29
- **Serves:** C-5, NFR-1 · supersedes ADR-008 decision #2 (Render demo path)

## Context

ADR-008 chose AWS Secrets Manager for production but Render-native env vars for the live demo —
because ADR-005 hosted the demo on Render. ADR-016 later moved the demo to AWS (ECS Fargate), so
the "demo isn't on AWS" rationale no longer holds. On AWS, the demo should use Secrets Manager the
same way production would — it turns a documented non-goal into a demoable capability.

## Decision

- **Demo + prod both use AWS Secrets Manager.** Secrets are defined in Terraform
  (`aws_secretsmanager_secret` + `aws_secretsmanager_secret_version`) and injected into ECS Fargate
  via the task-definition `secrets` block (`name` + `valueFrom` ARN).
- **App stays secretless + cloud-agnostic:** the app reads the value from an env var
  (`CLIENT_SECRET`), never from AWS — so the only code change is
  `client-secret: ${CLIENT_SECRET:api-auth-secret}`. The IdP/client secret lives in Secrets Manager;
  the app holds no secret.
- **IAM:** the ECS **task execution role** gets `secretsmanager:GetSecretValue` scoped to the secret
  ARN (least privilege — NFR-2).
- **Rotation:** rotate centrally in Secrets Manager; ECS restarts pick up the new value (config-only,
  ADR-008 decision #5).

## Consequences

- **Pros:** the demo demonstrates the real production secret path (a TriNet signal); app stays
  agnostic; least-privilege IAM.
- **Cons:** requires the ECS task role + a real secret value at deploy time.

## Alternatives considered

- **`spring-cloud-aws` (`spring.config.import=aws-secretsmanager:…`)** — rejected: couples the app
  to AWS; the env-injection pattern keeps the app provider-agnostic.
- **Render-native env vars (ADR-008 #2)** — superseded: the demo is on AWS now.
- **Hardcoded secrets** — rejected (C-5).
