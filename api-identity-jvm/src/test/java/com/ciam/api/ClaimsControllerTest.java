package com.ciam.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClaimsController.class)
@Import(SecurityConfig.class)
class ClaimsControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    JwtDecoder jwtDecoder;

    @Test
    void healthIsPublic() throws Exception {
        mvc.perform(get("/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true));
    }

    @Test
    void meRequiresAuth() throws Exception {
        mvc.perform(get("/api/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void meAcceptsValidToken() throws Exception {
        mvc.perform(get("/api/me").with(SecurityMockMvcRequestPostProcessors.jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.claims.sub").exists());
    }

    @Test
    void adminRequiresScope() throws Exception {
        mvc.perform(get("/api/admin").with(SecurityMockMvcRequestPostProcessors.jwt()))
            .andExpect(status().isForbidden());

        mvc.perform(get("/api/admin").with(
                SecurityMockMvcRequestPostProcessors.jwt()
                    .jwt(j -> j.claim("scope", "openid admin"))
                    .authorities(new SimpleGrantedAuthority("SCOPE_admin"))))
            .andExpect(status().isOk());
    }
}
