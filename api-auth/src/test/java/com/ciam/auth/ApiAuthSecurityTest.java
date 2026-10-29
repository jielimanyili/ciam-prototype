package com.ciam.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies the BFF's security surface: unauthenticated requests are sent to a login flow
 * (server-side oauth2Login / saml2Login) and CSRF is enforced (NFR-9). The SAML relying-party
 * repository is mocked so the IdP metadata isn't fetched during the offline test.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiAuthSecurityTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    RelyingPartyRegistrationRepository samlRegistrationRepository;

    @Test
    void unauthenticatedRedirectsToLogin() throws Exception {
        mvc.perform(get("/api/me"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void csrfRequiredOnPost() throws Exception {
        mvc.perform(post("/logout"))
                .andExpect(status().isForbidden());
    }
}

