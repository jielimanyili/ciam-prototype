# ADR-022 — Render/Vercel fallback for the live demo (AWS blocked by org SCP)

- **Status:** Accepted
- **Date:** 2026-10-30
- **Serves:** NFR-4, NFR-6 · fallback for ADR-016 (AWS host)

## Context

ADR-016 chose AWS (EC2 + ECS + S3/CloudFront) for the live demo. At deploy time, every AWS account
the author can create lands in an AWS Organization whose Service Control Policy denies the compute
stack — `ec2:Describe*`, `ecs:List*`, `ecr:Describe*`, `elasticloadbalancing:Describe*`, `kms:List*`,
`ssm:GetParameter` — while allowing only S3 / CloudFront / IAM / CloudWatch Logs / STS. The account
was auto-placed on a "paid plan with a select set of AWS services" (signup flagged the author as an
existing customer), and the org's management account is not reachable from the author's Builder-ID
login. AWS support case `179339225800653` is open, but resolution is not guaranteed.

## Decision

Add a **Render + Vercel fallback** (free tiers) so the demo can go live regardless of the AWS outcome:

- **Backends** (`api-identity-jvm`, `api-identity-python`, `api-auth`) → **Render** free web
  services (Docker), declared in a root `render.yaml` blueprint.
- **Frontend** (`ui-identity`) → **Vercel** (or Render static) — static build whose
  `assets/config.json` points at the deployed Render URLs.
- Same Auth0 tenant + apps (issuer `https://jie-ciam.us.auth0.com/`, audience
  `https://api.ciam-prototype`); only host URLs + Auth0 redirect URIs change.

The AWS Terraform path (ADR-016) remains the preferred target if/when the SCP is lifted.

## Consequences

- **Pros:** free tiers; zero code change — the services are provider-agnostic (Dockerfiles,
  `CONTEXT_PATH` defaults to empty, and the SPA already reads a full `apiBaseUrl` URL).
- **Cons:** Render free services sleep after idle (cold starts); no single shared domain (each
  service gets its own `*.onrender.com` URL) unless a custom domain is added; weaker "production
  signal" than AWS.

## Alternatives considered

- **Wait for AWS to lift the SCP** — in progress (support case), not a guaranteed outcome.
- **Other PaaS (Railway, Fly.io)** — rejected: Render is the simplest Docker + static deploy.
- **Self-host (VPS + Docker Compose)** — rejected: more ops burden for a demo.