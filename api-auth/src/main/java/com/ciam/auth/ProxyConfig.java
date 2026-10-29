package com.ciam.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ProxyConfig {

    @Bean
    public RestClient resourceServerRestClient(
            @Value("${ciam.auth.resource-server-url:http://localhost:4101}") String url) {
        return RestClient.builder().baseUrl(url).build();
    }
}
