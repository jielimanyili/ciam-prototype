"""CIAM identity SDK — the Python counterpart of ``sdk-identity-jvm``.

Thin, opinionated helpers for OIDC/OAuth 2.0 so product squads get safe defaults
without re-deriving them. JWT *validation* intentionally lives in the resource
server (mirrors the JVM SDK, where Spring Security does validation and the SDK
standardizes everything around it).
"""
from .client_credentials import ClientCredentialsError, TokenResponse, fetch_client_credentials
from .discovery import OidcMetadata, discover
from .pkce import PkcePair, generate_pkce
from .scope import InsufficientScopeError, has_claim, require_scope, scopes_of

__all__ = [
    "ClientCredentialsError",
    "InsufficientScopeError",
    "OidcMetadata",
    "PkcePair",
    "TokenResponse",
    "discover",
    "fetch_client_credentials",
    "generate_pkce",
    "has_claim",
    "require_scope",
    "scopes_of",
]
