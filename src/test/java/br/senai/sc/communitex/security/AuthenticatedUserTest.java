package br.senai.sc.communitex.security;

import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticatedUserTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsUsernameFromUserDetails() {
        var user = Usuario.builder().username("user@test.com").role("ROLE_USER").build();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(user, null, List.of())
        );

        assertEquals("user@test.com", AuthenticatedUser.username());
    }

    @Test
    void returnsUsernameFromStringPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("user@test.com", null, List.of())
        );

        assertEquals("user@test.com", AuthenticatedUser.username());
    }

    @Test
    void rejectsMissingAnonymousAndUnsupportedPrincipals() {
        assertThrows(ForbiddenException.class, AuthenticatedUser::username);

        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("anonymousUser", null, List.of())
        );
        assertThrows(ForbiddenException.class, AuthenticatedUser::username);

        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(new Object(), null, List.of())
        );
        assertThrows(ForbiddenException.class, AuthenticatedUser::username);
    }
}
