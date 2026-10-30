# TODO — CIAM live demo (pick up here)

> Last updated: 2026-10-29. AWS + Auth0 accounts are created; the stack is
> Auth0-native and pushed to `main`. Remaining = Auth0 app setup (browser) +
> GitHub secrets + deploy. Full steps: `docs/RUNBOOK-AWS-AUTH0.md`.

## Done ✅

- AWS account `633697459923` (root MFA, `ciam-admin` IAM user + access key, CLI configured).
- Auth0 tenant `jie-ciam.us.auth0.com` (free tier).
- IaC + CI/CD written and validated (`terraform validate` passes, SPA `tsc` clean).
- Code + docs converted Okta → Auth0 (commits `eee7bbf`, `fd430d7`, `01cb2e4`).

## Values (keep handy)

- **Issuer:** `https://jie-ciam.us.auth0.com/`  ← trailing slash matters
- **Audience / API identifier:** `https://api.ciam-prototype`

## Next steps

1. [ ] **Check CI is green** — Actions → CI (`mvn -q test` validates the starter + `api-auth`
       Java changes; there's no local JDK).
2. [ ] **Auth0: create the API** — Applications → APIs → name `api-identity`,
       identifier `https://api.ciam-prototype`, signing **RS256**; Permissions → add **`admin`**.
3. [ ] **Auth0: create the 4 apps** (Applications → Applications → Create Application):
       - `ui-identity` — Single Page Application (Authorization Code + PKCE)
       - `mobile-identity` — Native (PKCE)
       - `api-auth` — Regular Web Application, Token Endpoint Auth = **Post** → copy the **client secret**
       - `ciam-service` — Machine to Machine → authorize the `api-identity` API with **`admin`**
       (redirect URLs are placeholders until the CloudFront URL exists — see step 8.)
4. [ ] **Auth0: demo user** — User Management → Users → `demo@example.com` + password
       (connection `Username-Password-Authentication`).
5. [ ] **GitHub secrets + variables** (Settings → Secrets and variables → Actions):
       - Secrets: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AUTH0_CLIENT_SECRET`
       - Variables: `OIDC_ISSUER` = `https://jie-ciam.us.auth0.com/`, `OIDC_AUDIENCE` = `https://api.ciam-prototype`
6. [ ] **Deploy** — Actions → Deploy (AWS) → Run workflow.
7. [ ] **Get the URL** — `terraform -chdir=terraform output -raw cloudfront_domain_name` (→ `<cloudfront>`).
8. [ ] **Wire redirect URLs** in Auth0 (once `<cloudfront>` is known):
       - `ui-identity`: callback + logout + web origin = `https://<cloudfront>/` (+ `http://localhost:4200/` for local)
       - `api-auth`: callback = `https://<cloudfront>/auth/login/oauth2/code/ciam`
       - `mobile-identity`: callback = `mobileidentity://callback` + `http://localhost:8081`
9. [ ] **Verify** (runbook §3.4):
       - `curl https://<cloudfront>/jvm/health` → `{"ok":true}`
       - `curl https://<cloudfront>/python/health` → `{"ok":true}`
       - `curl https://<cloudfront>/auth/actuator/health` → `{"status":"UP"}`
       - open `https://<cloudfront>/` → sign in (Auth0 demo user) → "Call /api/me"
       - M2M: `ciam-service` client-credentials → `/jvm/api/admin` (scope `admin`)
10. [ ] **Teardown** when not demoing — `terraform destroy` (ADR-016).

## Optional / later

- [ ] SDK publish accounts (Sonatype OSSRH + PyPI) — runbook Part 4 / Epic 3.
- [ ] Auth0 Actions/Hooks demo (the Okta-hooks-equivalent) — backlog.
