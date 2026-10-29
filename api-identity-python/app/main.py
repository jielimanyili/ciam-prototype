"""FastAPI resource server — mirrors ``api-identity-jvm`` (Spring Boot).

Endpoints are intentionally identical to the Java server (optionally under a
``CONTEXT_PATH`` prefix when fronted by CloudFront path-based routing):
- GET /health       public
- GET /api/me       authenticated (returns token claims)
- GET /api/admin    requires the ``admin`` scope
- GET /api/claims   authenticated (scope / roles / groups / client_id)
"""
import os

from fastapi import Depends, FastAPI, Request
from fastapi.responses import JSONResponse

from ciam_identity import InsufficientScopeError, require_scope

from .security import get_claims

PREFIX = os.environ.get("CONTEXT_PATH", "")

app = FastAPI(title="api-identity-python", version="0.1.0")


@app.exception_handler(InsufficientScopeError)
async def _insufficient_scope_handler(request: Request, exc: InsufficientScopeError):
    return JSONResponse(status_code=403, content={"detail": str(exc)})


@app.get(PREFIX + "/health")
def health():
    return {"ok": True}


@app.get(PREFIX + "/api/me")
def me(claims: dict = Depends(get_claims)):
    return {"claims": claims}


@app.get(PREFIX + "/api/admin")
def admin(claims: dict = Depends(get_claims)):
    require_scope(claims, "admin")
    return {"message": "welcome, admin"}


@app.get(PREFIX + "/api/claims")
def claims(claims: dict = Depends(get_claims)):
    def _as_list(value):
        if value is None:
            return None
        return value if isinstance(value, list) else [value]

    return {
        "scope": claims.get("scope"),
        "roles": _as_list(claims.get("roles")),
        "groups": _as_list(claims.get("groups")),
        "client_id": claims.get("client_id"),
    }
