package com.ciam.identity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** OAuth2 Client Credentials grant — machine-to-machine token fetch. */
public final class ClientCredentials {

    private ClientCredentials() {}

    public static TokenResponse fetch(String tokenEndpoint, String clientId, String clientSecret, String scope)
            throws IOException, InterruptedException {
        String form = "grant_type=client_credentials"
                + (scope == null || scope.isBlank() ? "" : "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8));
        String basic = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder(URI.create(tokenEndpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + basic)
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("client_credentials failed (" + response.statusCode() + "): " + response.body());
        }
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return new TokenResponse(
                json.get("access_token").getAsString(),
                json.has("id_token") ? json.get("id_token").getAsString() : null,
                json.has("refresh_token") ? json.get("refresh_token").getAsString() : null,
                json.get("token_type").getAsString(),
                json.get("expires_in").getAsLong(),
                json.has("scope") ? json.get("scope").getAsString() : null);
    }
}
