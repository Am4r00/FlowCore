package com.flowcore.workflow;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthenticationHttpTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Test
    void shouldRejectLoginForInactiveAccount() throws Exception{
        String login = "teste." + UUID.randomUUID();
        String email = login + "@example.com";
        String senha = "teste123";

        jdbc.update("INSERT INTO user_account (display_name,login,email,password_hash,active) VALUES (?,?,?,?,?)",
                  "teste.teste",login,email,passwordEncoder.encode(senha),false
                   );

        MockHttpSession session = new MockHttpSession();

        mvc.perform(post("/api/auth/login")
                        .session(session)
                        .with(csrf())
                        .param("username",login)
                        .param("password",senha))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/auth/me")
                        .session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAuthenticateActiveAccountAndReturnSummary() throws Exception{
        String displayName = "testezinho";
        String login = "teste." + UUID.randomUUID();
        String email = login + "@example.com";
        String senha = "teste123";


        jdbc.update("INSERT INTO user_account (display_name,login,email,password_hash,active) VALUES (?,?,?,?,?)",
                displayName,login,email,passwordEncoder.encode(senha),true
        );

        MockHttpSession session = new MockHttpSession();

        mvc.perform(post("/api/auth/login")
                        .session(session)
                        .with(csrf())
                        .param("username",login)
                        .param("password",senha))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/auth/me")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(login))
                .andExpect(jsonPath("$.displayName").value(displayName))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

}
