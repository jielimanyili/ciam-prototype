package com.ciam.identity;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class PkceTest {

    @Test
    void generatesS256Pair() throws Exception {
        PkcePair pair = Pkce.generate();
        assertEquals(43, pair.codeVerifier().length());
        assertEquals("S256", pair.codeChallengeMethod());
        byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(pair.codeVerifier().getBytes(StandardCharsets.US_ASCII));
        assertEquals(Base64.getUrlEncoder().withoutPadding().encodeToString(hash), pair.codeChallenge());
    }

    @Test
    void isUniquePerCall() {
        assertNotEquals(Pkce.generate().codeVerifier(), Pkce.generate().codeVerifier());
    }
}
