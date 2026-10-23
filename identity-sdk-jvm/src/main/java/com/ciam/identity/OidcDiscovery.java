package com.ciam.identity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/** OIDC discovery (RFC 8414) — fetch the provider's {@code /.well-known/openid-configuration}. */
public final class OidcDiscovery {

    private OidcDiscovery() {}

    public static OidcMetadata discover(String issuer) throws IOException, InterruptedException {
        String base = issuer.replaceAll("/+$", "");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/.well-known/openid-configuration"))
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("OIDC discovery failed (" + response.statusCode() + ")");
        }
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return new OidcMetadata(
                json.get("issuer").getAsString(),
                json.get("authorization_endpoint").getAsString(),
                json.get("token_endpoint").getAsString(),
                json.get("jwks_uri").getAsString(),
                json.has("end_session_endpoint") ? json.get("end_session_endpoint").getAsString() : null);
    }
}
