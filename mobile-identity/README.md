# mobile-identity

React Native (Expo) client — Authorization Code + PKCE, mirroring `ui-identity` on mobile.
See [DESIGN §2](../docs/DESIGN.md) and [ADR-003](../docs/adr/adr-003-react-native-expo.md).

## Run

```bash
cd mobile-identity
npm install
npx expo start          # Expo Go (scan the QR) — or:
npx expo start --web    # browser preview at http://localhost:8081
```

Pre-requisites: the IdP (Keycloak) and a resource server must be running —
`docker compose up -d` (Keycloak) + `api-identity-jvm` on 4101 (or `api-identity-python` on 4102).

## Auth flow

- **Login** → `expo-auth-session` runs Authorization Code + PKCE (S256) against the Keycloak
  `mobile-identity` client.
- **Token storage** → refresh token in `expo-secure-store` (Keychain/Keystore); access token in
  memory only.
- **Call** → `GET /api/me` with the bearer token (default `http://localhost:4101`).
- **Sign out** → clears the access token + refresh token.

## Redirect URIs (register in Keycloak)

| Mode | Redirect URI |
|---|---|
| Web preview | `http://localhost:8081/*` |
| Standalone build | `mobileidentity://callback` |
| Expo Go (dev) | dynamic `exp://<host>:8081/--/callback` — add manually to the Keycloak client |

> Expo Go's host is dynamic, so its `exp://` redirect can't be pre-registered in the realm JSON —
> prefer `npx expo start --web` for a reproducible demo, or add the URL manually.
