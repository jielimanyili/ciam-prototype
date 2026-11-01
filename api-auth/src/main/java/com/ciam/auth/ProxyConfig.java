package com.ciam.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ProxyConfig {

    @Bean
    public RestClient resourceServerRestClient(
            @Value("${ciam.auth.resource-server-url:http://localhost:4101}") String url) {
        // Explicit timeouts so a slow/unreachable resource server fails fast with a clear
        // error instead of hanging the request (which surfaces as a 502 at the edge).
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);
        return RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }
}
