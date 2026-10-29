# Terraform — AWS live demo (M3)

Deploys the four-runtime demo on AWS per [ADR-016](../docs/adr/adr-016-aws-deployment.md) and
injects secrets per [ADR-019](../docs/adr/adr-019-secrets-injection-aws.md).

## What it creates

| File | Resources |
|---|---|
| `vpc.tf` | VPC + 2 public subnets + IGW + route table (demo uses public subnets — no NAT cost) |
| `ecr.tf` | ECR repositories for `api-identity-jvm`, `api-identity-python`, `api-auth` |
| `ec2.tf` | **Free-tier EC2 host** — one `t3.micro` (750 free h/month) registered to ECS |
| `ecs*.tf` | ECS cluster, EC2-launch-type task definitions + services, CloudWatch log groups, task SG |
| `alb.tf` | ALB + target groups + HTTPS listener with **host-based routing** (`api.` / `api-py.` / `auth.`) |
| `acm.tf` + `route53.tf` | ACM wildcard cert (DNS-validated) + ALIAS records |
| `s3-cloudfront.tf` | S3 (private) + CloudFront (OAC) for `ui-identity`, SPA 404 fallback |
| `secrets.tf` | Secrets Manager secret for `api-auth`'s `CLIENT_SECRET` |
| `iam.tf` | Task execution role (ECR pull + `secretsmanager:GetSecretValue`, least privilege) + task role |

## Prerequisites

- AWS account + credentials (or a GitHub OIDC role for CI).
- A Route53 hosted zone (`route53_zone_id`) and a domain (`domain_name`).
- An Okta dev tenant (`oidc_issuer`) and the `api-auth` client secret.

## Cost (free-tier by design)

- **Compute = $0:** one `t3.micro` EC2 instance (750 free h/month) runs all three backends via ECS.
  The JVM apps are memory-tuned (`JAVA_TOOL_OPTIONS=-Xmx192m`) to fit the 1 GB instance. If it ever
  OOMs, bump `instance_type` to `t3.small`/`medium` (~$15–25/month) — the only paid change.
- **ALB / S3 / CloudFront / ECR / CloudWatch = free tier.**
- **Residual (~$1/month):** Secrets Manager ($0.40/secret) + Route53 hosted zone ($0.50) + the
  domain itself (~$12/yr). These are not free-tier, but are negligible.

## Apply

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars   # fill in real values — never commit it
terraform init
terraform plan
terraform apply
```

Then build + push the images (tag must match `image_tag`):

```bash
export IMAGE_TAG=latest
docker build -t "$ECR_JVM/api-identity-jvm:$IMAGE_TAG"        -f api-identity-jvm/Dockerfile .
docker build -t "$ECR_PY/api-identity-python:$IMAGE_TAG"      -f api-identity-python/Dockerfile .
docker build -t "$ECR_AUTH/api-auth:$IMAGE_TAG"               -f api-auth/Dockerfile ./api-auth
# docker push each image to its ECR repo
```

And upload the SPA (`dist/ui-identity/browser/` → the S3 bucket):

```bash
aws s3 sync ui-identity/dist/ui-identity/browser s3://<spa-bucket> --delete
```

## CI/CD

`.github/workflows/deploy.yml` (main only) runs the same steps with OIDC AWS auth. Required
GitHub config: secret `AWS_ROLE_ARN` (OIDC role) + `OKTA_CLIENT_SECRET`; variables
`DOMAIN_NAME`, `ROUTE53_ZONE_ID`, `OIDC_ISSUER`.

## Teardown

```bash
terraform destroy   # tear down when not demoing (ADR-016 consequence)
```
