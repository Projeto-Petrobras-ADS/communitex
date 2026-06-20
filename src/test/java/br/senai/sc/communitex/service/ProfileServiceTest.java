package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.PasswordChangeRequest;
import br.senai.sc.communitex.exception.RegistrationValidationException;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PessoaFisicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock PessoaFisicaRepository pessoaFisicaRepository;
    @Mock EmpresaRepository empresaRepository;
    @Mock UsuarioService usuarioService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    private ProfileService service;

    @BeforeEach
    void setUp() {
        service = new ProfileService(pessoaFisicaRepository, empresaRepository, usuarioService,
                passwordEncoder, jwtService);
    }

    @Test
    void changesPasswordAndReturnsFreshTokens() {
        var usuario = Usuario.builder().username("maria@email.com").password("hash-antigo").role("ROLE_USER").build();
        when(usuarioService.findByUsername("maria@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Antiga@123", "hash-antigo")).thenReturn(true);
        when(passwordEncoder.encode("Nova@1234")).thenReturn("hash-novo");
        when(jwtService.generateToken(usuario)).thenReturn("access");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh");

        try (MockedStatic<br.senai.sc.communitex.security.AuthenticatedUser> auth =
                     mockStatic(br.senai.sc.communitex.security.AuthenticatedUser.class)) {
            auth.when(br.senai.sc.communitex.security.AuthenticatedUser::username).thenReturn("maria@email.com");
            var response = service.changePassword(new PasswordChangeRequest("Antiga@123", "Nova@1234", "Nova@1234"));

            assertEquals("access", response.accessToken);
            assertEquals("refresh", response.refreshToken);
            assertEquals("hash-novo", usuario.getPassword());
            verify(usuarioService).save(usuario);
        }
    }

    @Test
    void rejectsIncorrectCurrentPasswordAndWeakNewPassword() {
        var usuario = Usuario.builder().username("maria@email.com").password("hash-antigo").role("ROLE_USER").build();
        when(usuarioService.findByUsername("maria@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        try (MockedStatic<br.senai.sc.communitex.security.AuthenticatedUser> auth =
                     mockStatic(br.senai.sc.communitex.security.AuthenticatedUser.class)) {
            auth.when(br.senai.sc.communitex.security.AuthenticatedUser::username).thenReturn("maria@email.com");
            var error = assertThrows(RegistrationValidationException.class,
                    () -> service.changePassword(new PasswordChangeRequest("errada", "fraca", "diferente")));

            assertEquals("A senha atual está incorreta", error.getErrors().get("senhaAtual"));
            assertEquals(true, error.getErrors().containsKey("novaSenha"));
            assertEquals(true, error.getErrors().containsKey("confirmacaoSenha"));
        }
    }
}
