# Keycloak (local OIDC provider)

`docker-compose.yml` starts a Keycloak 26 instance with the `ciam-demo` realm
imported from `ciam-realm.json`. It stands in for Okta so the prototype runs
end-to-end with no external accounts. Swap to Okta by changing the issuer +
client config (see the root `README.md`).

## What the realm provides

| Client | Type | Grant | Secret |
|---|---|---|---|
| `ui-identity` | public (SPA) | Authorization Code + PKCE | — |
| `mobile-identity` | public (mobile) | Authorization Code + PKCE | — |
| `api-auth` | confidential (BFF) | Authorization Code | `api-auth-secret` |
| `ciam-service` | confidential | Client Credentials | `service-secret` |

- Demo user: `demo` / `demo`
- Admin console: http://localhost:8080 (user `admin` / `admin`)

## Run

```bash
docker compose up -d
# wait ~30s for Keycloak to boot, then:
curl http://localhost:8080/realms/ciam-demo/.well-known/openid-configuration
```

## Manual setup (fallback if the realm import needs adjustment)

1. Open http://localhost:8080, sign in as `admin`/`admin`.
2. Create realm `ciam-demo`.
3. Create client `ui-identity`:
   - Client type: **OpenID Connect** · **Public client** ON
   - Valid redirect URIs: `http://localhost:4200/*` · Web origins: `http://localhost:4200`
   - Advanced → Proof Key for Code Exchange (PKCE) Code Challenge Method: **S256**
   - Client scopes → `ui-identity-dedicated` → **Add mapper → Audience** — Included Custom Audience `api-identity-jvm` (add to access token); repeat for `api-identity-python`
4. Create client `mobile-identity`:
   - Client type: **OpenID Connect** · **Public client** ON
   - Valid redirect URIs: `http://localhost:8081/*` · `mobileidentity://callback`; Web origins: `http://localhost:8081`
   - Advanced → Proof Key for Code Exchange (PKCE) Code Challenge Method: **S256**
   - Client scopes → `mobile-identity-dedicated` → **Add mapper → Audience** — `api-identity-jvm` + `api-identity-python`
5. Create client `api-auth`:
   - Client type: **OpenID Connect** · **Client authentication** ON (confidential)
   - Credentials tab → set client secret `api-auth-secret`
   - Valid redirect URIs: `http://localhost:4103/login/oauth2/code/*`; Web origins: `http://localhost:4103`
   - Client scopes → `api-auth-dedicated` → **Add mapper → Audience** — Included Custom Audience `api-identity-jvm`
6. Create client `ciam-service`:
   - Client type: **OpenID Connect** · **Client authentication** ON
   - Credentials tab → set client secret `service-secret`
   - **Service accounts roles** ON
   - Client scopes → add a client scope `admin` (with `include in token scope` ON)
   - Client scopes → `ciam-service-dedicated` → **Add mapper → Audience** — Included Custom Audience `api-identity-jvm`
7. Create user `demo` with password `demo`.

> Note: `ciam-realm.json` is a best-effort import; if Keycloak warns on import,
> use the manual steps above — they are the guaranteed path.
