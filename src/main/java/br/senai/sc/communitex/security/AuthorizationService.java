package br.senai.sc.communitex.security;

import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PessoaFisicaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("authz")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthorizationService {

    private final PracaRepository pracaRepository;
    private final PessoaFisicaRepository pessoaFisicaRepository;
    private final EmpresaRepository empresaRepository;

    public boolean isAdmin() {
        return hasRole(SecurityContextHolder.getContext().getAuthentication(), "ROLE_ADMIN");
    }

    public boolean isPracaOwnerOrAdmin(Long pracaId) {
        if (isAdmin()) {
            return true;
        }
        var username = currentUsername();
        return username != null && pracaRepository.findById(pracaId)
                .map(praca -> praca.getCadastradoPor() != null
                        && praca.getCadastradoPor().getUsuario() != null
                        && username.equals(praca.getCadastradoPor().getUsuario().getUsername()))
                .orElse(false);
    }

    public boolean isPessoaFisicaOwnerOrAdmin(Long pessoaFisicaId) {
        if (isAdmin()) {
            return true;
        }
        var username = currentUsername();
        return username != null && pessoaFisicaRepository.findById(pessoaFisicaId)
                .map(pessoa -> pessoa.getUsuario() != null
                        && username.equals(pessoa.getUsuario().getUsername()))
                .orElse(false);
    }

    public boolean isEmpresaOwnerOrAdmin(Long empresaId) {
        if (isAdmin()) {
            return true;
        }
        var username = currentUsername();
        return username != null && empresaRepository.findById(empresaId)
                .map(empresa -> empresa.getUsuarioRepresentante() != null
                        && username.equals(empresa.getUsuarioRepresentante().getUsername()))
                .orElse(false);
    }

    private String currentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        var principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String username) {
            return username;
        }
        return null;
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }
}
