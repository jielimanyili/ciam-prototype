"""OIDC discovery (RFC 8414) — fetch the provider's /.well-known/openid-configuration.

Python counterpart of the JVM SDK's ``OidcDiscovery.discover()``.
"""
from dataclasses import dataclass
from typing import Optional

import httpx


@dataclass(frozen=True)
class OidcMetadata:
    issuer: str
    authorization_endpoint: str
    token_endpoint: str
    jwks_uri: str
    end_session_endpoint: Optional[str] = None


def discover(issuer: str, timeout: float = 10.0) -> OidcMetadata:
    base = issuer.rstrip("/")
    resp = httpx.get(f"{base}/.well-known/openid-configuration", timeout=timeout)
    resp.raise_for_status()
    data = resp.json()
    return OidcMetadata(
        issuer=data["issuer"],
        authorization_endpoint=data["authorization_endpoint"],
        token_endpoint=data["token_endpoint"],
        jwks_uri=data["jwks_uri"],
        end_session_endpoint=data.get("end_session_endpoint"),
    )
