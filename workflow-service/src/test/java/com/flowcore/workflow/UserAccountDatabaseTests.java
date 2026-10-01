package com.flowcore.workflow;

import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserAccountDatabaseTests {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void shouldInsertAccountWithDefaults() {
        String login = "teste." + UUID.randomUUID();
        String email = login + "@example.com";

        insertAccount(login, email);

        var account = jdbc.queryForMap("""
                SELECT id, display_name, login, email, active, created_at
                FROM user_account
                WHERE login = ?
                """, login);

        assertInstanceOf(UUID.class, account.get("id"));
        assertEquals("Pessoa Teste", account.get("display_name"));
        assertEquals(login, account.get("login"));
        assertEquals(email, account.get("email"));
        assertEquals(Boolean.TRUE, account.get("active"));
        assertNotNull(account.get("created_at"));
    }

    @Test
    void shouldRejectDuplicateEmailIgnoringCase() {
        String suffix = UUID.randomUUID().toString();
        String email = "pessoa." + suffix + "@example.com";

        insertAccount("primeiro." + suffix, email);

        assertThrows(DuplicateKeyException.class, () ->
                insertAccount(
                        "segundo." + suffix,
                        email.toUpperCase(Locale.ROOT)
                )
        );
    }

    private void insertAccount(String login, String email) {
        jdbc.update("""
                INSERT INTO user_account (
                    display_name, login, email, password_hash
                )
                VALUES (?, ?, ?, ?)
                """,
                "Pessoa Teste",
                login,
                email,
                "marcador-apenas-para-teste-de-estrutura"
        );
    }
}