# sdk-identity-python

The **Python** counterpart of [`sdk-identity-jvm`](../sdk-identity-jvm) — a thin, opinionated
identity library so product squads get safe OIDC/OAuth 2.0 defaults without re-deriving them.

JWT *validation* deliberately lives in the resource server (mirroring the JVM SDK, where Spring
Security does validation and the SDK standardizes everything *around* it). See
[ADR-011](../docs/adr/adr-011-python-sdk-and-resource-server.md).

## Java ↔ Python mapping

| JVM (`sdk-identity-jvm`) | Python (`sdk-identity-python`) |
|---|---|
| `Pkce.generate()` | `generate_pkce()` |
| `OidcDiscovery.discover(issuer)` | `discover(issuer)` |
| `ClientCredentials.fetch(tokenEndpoint, id, secret, scope)` | `fetch_client_credentials(token_endpoint, id, secret, scope)` |
| `ScopeAssertions.scopesOf(claims)` | `scopes_of(claims)` |
| `ScopeAssertions.requireScope(claims, "admin")` | `require_scope(claims, "admin")` |
| `ScopeAssertions.hasClaim(claims, key, value)` | `has_claim(claims, key, value)` |

## Install + test

```bash
python3 -m venv .venv && source .venv/bin/activate
pip install -e ".[dev]"
pytest
```
