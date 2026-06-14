package br.senai.sc.communitex.security;

import br.senai.sc.communitex.exception.ForbiddenException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public final class AuthenticatedUser {

    private AuthenticatedUser() {
    }

    public static String username() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Usuario autenticado nao encontrado no contexto");
        }

        var principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String username && !"anonymousUser".equals(username)) {
            return username;
        }
        throw new ForbiddenException("Usuario autenticado nao encontrado no contexto");
    }
}
