# ADR-020 — Free-tier secret storage: SSM Parameter Store for the demo

- **Status:** Accepted
- **Date:** 2026-10-29
- **Serves:** C-5, NFR-1 · supersedes ADR-019 (demo path)

## Context

ADR-019 chose AWS Secrets Manager for **both** demo and prod. The demo's explicit goal is to run
entirely inside the AWS Free Tier, and Secrets Manager ($0.40/secret/month) is the last item that
isn't free. AWS Systems Manager **Parameter Store** (Standard tier) stores `SecureString`
parameters for **$0**, and the ECS task-definition `secrets` block supports both backends — so the
app-facing behavior (the secret arrives as the `CLIENT_SECRET` env var; the app stays secretless) is
unchanged.

## Decision

- **Demo secret store = SSM Parameter Store (Standard tier, `SecureString`)** — free.
- **Production secret store = AWS Secrets Manager** (unchanged) — automatic rotation, cross-account
  sharing, and lifecycle features the demo doesn't need.
- The ECS `secrets` block `valueFrom` points at the SSM parameter ARN; the app still reads
  `CLIENT_SECRET` from an env var (ADR-019's injection pattern preserved — the app holds no secret).
- **IAM:** the ECS task execution role gets `ssm:GetParameter`/`ssm:GetParameters` on the parameter
  ARN and `kms:Decrypt` on the AWS-managed `alias/aws/ssm` key (least privilege — NFR-2).

## Consequences

- **Pros:** the demo is now fully free-tier ($0); the secret-injection pattern and the app are unchanged.
- **Cons:** SSM Standard lacks Secrets Manager's automatic rotation + cross-account sharing — those
  are production concerns, and prod keeps Secrets Manager.

## Alternatives considered

- **Secrets Manager (ADR-019)** — superseded for the demo: $0.40/secret/month breaks the "fully
  free-tier" goal with no demo-visible benefit.
- **Plain env var in the task definition / Terraform state** — rejected: leaks the secret into the
  ECS console + state (violates C-5 / ADR-008).
- **GitHub Actions secret → env** — rejected: couples runtime to the CI tool and still lands in the
  task definition.
