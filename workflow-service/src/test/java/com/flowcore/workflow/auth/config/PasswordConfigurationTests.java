package com.flowcore.workflow.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordConfigurationTests {

    @Test
    void shouldMatchOnlyTheCorrectPassword(){
        PasswordEncoder encoder =
                new PasswordConfiguration().passwordEncoder();

        String hash = encoder.encode("senha-ficticia-do-teste");

        assertTrue(encoder.matches("senha-ficticia-do-teste", hash));
        assertFalse(encoder.matches("senha-ficticia-errada", hash));
    }
}
