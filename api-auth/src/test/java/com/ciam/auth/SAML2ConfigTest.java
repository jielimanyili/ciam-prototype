package com.ciam.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;

/**
 * Verifies the IdP SAML metadata parses into a relying-party registration (the brittle part of the
 * SAML SP config): the SSO URL + entityId are extracted correctly.
 */
class SAML2ConfigTest {

    private static final String IDP_CERT =
            "MIICrDCCAZQCCQCqVOWvAe2ojTANBgkqhkiG9w0BAQsFADAYMRYwFAYDVQQDDA1TQU1MIFRlc3QgSWRQ"
            + "MB4XDTI2MTAyOTIyNDQ1OFoXDTM2MTAyNjIyNDQ1OFowGDEWMBQGA1UEAwwNU0FNTCBUZXN0IElkUD"
            + "CCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALBFeF0xez64L/fkSQGozbzXr69SGjBgQfkZ"
            + "pX+Qi0jPpeI7eqp65f4GXNd7caPqo2ru0qjt196ZcljPAzV0XTM1qLqLklpPFOH9tR+DXT1z6+CDUH"
            + "ib0HFuwF7pRYo50nSBmLNezJKmF4K+krkCHYoAtuAYu8WLYLnk7cKH3Z/dTuW3YMNkeWbYm0SyWsZN"
            + "sUfAzXuywzooq41GRnye8ltG4QQGmbQOp51sF/k0CEBG+YaGJpk6a0MUHYoxzuU+TvIRbp5qOc3IPr"
            + "1EFHxHW7dr0O1YE1lJ4KIPt4c+ULXFipF6yIAcB//pZ/qcl0B6U18zw6455t8+ua35xjYGLBcCAwEA"
            + "ATANBgkqhkiG9w0BAQsFAAOCAQEAAGLevZj6QnBX17lufkrwCyVrwfNFykiDTx2HkpZ2UZ6m2oKaYd"
            + "ywFBMS/ni3WauTLfas2DjKkGr0Np+lSBQJPe8AZgq5TgT0AMeIzJEYyCQFebyurkbefpNue1hLzJ2E"
            + "DBxtTnwnzX822jFxBoRYBXqwDGSUDRXXozmXl/K/pq7x+HctafoutDuYuAGXq1lGV4/TBgVz3ZPajI"
            + "DtikehLmuWPcC30pnGa0D15mK9SxykUaiQ2NPLwTcm3khovstuBy7tPZYC9C9UCLEr6D30q1r4JJZN"
            + "sNPBQ0JB4Qn3PTFRmoZKtQrEOXbfWCy2CBeXJDoqaVW4wsHs3rCk1tIErQ==";

    @Test
    void parsesIdpMetadata() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/descriptor", exchange -> respond(exchange,
                "<EntityDescriptor xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\" entityID=\"http://localhost:8080/realms/ciam-demo\">"
                + "<IDPSSODescriptor protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">"
                + "<KeyDescriptor use=\"signing\">"
                + "<ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\"><ds:X509Data><ds:X509Certificate>"
                + IDP_CERT
                + "</ds:X509Certificate></ds:X509Data></ds:KeyInfo>"
                + "</KeyDescriptor>"
                + "<SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect\" Location=\"http://localhost:8080/realms/ciam-demo/protocol/saml\"/>"
                + "</IDPSSODescriptor></EntityDescriptor>"));
        server.start();
        try {
            int port = server.getAddress().getPort();
            RelyingPartyRegistration registration = RelyingPartyRegistrations
                    .fromMetadataLocation("http://127.0.0.1:" + port + "/descriptor")
                    .registrationId("keycloak")
                    .entityId("api-auth-saml")
                    .assertionConsumerServiceLocation("http://localhost:4103/login/saml2/sso/keycloak")
                    .build();

            assertThat(registration.getAssertingPartyDetails().getSingleSignOnServiceLocation())
                    .isEqualTo("http://localhost:8080/realms/ciam-demo/protocol/saml");
            assertThat(registration.getEntityId()).isEqualTo("api-auth-saml");
            assertThat(registration.getAssertingPartyDetails().getVerificationX509Credentials()).hasSize(1);
        } finally {
            server.stop(0);
        }
    }

    private static void respond(HttpExchange exchange, String xml) throws IOException {
        byte[] body = xml.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/samlmetadata+xml");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }
}
