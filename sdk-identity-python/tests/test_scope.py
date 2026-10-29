import pytest

from ciam_identity import InsufficientScopeError, has_claim, require_scope, scopes_of


def test_parses_scopes():
    assert scopes_of({"scope": "openid profile admin"}) == ["openid", "profile", "admin"]


def test_require_scope_raises_when_missing():
    with pytest.raises(InsufficientScopeError):
        require_scope({"scope": "openid"}, "admin")


def test_require_scope_passes_when_present():
    require_scope({"scope": "openid admin"}, "admin")


def test_has_claim_checks_values():
    claims = {"groups": ["hr", "payroll"]}
    assert has_claim(claims, "groups", "hr") is True
    assert has_claim(claims, "groups", "legal") is False
