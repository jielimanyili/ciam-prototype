import pytest
from fastapi.testclient import TestClient

from app.main import app
from app.security import get_claims

client = TestClient(app)


@pytest.fixture(autouse=True)
def _clear_overrides():
    app.dependency_overrides.clear()
    yield
    app.dependency_overrides.clear()


def _override(claims):
    app.dependency_overrides[get_claims] = lambda: claims


def test_health_is_public():
    assert client.get("/health").json() == {"ok": True}


def test_me_requires_auth():
    assert client.get("/api/me").status_code == 401


def test_me_returns_claims():
    _override({"sub": "demo", "scope": "openid"})
    r = client.get("/api/me")
    assert r.status_code == 200
    assert r.json()["claims"]["sub"] == "demo"


def test_admin_requires_scope():
    _override({"sub": "demo", "scope": "openid"})
    assert client.get("/api/admin").status_code == 403


def test_admin_accepts_admin_scope():
    _override({"sub": "demo", "scope": "openid admin"})
    r = client.get("/api/admin")
    assert r.status_code == 200
    assert r.json() == {"message": "welcome, admin"}
