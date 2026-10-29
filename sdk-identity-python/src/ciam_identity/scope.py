"""Scope + claim helpers for authorizing against a verified token's claims.

Python counterpart of the JVM SDK's ``ScopeAssertions``.
"""
from typing import Any, List, Mapping


class InsufficientScopeError(RuntimeError):
    def __init__(self, scope: str):
        super().__init__(f"missing required scope: {scope}")


def scopes_of(claims: Mapping[str, Any]) -> List[str]:
    scope = claims.get("scope")
    if not isinstance(scope, str) or not scope.strip():
        return []
    return [s for s in scope.strip().split() if s]


def require_scope(claims: Mapping[str, Any], required: str) -> None:
    if required not in scopes_of(claims):
        raise InsufficientScopeError(required)


def has_claim(claims: Mapping[str, Any], key: str, value: Any = None) -> bool:
    v = claims.get(key)
    if value is None:
        return v is not None
    if isinstance(v, list):
        return value in v
    return value == v
