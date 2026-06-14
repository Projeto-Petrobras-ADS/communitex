package br.senai.sc.communitex.security;

import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PessoaFisicaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private PracaRepository pracaRepository;
    @Mock
    private PessoaFisicaRepository pessoaFisicaRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @InjectMocks
    private AuthorizationService service;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminCanAccessEveryOwnedResource() {
        authenticate("admin", "ROLE_ADMIN");

        assertTrue(service.isAdmin());
        assertTrue(service.isPracaOwnerOrAdmin(1L));
        assertTrue(service.isPessoaFisicaOwnerOrAdmin(1L));
        assertTrue(service.isEmpresaOwnerOrAdmin(1L));
    }

    @Test
    void ownersCanAccessTheirResources() {
        var user = Usuario.builder().username("owner@test.com").role("ROLE_USER").build();
        authenticate("owner@test.com", "ROLE_USER");
        when(pracaRepository.findById(1L)).thenReturn(Optional.of(Praca.builder()
                .cadastradoPor(PessoaFisica.builder().usuario(user).build()).build()));
        when(pessoaFisicaRepository.findById(2L)).thenReturn(Optional.of(PessoaFisica.builder().usuario(user).build()));
        when(empresaRepository.findById(3L)).thenReturn(Optional.of(Empresa.builder().usuarioRepresentante(user).build()));

        assertTrue(service.isPracaOwnerOrAdmin(1L));
        assertTrue(service.isPessoaFisicaOwnerOrAdmin(2L));
        assertTrue(service.isEmpresaOwnerOrAdmin(3L));
    }

    @Test
    void anonymousMissingAndDifferentOwnersAreRejected() {
        assertFalse(service.isAdmin());
        assertFalse(service.isPracaOwnerOrAdmin(1L));

        authenticate("other@test.com", "ROLE_USER");
        when(pracaRepository.findById(1L)).thenReturn(Optional.of(Praca.builder().cadastradoPor(null).build()));
        when(pessoaFisicaRepository.findById(2L)).thenReturn(Optional.empty());
        when(empresaRepository.findById(3L)).thenReturn(Optional.of(Empresa.builder().usuarioRepresentante(null).build()));

        assertFalse(service.isPracaOwnerOrAdmin(1L));
        assertFalse(service.isPessoaFisicaOwnerOrAdmin(2L));
        assertFalse(service.isEmpresaOwnerOrAdmin(3L));
    }

    private void authenticate(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority(role))
        ));
    }
}
