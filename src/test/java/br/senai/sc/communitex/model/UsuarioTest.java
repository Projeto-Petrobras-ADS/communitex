package br.senai.sc.communitex.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

    @Test
    void handlesMissingAndConfiguredRoles() {
        var user = new Usuario();
        assertTrue(user.getAuthorities().isEmpty());

        user.setRole("ROLE_USER");
        assertEquals("ROLE_USER", user.getAuthorities().iterator().next().getAuthority());
    }
}
