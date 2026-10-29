"""OAuth 2.0 Client Credentials grant — machine-to-machine token fetch.

Python counterpart of the JVM SDK's ``ClientCredentials.fetch()``.
"""
from dataclasses import dataclass
from typing import Optional

import httpx


class ClientCredentialsError(RuntimeError):
    """Raised when the client-credentials token request fails."""


@dataclass(frozen=True)
class TokenResponse:
    access_token: str
    token_type: str
    expires_in: int
    id_token: Optional[str] = None
    refresh_token: Optional[str] = None
    scope: Optional[str] = None


def fetch_client_credentials(
    token_endpoint: str,
    client_id: str,
    client_secret: str,
    scope: Optional[str] = None,
    timeout: float = 10.0,
) -> TokenResponse:
    data = {"grant_type": "client_credentials"}
    if scope:
        data["scope"] = scope

    resp = httpx.post(
        token_endpoint,
        data=data,
        auth=(client_id, client_secret),  # HTTP Basic
        timeout=timeout,
    )
    if resp.status_code != 200:
        raise ClientCredentialsError(f"client_credentials failed ({resp.status_code}): {resp.text}")

    body = resp.json()
    return TokenResponse(
        access_token=body["access_token"],
        token_type=body["token_type"],
        expires_in=body["expires_in"],
        id_token=body.get("id_token"),
        refresh_token=body.get("refresh_token"),
        scope=body.get("scope"),
    )
