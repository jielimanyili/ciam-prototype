# CIAM Anti-Patterns

The "don't do this" list — each one is a red flag a Principal interviewer watches for. For each: **what it is → why it's bad → what to do instead.**

## 1. Implicit flow / tokens in the URL
Tokens in the fragment leak via browser history, referrers, and logs. → Use **Authorization Code + PKCE**.

## 2. Resource-owner password grant for users
Collecting credentials in your own form to exchange = phishing surface + breaks MFA. → **Redirect to the IdP**.

## 3. Tokens in localStorage
Any XSS can read them. → Access token **in memory**; refresh token in an **HttpOnly** cookie; or a BFF.

## 4. Long-lived, non-rotating tokens
A stolen token stays valid = a skeleton key. → **Short expiry + rotation + reuse detection**.

## 5. Symmetric (HS256) validation everywhere
A shared secret copied to every service = one leak breaks all. → **RS256 + JWKS**.

## 6. Secrets in tokens / in CIAM
Tokens are **signed, not encrypted** — anyone can read the payload. → DB passwords / API keys live in a **secrets manager**.

## 7. Trusting the client / client-side-only checks
`if (role === 'admin')` in the UI is theater. → **Enforce at the resource server**.

## 8. Over-scoped tokens (grant-all)
A `*` scope means any breach escalates everywhere. → **Least privilege** per client and flow.

## 9. Hand-rolling crypto / building your own IdP
It always ends in a CVE. → Use a **vetted IdP + libraries** (jose, Spring Security).

## 10. Treating CIAM like workforce IAM
Missing self-registration, consent, social login, and customer scale. → **Design for external customers**.

## 11. Ignoring consent & privacy
GDPR/CCPA fines + lost trust. → **Consent + data minimization + right-to-be-forgotten**.

## 12. Big-bang auth migration
No rollback, high risk. → **Strangler pattern**: dual-run + feature flags.

## 13. No refresh rotation / no revocation
You can't kill a stolen session. → **Rotation + revocation endpoints**.

## 14. Conflating scopes / claims / roles
Leads to over- or under-permissioning. → **Scopes = permissions · claims = facts · roles = groupings**.

## 15. No audit logging of authz denials
You can't detect an attack you don't record. → **Log every 401/403** with context.

---

> A strong interview answer leads with *judgment*: "the mistake isn't using OIDC wrong — it's using it without PKCE, rotation, and least privilege." This list is that judgment, made explicit.
