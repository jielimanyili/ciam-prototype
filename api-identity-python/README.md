# api-identity-python

The **Python** counterpart of [`api-identity`](../api-identity) — a FastAPI resource server that
protects the same endpoints with the same JWT validation (RS256/JWKS + issuer + audience + scope
enforcement), using [`sdk-identity-python`](../sdk-identity-python). See
[ADR-011](../docs/adr/adr-011-python-sdk-and-resource-server.md).

## Run

```bash
# from the repo root — needs the IdP (Keycloak) running:
docker compose up -d

# build + run (installs the SDK from the local path):
docker build -f api-identity-python/Dockerfile -t api-identity-python .
docker run -p 4102:4102 -e OIDC_ISSUER=http://localhost:8080/realms/ciam-demo api-identity-python

# or locally without Docker:
python3 -m venv .venv && source .venv/bin/activate
pip install -e ../sdk-identity-python
pip install -r requirements.txt
uvicorn app.main:app --port 4102
```

Env vars (same as the Java server):

| Var | Default |
|---|---|
| `OIDC_ISSUER` | `http://localhost:8080/realms/ciam-demo` |
| `OIDC_AUDIENCE` | `api-identity` |
| `PORT` | `4102` |

## Tests

```bash
pytest
```
