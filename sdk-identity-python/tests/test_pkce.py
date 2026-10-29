import base64
import hashlib

from ciam_identity import generate_pkce


def test_generates_s256_pair():
    pair = generate_pkce()
    assert len(pair.code_verifier) == 43
    assert pair.code_challenge_method == "S256"
    digest = hashlib.sha256(pair.code_verifier.encode("ascii")).digest()
    assert pair.code_challenge == base64.urlsafe_b64encode(digest).rstrip(b"=").decode("ascii")


def test_is_unique_per_call():
    assert generate_pkce().code_verifier != generate_pkce().code_verifier
