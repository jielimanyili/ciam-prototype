#!/usr/bin/env python3
"""Generate the shared RS256 JWT test vectors (ADR-014).

Produces a test-only RSA keypair and a tokens.json of pre-signed JWTs covering the
parity cases both resource servers must agree on (accept/reject). The key here is
explicitly NOT a secret — it exists only to exercise validation.

Usage:
    python3 generate_jwt.py
"""
import json
import os
from datetime import datetime, timedelta, timezone

import jwt
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import rsa

HERE = os.path.dirname(os.path.abspath(__file__))
JWT_DIR = os.path.join(HERE, "jwt")
os.makedirs(JWT_DIR, exist_ok=True)

ISSUER = "http://localhost:8080/realms/ciam-demo"
AUDIENCE = "api-identity-jvm"


def _write(name: str, data: bytes) -> None:
    with open(os.path.join(JWT_DIR, name), "wb") as f:
        f.write(data)


def _sign(private_key, claims: dict) -> str:
    return jwt.encode(claims, private_key, algorithm="RS256")


def main() -> None:
    key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    _write(
        "rsa-private.pem",
        key.private_bytes(
            serialization.Encoding.PEM,
            serialization.PrivateFormat.PKCS8,
            serialization.NoEncryption(),
        ),
    )
    _write(
        "rsa-public.pem",
        key.public_key().public_bytes(
            serialization.Encoding.PEM,
            serialization.PublicFormat.SubjectPublicKeyInfo,
        ),
    )

    # A second key mints the "bad signature" token.
    other = rsa.generate_private_key(public_exponent=65537, key_size=2048)

    now = datetime.now(timezone.utc)
    base = {
        "iss": ISSUER,
        "aud": [AUDIENCE],
        "sub": "demo",
        "scope": "openid admin",
        "iat": int(now.timestamp()),
        "exp": int((now + timedelta(hours=1)).timestamp()),
    }

    cases = {}
    cases["valid"] = {"token": _sign(key, base), "expected": "accept", "claims": base}
    cases["bad_signature"] = {"token": _sign(other, base), "expected": "reject", "claims": base}

    w = dict(base)
    w["iss"] = "https://evil.example/realms/ciam-demo"
    cases["wrong_issuer"] = {"token": _sign(key, w), "expected": "reject", "claims": w}

    w = dict(base)
    w["aud"] = ["other-api"]
    cases["wrong_audience"] = {"token": _sign(key, w), "expected": "reject", "claims": w}

    w = dict(base)
    w["exp"] = int((now - timedelta(hours=1)).timestamp())
    cases["expired"] = {"token": _sign(key, w), "expected": "reject", "claims": w}

    with open(os.path.join(JWT_DIR, "tokens.json"), "w") as f:
        json.dump(
            {"issuer": ISSUER, "audience": AUDIENCE, "algorithm": "RS256", "cases": cases},
            f,
            indent=2,
        )

    print("wrote", os.path.join(JWT_DIR, "tokens.json"))


if __name__ == "__main__":
    main()
