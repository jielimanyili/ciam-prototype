# ADR-016 — AWS deployment target (supersedes ADR-005 hosts)

- **Status:** Accepted
- **Date:** 2026-10-29
- **Serves:** NFR-4, NFR-6 · supersedes ADR-005 (host choice)

## Context

ADR-005 chose Render (backend) + Vercel (frontend) for the live demo — simple free tiers. But the
reference targets TriNet, which runs on AWS/OCI, so Render/Vercel under-sell production depth. AWS
deployment is the stronger signal and covers a second cloud (alongside the GCP FHIR demo).

## Decision

Deploy the four-runtime demo on **AWS**:

- **Frontend** (`ui-identity`) → **S3 + CloudFront** (static, HTTPS via ACM).
- **Backends** (`api-identity-jvm`, `api-identity-python`) → **ECS Fargate** behind an **ALB**.
- **Images** → **ECR** (demo) / **JFrog** (enterprise, ADR-017).
- **Secrets** → **AWS Secrets Manager** (ADR-008).
- **IaC** → **Terraform** (reuses the author's FHIR-project Terraform depth).
- **IdP** → Okta dev tenant (SaaS — unchanged).

## Consequences

- **Pros:** production-real signal; matches TriNet's AWS/OCI; GCP + AWS breadth across the two demos.
- **Cons:** more setup + cost than Render/Vercel (Fargate + ALB); teardown required when not demoing.

## Alternatives considered

- **Render + Vercel (ADR-005)** — superseded: demo convenience, weaker signal.
- **App Runner** — rejected: simpler, but ECS Fargate matches TriNet's AWS ECS pattern.
- **EKS** — rejected: Kubernetes overhead for four services.
