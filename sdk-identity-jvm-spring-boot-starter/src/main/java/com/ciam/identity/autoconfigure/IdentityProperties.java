package com.ciam.identity.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** CIAM identity configuration — {@code ciam.identity.*}. */
@ConfigurationProperties(prefix = "ciam.identity")
public class IdentityProperties {

    /** OIDC issuer — the JWKS URI is auto-discovered from {@code /.well-known/openid-configuration} (RFC 8414). */
    private String issuerUri;

    /** Required {@code aud} claim — the check Spring's {@code issuer-uri} does NOT do by default. */
    private String audience;

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }
}
