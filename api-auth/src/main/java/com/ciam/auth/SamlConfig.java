package com.ciam.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;

/**
 * SAML 2.0 SP (Epic 2, ADR-004). The IdP metadata is resolved here so SAML can be toggled off
 * for an OIDC-only deploy (e.g. the Okta live demo) without the SP trying to fetch a local IdP
 * at startup. When {@code saml.enabled=false} the repository is empty and {@code oauth2Login()}
 * is unaffected.
 */
@Configuration
public class SamlConfig {

    @Bean
    public RelyingPartyRegistrationRepository samlRelyingPartyRegistrationRepository(
            @Value("${saml.enabled:true}") boolean enabled,
            @Value("${saml.idp.metadata-uri:http://localhost:8080/realms/ciam-demo/protocol/saml/descriptor}") String metadataUri) {
        if (!enabled) {
            // SAML disabled: keep saml2Login() wired but perform no IdP metadata fetch.
            return registrationId -> null;
        }
        RelyingPartyRegistration registration = RelyingPartyRegistrations
                .fromMetadataLocation(metadataUri)
                .registrationId("keycloak")
                .entityId("api-auth-saml")
                .assertionConsumerServiceLocation("http://localhost:4103/login/saml2/sso/keycloak")
                .build();
        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }
}