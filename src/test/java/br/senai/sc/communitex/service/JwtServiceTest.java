package br.senai.sc.communitex.service;

import br.senai.sc.communitex.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService service;
    private Usuario user;

    @BeforeEach
    void setUp() {
        service = new JwtService();
        ReflectionTestUtils.setField(service, "SECRET_KEY", "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
        ReflectionTestUtils.setField(service, "acessExpirationTimeMs", 60_000L);
        ReflectionTestUtils.setField(service, "refreshExpirationMs", 120_000L);
        user = Usuario.builder().username("user@test.com").password("hash").role("ROLE_USER").build();
    }

    @Test
    void generatesAndReadsAccessAndRefreshTokens() {
        var access = service.generateToken(user);
        var refresh = service.generateRefreshToken(user);

        assertEquals("user@test.com", service.extractUsername(access));
        assertEquals(List.of("ROLE_USER"), service.extractRoles(access));
        assertTrue(service.isTokenValid(access, user));
        assertTrue(service.isTokenValid(refresh, user));
    }

    @Test
    void rejectsTokenForDifferentUserAndReturnsEmptyRolesWhenAbsent() {
        var token = service.generateToken(user);
        var other = Usuario.builder().username("other@test.com").password("hash").role("ROLE_USER").build();

        assertFalse(service.isTokenValid(token, other));
        assertEquals(List.of("ROLE_USER"), service.extractClaim(token, claims -> claims.get("roles", List.class)));
    }
}
