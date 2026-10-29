package com.ciam.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies the BFF's security surface: unauthenticated requests redirect to the IdP
 * (server-side oauth2Login) and CSRF is enforced (NFR-9).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiAuthSecurityTest {

    @Autowired
    MockMvc mvc;

    @Test
    void unauthenticatedRedirectsToLogin() throws Exception {
        mvc.perform(get("/api/me"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/ciam"));
    }

    @Test
    void csrfRequiredOnPost() throws Exception {
        mvc.perform(post("/logout"))
                .andExpect(status().isForbidden());
    }
}
