# Threat Model (STRIDE)

Scope: the CIAM prototype — SPA/mobile → API gateway → resource server → IdP → enterprise systems.

## Assets
- User credentials + PII
- Access / refresh / ID tokens
- Token-signing keys (IdP)
- Client secrets + DB credentials

## Trust boundaries
1. Browser/mobile ↔ public internet
2. Client ↔ API gateway
3. Gateway ↔ resource server
4. Resource server ↔ IdP (JWKS / discovery)
5. Resource server ↔ enterprise systems (service-to-service)

## Threats & mitigations

| # | STRIDE | Example attack | Mitigation |
|---|---|---|---|
| S1 | Spoofing (user) | Phishing login / impersonation | MFA, passkeys, phishing-resistant auth |
| S2 | Spoofing (service) | Forged/expired token | RS256/JWKS + issuer + audience verification |
| T1 | Tampering | Modify JWT claims | Signed JWT (RS256); reject unsigned/malformed |
| T2 | Tampering (request) | Replay an authorization code | PKCE + one-time code + `state`/`nonce` |
| R1 | Repudiation | Deny a privileged action | Audit log with token `sub`/`jti` on every request |
| I1 | Info disclosure | Read token payload | Signed ≠ encrypted → no secrets in tokens; TLS everywhere |
| I2 | Info disclosure | XSS reads a stored token | No localStorage tokens; CSP; HttpOnly cookies |
| D1 | Denial of service | Credential stuffing / token flood | Rate limiting, bot detection, short tokens, JWKS caching |
| E1 | Elevation of privilege | Replay a stolen refresh token | Rotation + reuse detection → revoke the whole grant |
| E2 | Elevation of privilege | Over-scoped token | Least-privilege scopes; enforce at resource server |

## Key controls (the four that kill most attacks)
1. **Validate-then-authorize** at every trust boundary (JWKS → issuer/audience/exp → scopes).
2. **PKCE + short-lived tokens + rotation** — the big three against token theft.
3. **Secrets out of tokens** (signed ≠ encrypted).
4. **Log denials** — you can't detect what you don't record.

> This maps 1:1 to the controls implemented/tested in the prototype (the `api-identity-jvm` — JWT validation + scope enforcement), so every mitigation here has working code behind it.
