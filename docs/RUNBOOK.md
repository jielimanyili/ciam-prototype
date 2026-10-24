# Runbook — CIAM Prototype

> How to run, verify, troubleshoot, rotate secrets, and respond to incidents. Production observability is a non-goal (`REQUIREMENTS.md §5`), so this stays manual + lightweight.

## Run locally

1. `docker compose up -d` — start Keycloak (`ciam-demo` realm).
2. Verify IdP: `curl http://localhost:8080/realms/ciam-demo/.well-known/openid-configuration`.
3. `cd api-identity && docker build -t api-identity . && docker run -p 4101:4101 api-identity`.
4. `cd ../ui-identity && npm install && npm start` → http://localhost:4200 → `demo` / `demo`.
5. Verify API: `curl http://localhost:4101/health` → `{"ok":true}`.

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| IdP discovery 404 / timeout | Keycloak not booted yet | wait ~30s; `docker compose logs keycloak` |
| 401 on `/api/me` | missing / expired token | re-login; check token expiry |
| 401 `invalid_token — audience missing` | `aud` ≠ `OIDC_AUDIENCE` | set `OIDC_AUDIENCE=api-identity`; re-mint token |
| 403 on `/api/admin` | token lacks the `admin` scope | add the scope to the client / request |
| SPA redirect loop | `client_id` / redirect URI mismatch | check the `ui-identity` client + `auth.service.ts` |

## Secret rotation

- **Keycloak client secret** (`ciam-service`): regenerate in the admin console → update the consumer secret store. (Local stand-in; see `keycloak/README.md`.)
- **Prod**: secrets live in AWS Secrets Manager (ADR-008); rotation = update the secret + restart the service (injected at boot).
- **Never** commit secrets; `.env` is gitignored.

## Incident response

1. **Detect** — 401/403 spikes, authN failures in logs.
2. **Contain** — revoke the affected token/client; rotate secrets.
3. **Remediate** — fix + deploy (roll back via `RELEASING.md` if needed).
4. **Postmortem** — update `threat-model.md` / `risk-register.md`.

Severity = likelihood × impact (see `risk-register.md`). Escalate to the Identity/Auth squad (ADR-010).
