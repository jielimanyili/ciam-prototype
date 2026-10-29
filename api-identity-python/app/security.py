"""JWT validation for the Python resource server.

Mirrors ``api-identity-jvm``'s Spring Security resource-server config:
- RS256 signature via JWKS (auto-discovered from the issuer — Spring's ``issuer-uri``)
- issuer check
- audience check (the one Spring does NOT do by default → ``AudienceValidator``)
- expiry (checked by PyJWT)

The JWKS URI is discovered with the SDK's ``discover()``, so the resource server
demonstrates consuming ``sdk-identity-python`` (the FR-6 "reusable SDK" story).
"""
import os
from typing import Dict

import jwt
from fastapi import Depends, HTTPException
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jwt import PyJWKClient

from ciam_identity import discover

ISSUER = os.environ.get("OIDC_ISSUER", "http://localhost:8080/realms/ciam-demo")
AUDIENCE = os.environ.get("OIDC_AUDIENCE", "api-identity-python")

_bearer = HTTPBearer(auto_error=False)
_jwks = None


def _jwks_client() -> PyJWKClient:
    global _jwks
    if _jwks is None:
        _jwks = PyJWKClient(discover(ISSUER).jwks_uri)
    return _jwks


def get_claims(cred: HTTPAuthorizationCredentials = Depends(_bearer)) -> Dict:
    if cred is None:
        raise HTTPException(status_code=401, detail="Missing bearer token")
    try:
        key = _jwks_client().get_signing_key_from_jwt(cred.credentials)
        return jwt.decode(
            cred.credentials,
            key.key,
            algorithms=["RS256"],
            issuer=ISSUER,
            audience=AUDIENCE,
        )
    except jwt.PyJWTError as exc:
        raise HTTPException(status_code=401, detail=f"Invalid token: {exc}")
