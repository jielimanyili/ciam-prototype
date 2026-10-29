package com.ciam.identity.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtDecoder;

class IdentityAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(IdentityAutoConfiguration.class));

    @Test
    void backsOffWhenIssuerMissing() {
        runner.run(ctx -> assertThat(ctx).doesNotHaveBean(JwtDecoder.class));
    }

    @Test
    void configuresDecoderWhenIssuerSet() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        String jwks = jwksJson();

        server.createContext("/realms/ciam-demo/.well-known/openid-configuration", exchange -> {
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            respond(exchange, "{\"issuer\":\"" + base + "/realms/ciam-demo\","
                    + "\"authorization_endpoint\":\"" + base + "/realms/ciam-demo/protocol/openid-connect/auth\","
                    + "\"token_endpoint\":\"" + base + "/realms/ciam-demo/protocol/openid-connect/token\","
                    + "\"jwks_uri\":\"" + base + "/jwks\"}");
        });
        server.createContext("/jwks", exchange -> respond(exchange, jwks));

        server.start();
        try {
            int port = server.getAddress().getPort();
            runner
                .withPropertyValues(
                        "ciam.identity.issuer-uri=http://127.0.0.1:" + port + "/realms/ciam-demo",
                        "ciam.identity.audience=api-identity-jvm")
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(JwtDecoder.class);
                    assertThat(ctx).hasSingleBean(IdentityProperties.class);
                });
        } finally {
            server.stop(0);
        }
    }

    /** Reuses the shared test-fixtures RSA key so the starter test agrees with the parity tests. */
    private static String jwksJson() throws Exception {
        byte[] pem = Files.readAllBytes(Path.of("../test-fixtures/jwt/rsa-public.pem"));
        RSAPublicKey pub = RsaKeyConverters.x509().convert(new ByteArrayInputStream(pem));
        return "{\"keys\":[{\"kty\":\"RSA\",\"use\":\"sig\",\"alg\":\"RS256\",\"kid\":\"test\","
                + "\"n\":\"" + base64Url(pub.getModulus())
                + "\",\"e\":\"" + base64Url(pub.getPublicExponent()) + "\"}]}";
    }

    private static String base64Url(BigInteger i) {
        byte[] b = i.toByteArray();
        if (b.length > 1 && b[0] == 0) {
            b = Arrays.copyOfRange(b, 1, b.length);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private static void respond(HttpExchange exchange, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }
}


