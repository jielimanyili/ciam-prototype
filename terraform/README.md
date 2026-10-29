# Terraform — AWS live demo (M3)

Deploys the four-runtime demo on AWS per [ADR-016](../docs/adr/adr-016-aws-deployment.md) and
injects secrets per [ADR-020](../docs/adr/adr-020-free-secret-storage-ssm.md).

## What it creates

| File | Resources |
|---|---|
| `vpc.tf` | VPC + 2 public subnets + IGW + route table (demo uses public subnets — no NAT cost) |
| `ecr.tf` | ECR repositories for `api-identity-jvm`, `api-identity-python`, `api-auth` |
| `ec2.tf` | **Free-tier EC2 host** — one `t3.micro` (750 free h/month) registered to ECS |
| `ecs*.tf` | ECS cluster, EC2-launch-type task definitions + services, CloudWatch log groups, task SG |
| `alb.tf` | ALB + target groups + HTTP listener with **path-based routing** (`/jvm/*` `/python/*` `/auth/*`) |
| `s3-cloudfront.tf` | S3 (private) + CloudFront — single free `*.cloudfront.net` HTTPS front door (SPA + API paths) |
| `secrets.tf` | SSM Parameter Store `SecureString` for `api-auth`'s `CLIENT_SECRET` (ADR-020) |
| `iam.tf` | Task execution role (ECR pull + `ssm:GetParameter` + `kms:Decrypt`, least privilege) + task role |

## Prerequisites

- AWS account + credentials (or a GitHub OIDC role for CI).
- An Okta dev tenant (`oidc_issuer`) and the `api-auth` client secret.

## Cost (free-tier by design)

- **Compute = $0:** one `t3.micro` EC2 instance (750 free h/month) runs all three backends via ECS.
  The JVM apps are memory-tuned (`JAVA_TOOL_OPTIONS=-Xmx192m`) to fit the 1 GB instance. If it ever
  OOMs, bump `instance_type` to `t3.small`/`medium` (~$15–25/month) — the only paid change.
- **ALB / S3 / CloudFront / ECR / CloudWatch = free tier.**
- **No domain, no ACM, no Route53** — HTTPS comes from CloudFront's free `*.cloudfront.net` cert.
- **Fully $0** — secrets use SSM Parameter Store (Standard tier is free, ADR-020); production
  would switch to Secrets Manager (ADR-019).

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
