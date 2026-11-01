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
        // Timeouts tuned for Render's free tier (ADR-022). A free web service sleeps after ~15 min
        // idle and cold-starts in ~50-60 s (JVM) before it can answer, so the read timeout must
        // exceed that window — otherwise the BFF aborts a legitimate request mid spin-up (which used
        // to surface as a 502 at the edge with no app log). The connect timeout can stay short: the
        // public *.onrender.com endpoint is fronted by Render/Cloudflare, which accepts the TCP
        // connection immediately and holds the request while the upstream boots.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(120_000);
        return RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }
}
