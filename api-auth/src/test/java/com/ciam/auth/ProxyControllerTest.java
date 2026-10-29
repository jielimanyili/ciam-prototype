package com.ciam.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.RestClient;

class ProxyControllerTest {

    @Test
    void forwardsBearerTokenToResourceServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> authHeader = new AtomicReference<>();
        server.createContext("/api/me", exchange -> {
            authHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            respond(exchange, "{\"claims\":{\"sub\":\"demo\"}}");
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            RestClient restClient = RestClient.builder().baseUrl("http://127.0.0.1:" + port).build();
            ProxyController controller = new ProxyController(restClient);

            OAuth2AccessToken token = new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER, "test-token",
                    Instant.now(), Instant.now().plusSeconds(60));
            ClientRegistration registration = ClientRegistration.withRegistrationId("ciam")
                    .clientId("api-auth")
                    .clientSecret("api-auth-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .authorizationUri("http://localhost:8080/realms/ciam-demo/protocol/openid-connect/auth")
                    .tokenUri("http://localhost:8080/realms/ciam-demo/protocol/openid-connect/token")
                    .build();
            OAuth2AuthorizedClient authorized = new OAuth2AuthorizedClient(registration, "demo", token);

            ResponseEntity<String> response = controller.me(authorized);

            assertThat(response.getBody()).contains("demo");
            assertThat(authHeader.get()).isEqualTo("Bearer test-token");
        } finally {
            server.stop(0);
        }
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
