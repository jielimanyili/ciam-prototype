# Runbook — AWS + Okta account setup (M3 live demo)

The Terraform (`terraform/`) and CI/CD (`.github/workflows/deploy.yml`) are written and validated.
This runbook is the **only remaining step**: create the two accounts and wire them to the repo so
`terraform apply` / the deploy workflow actually runs.

> The only parts I can't automate are the browser signups (email, billing, verification).
> Everything after signup is scripted or copy-paste below.

---

## Part 0 — Prerequisites on your Mac

```bash
brew install awscli          # AWS CLI v2 (terraform is already installed)
aws --version                # expect: aws-cli/2.x
```

---

## Part 1 — AWS account

### 1.1 Sign up (browser)

1. Go to **https://aws.amazon.com/free/** → *Create an AWS Account*.
2. Use your email + a credit card (Free Tier; you won't be charged for this demo if you tear down).
3. After verification, sign in as **root** and **enable MFA** (Security → MFA). Do all work as an IAM user, not root.

### 1.2 Create an admin IAM user + configure the CLI

1. IAM → **Users** → *Create user* → name `ciam-admin` → attach policy **AdministratorAccess**.
2. Security credentials → *Create access key* → copy `Access Key ID` + `Secret Access Key`.
3. Back on your Mac:

```bash
aws configure
# AWS Access Key ID:     <paste>
# AWS Secret Access Key: <paste>
# Default region:        us-east-1
# Default output format: json

aws sts get-caller-identity   # note the 12-digit "Account"
```

### 1.3 Register GitHub Actions OIDC (one command)

This lets CI assume an AWS role with **no long-lived keys** (ADR-018). From the repo root:

```bash
chmod +x infra/github-oidc/setup.sh
export ACCOUNT_ID=<your-12-digit-account-id>
infra/github-oidc/setup.sh
# prints: AWS_ROLE_ARN = arn:aws:iam::<id>:role/github-actions-ciam-deploy
```

It creates: the `token.actions.githubusercontent.com` OIDC provider · a `github-actions-ciam-deploy`
role (trust = only the `jielimanyili/ciam-prototype` repo) · a service-scoped `ciam-deploy` policy
(see `infra/github-oidc/deploy-policy.json`). For a throwaway demo you can instead attach
`AdministratorAccess` to that role — the scoped policy is the better default (NFR-2).

### 1.4 Domain + Route53 (required for HTTPS + host routing)

The stack host-routes `api.` / `api-py.` / `auth.` / `app.` off `domain_name` and needs an ACM cert.

- Buy a domain (**Route53 → Registered domains** ~$12/yr) **or** use a domain you already own.
- Route53 → **Hosted zones** → *Create hosted zone* → note the **Hosted zone ID**.
- If the domain is registered elsewhere, copy the 4 NS records into your registrar.

You now have: `ACCOUNT_ID`, `DOMAIN_NAME`, `ROUTE53_ZONE_ID`.

---

## Part 2 — Okta dev tenant

### 2.1 Sign up (browser)

1. Go to **https://developer.okta.com/signup/** → create a free developer account (no card).
2. You get a domain **`dev-<id>.okta.com`** and an admin console (check your signup email for the URL).

Your OIDC issuer for the `default` authorization server is:

```
https://dev-<id>.okta.com/oauth2/default
```

Its access-token audience is **`api://default`** (that's the value to use for `OIDC_AUDIENCE`).

### 2.2 Create the app integrations (Applications → Create App Integration → OIDC)

| App | Type | Grant | Key settings |
|---|---|---|---|
| `ui-identity` | **Single-Page App (SPA)** | Authorization Code **+ PKCE (S256)** | Sign-in redirect URI `https://app.<domain>/` (and `http://localhost:4200/` for local) |
| `mobile-identity` | **Native** | Authorization Code **+ PKCE (S256)** | Sign-in redirect `mobileidentity://callback` + `http://localhost:8081` |
| `api-auth` | **Web** (confidential) | Authorization Code | Client secret (copy it → `OKTA_CLIENT_SECRET`); redirect `https://auth.<domain>/login/oauth2/code/ciam` (+ `http://localhost:4103/login/oauth2/code/ciam`) |
| `ciam-service` | **Service (Machine-to-Machine)** | Client Credentials | Client secret |

### 2.3 Audience + admin scope (ADR-013)

- **Audience:** the `default` server emits `aud = api://default`. Set `oidc_audience_jvm` and
  `oidc_audience_python` to **`api://default`** in `terraform.tfvars` (or create a custom
  authorization server per API if you want distinct audiences — optional).
- **`admin` scope** (for the `ciam-service` M2M → `/api/admin`): Security → API → **default** →
  *Scopes → Add scope* `admin` → then in the `ciam-service` app, *Okta API Scopes* → grant `admin`.

### 2.4 Demo user

Directory → **People** → *Add person* → `demo@example.com` → set a password (or "Send user
activation email"). This is the account you sign in with during the demo.

---

## Part 3 — Wire the repo to the accounts

### 3.1 Terraform variables

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars   # gitignored — never committed
```

Fill in:

| Var | Value |
|---|---|
| `region` | `us-east-1` |
| `oidc_issuer` | `https://dev-<id>.okta.com/oauth2/default` |
| `oidc_audience_jvm` / `oidc_audience_python` | `api://default` |
| `api_auth_client_secret` | the `api-auth` client secret from Okta |
| `domain_name` | your domain |
| `route53_zone_id` | your hosted-zone id |
| `saml_enabled` | `false` (OIDC-only demo; SAML stays a Keycloak-local feature — Epic 2) |

### 3.2 GitHub secrets + variables

Repo → **Settings → Secrets and variables → Actions**.

**Secrets:**

| Name | Value |
|---|---|
| `AWS_ROLE_ARN` | `arn:aws:iam::<id>:role/github-actions-ciam-deploy` (from §1.3) |
| `OKTA_CLIENT_SECRET` | the `api-auth` client secret |

**Variables (repo-level):**

| Name | Value |
|---|---|
| `DOMAIN_NAME` | your domain |
| `ROUTE53_ZONE_ID` | your hosted-zone id |
| `OIDC_ISSUER` | `https://dev-<id>.okta.com/oauth2/default` |

### 3.3 Deploy

**Option A — CI (recommended):** push to `main` (or *Actions → Deploy (AWS) → Run workflow*).
`deploy.yml` applies Terraform, builds + pushes the 3 images to ECR, syncs the SPA to S3, and rolls ECS.

**Option B — manual:** see `terraform/README.md` (apply + `docker build/push` + `aws s3 sync`).

### 3.4 Verify

```bash
curl https://api.<domain>/health               # {"ok":true}
curl https://api-py.<domain>/health            # {"ok":true}
curl https://auth.<domain>/actuator/health     # {"status":"UP"}
open https://app.<domain>                      # Sign in → Okta (demo user) → "Call /api/me"
```

### 3.5 Teardown (do this when not demoing — ADR-016)

```bash
cd terraform && terraform destroy
```

---

## Part 4 (bonus) — SDK publish accounts (Epic 3 story 5)

The publish workflow needs, separately:

- **Maven Central:** a **Sonatype OSSRH** account (https://central.sonatype.org) → secrets
  `OSSRH_USERNAME`, `OSSRH_PASSWORD`, `GPG_PRIVATE_KEY` (ASCII-armored), `GPG_PASSPHRASE`.
- **PyPI:** a PyPI account + API token → secret `PYPI_API_TOKEN`.

Then push a `v*` tag; `.github/workflows/publish.yml` publishes both SDKs.
