# Runbook — AWS + Auth0 account setup (M3 live demo)

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

### 1.3 GitHub Actions credentials (long-lived keys)

> **Note:** this account is a member of an AWS Organization whose Service Control Policy denies
> `iam:CreateOpenIDConnectProvider`, so the GitHub OIDC federation path can't be used here. The demo
> falls back to long-lived access keys. (`infra/github-oidc/` remains for accounts without that SCP;
> `iam:CreateRole` is **not** blocked, so Terraform works fine.)

1. The `ciam-admin` user already has an access key from §1.2. Keep the **Access Key ID** +
   **Secret Access Key** handy — they become GitHub secrets in §3.2.

### 1.4 No domain needed — CloudFront's free URL

The stack serves everything from one free **`*.cloudfront.net`** URL (no domain purchase, no ACM,
no Route53). After the first `terraform apply`, run `terraform output cloudfront_domain_name` to get
it — you'll paste it into Auth0 as the redirect URI in Part 2.

---

## Part 2 — Auth0 tenant

### 2.1 Sign up (browser)

1. Go to **https://auth0.com/signup** → *For myself* → free tenant (no card). Okta's free plan now
   requires a corporate email, so the demo uses Auth0 instead — CIAM-native and Gmail-friendly.
2. Pick a tenant domain like **`jie-ciam.us.auth0.com`** (can't be changed later) and region `US`.

Your OIDC issuer is your tenant URL **with a trailing slash** (Auth0's `iss` claim carries it):

```
https://jie-ciam.us.auth0.com/
```

Its access-token audience is the **API identifier** you create in §2.3 (`https://api.ciam-prototype`).

### 2.2 Create the applications (Applications → Applications → Create Application)

> `<cloudfront>` = `terraform output cloudfront_domain_name` (run `terraform apply` in Part 3 first
> if you don't have it yet — the issuer from §2.1 is enough to apply).

| App | Type | Grant | Key settings |
|---|---|---|---|
| `ui-identity` | **Single Page Application** | Authorization Code **+ PKCE** | Allowed Callback `https://<cloudfront>/` (+ `http://localhost:4200/`); Allowed Logout `https://<cloudfront>/`; Allowed Web Origins `https://<cloudfront>/` |
| `mobile-identity` | **Native** | Authorization Code **+ PKCE** | Allowed Callback `mobileidentity://callback` + `http://localhost:8081` |
| `api-auth` | **Regular Web Application** | Authorization Code | Token Endpoint Auth = **Post**; Allowed Callback `https://<cloudfront>/auth/login/oauth2/code/ciam` (+ `http://localhost:4103/login/oauth2/code/ciam`); copy the **Client Secret** |
| `ciam-service` | **Machine to Machine** | Client Credentials | Authorized for the `api-identity` API with the **`admin`** scope |

### 2.3 API (audience) + `admin` scope (ADR-013)

- **Applications → APIs → Create API** → name `api-identity`, **Identifier** `https://api.ciam-prototype`,
  signing **RS256**. That identifier is the value for `oidc_audience_jvm` / `oidc_audience_python`
  and `OIDC_AUDIENCE`.
- Add an **`admin`** permission: API → **Permissions → Add** → `admin` (used by `ciam-service` → `/api/admin`).
- The SPA + BFF request this audience in the authorize call, so their access tokens carry
  `aud = https://api.ciam-prototype`.

### 2.4 Demo user

User Management → **Users → Create User** → `demo@example.com` (connection
`Username-Password-Authentication`) → set a password. This is the account you sign in with.

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
| `oidc_issuer` | `https://jie-ciam.us.auth0.com/` (trailing slash) |
| `oidc_audience_jvm` / `oidc_audience_python` | `https://api.ciam-prototype` |
| `api_auth_client_secret` | the `api-auth` client secret from Auth0 |
| `saml_enabled` | `false` (OIDC-only demo; SAML stays a Keycloak-local feature — Epic 2) |

### 3.2 GitHub secrets + variables

Repo → **Settings → Secrets and variables → Actions**.

**Secrets:**

| Name | Value |
|---|---|
| `AWS_ACCESS_KEY_ID` | the `ciam-admin` access key id (from §1.2) |
| `AWS_SECRET_ACCESS_KEY` | the `ciam-admin` secret access key |
| `AUTH0_CLIENT_SECRET` | the `api-auth` client secret |

**Variables (repo-level):**

| Name | Value |
|---|---|
| `OIDC_ISSUER` | `https://jie-ciam.us.auth0.com/` (trailing slash) |
| `OIDC_AUDIENCE` | `https://api.ciam-prototype` |

### 3.3 Deploy

**Option A — CI (recommended):** push to `main` (or *Actions → Deploy (AWS) → Run workflow*).
`deploy.yml` applies Terraform, builds + pushes the 3 images to ECR, syncs the SPA to S3, and rolls ECS.

**Option B — manual:** see `terraform/README.md` (apply + `docker build/push` + `aws s3 sync`).

### 3.4 Verify

```bash
CLOUDFRONT=$(terraform -chdir=terraform output -raw cloudfront_domain_name)
curl "https://$CLOUDFRONT/jvm/health"            # {"ok":true}
curl "https://$CLOUDFRONT/python/health"         # {"ok":true}
curl "https://$CLOUDFRONT/auth/actuator/health"  # {"status":"UP"}
open "https://$CLOUDFRONT/"                      # Sign in → Auth0 (demo user) → "Call /api/me"
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
