package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void givenUsernameExistente_whenFindByUsername_thenRetornaOptionalComUsuario() {
        var usuario = usuario(1L, "user@test.com", "ROLE_USER");
        when(usuarioRepository.findByUsername("user@test.com")).thenReturn(Optional.of(usuario));

        var resultado = usuarioService.findByUsername("user@test.com");

        assertEquals("user@test.com", resultado.orElseThrow().getUsername());
    }

    @Test
    void givenUsuarioValido_whenSave_thenPersisteUsuario() {
        var usuario = usuario(null, "user@test.com", "ROLE_USER");
        var salvo = usuario(5L, "user@test.com", "ROLE_USER");
        when(usuarioRepository.save(usuario)).thenReturn(salvo);

        var resultado = usuarioService.save(usuario);

        assertEquals(5L, resultado.getId());
        assertEquals("ROLE_USER", resultado.getRole());
    }

    @Test
    void givenUsernameExistente_whenLoadUserByUsername_thenRetornaUserDetails() {
        var usuario = usuario(1L, "auth@test.com", "ROLE_USER");
        when(usuarioRepository.findByUsername("auth@test.com")).thenReturn(Optional.of(usuario));

        var userDetails = usuarioService.loadUserByUsername("auth@test.com");

        assertSame(usuario, userDetails);
    }

    @Test
    void givenUsernameInexistente_whenLoadUserByUsername_thenLancaUsernameNotFoundException() {
        when(usuarioRepository.findByUsername("inexistente@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> usuarioService.loadUserByUsername("inexistente@test.com"));
    }

    private Usuario usuario(Long id, String username, String role) {
        var usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setPassword("hash");
        usuario.setRole(role);
        return usuario;
    }
}

