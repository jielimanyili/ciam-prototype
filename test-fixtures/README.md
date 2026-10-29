# Test fixtures — multi-runtime parity (ADR-014)

Language-agnostic fixtures consumed by **both** resource servers' test suites, so
`api-identity-jvm` (JUnit) and `api-identity-python` (pytest) assert the *same* outcomes.

| File | Purpose |
|---|---|
| `parity-matrix.json` | endpoint × auth/scope → expected HTTP status |
| `jwt/tokens.json` | pre-signed RS256 tokens: valid · bad_signature · wrong_issuer · wrong_audience · expired |
| `jwt/rsa-private.pem` / `rsa-public.pem` | test-only RSA keypair (explicitly **not** a secret) |
| `generate_jwt.py` | regenerates the JWT vectors |

## Regenerate

```bash
python3 generate_jwt.py
```

## Consume

- Java: `api-identity-jvm/src/test/java/com/ciam/api/ParityTest.java`
- Python: `api-identity-python/tests/test_parity.py`

Both load these files and fail if an endpoint's status or a token's accept/reject
differs from the fixture — the drift detector (ADR-014).
