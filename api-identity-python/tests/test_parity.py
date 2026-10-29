import json
from pathlib import Path

import jwt as pyjwt
import pytest
from fastapi.testclient import TestClient

from app.main import app
from app.security import get_claims

FIXTURES = Path(__file__).resolve().parents[2] / "test-fixtures"
MATRIX = json.loads((FIXTURES / "parity-matrix.json").read_text())
JWT = json.loads((FIXTURES / "jwt" / "tokens.json").read_text())

client = TestClient(app)


@pytest.fixture(autouse=True)
def _clear_overrides():
    app.dependency_overrides.clear()
    yield
    app.dependency_overrides.clear()


@pytest.mark.parametrize("case", MATRIX["endpoints"], ids=[c["name"] for c in MATRIX["endpoints"]])
def test_endpoint_matrix(case):
    if case["auth"] == "bearer":
        app.dependency_overrides[get_claims] = lambda: {"sub": "demo", "scope": case["scope"]}
    r = client.get(case["path"])
    assert r.status_code == case["expect"], f"{case['name']}: got {r.status_code}, want {case['expect']}"


@pytest.mark.parametrize("name", list(JWT["cases"]), ids=list(JWT["cases"]))
def test_jwt_validation(name):
    spec = JWT["cases"][name]
    pub = (FIXTURES / "jwt" / "rsa-public.pem").read_bytes()
    accepted = True
    try:
        pyjwt.decode(
            spec["token"],
            pub,
            algorithms=[JWT["algorithm"]],
            issuer=JWT["issuer"],
            audience=JWT["audience"],
        )
    except pyjwt.PyJWTError:
        accepted = False
    want = spec["expected"] == "accept"
    assert accepted == want, f"{name}: accepted={accepted}, expected={spec['expected']}"
