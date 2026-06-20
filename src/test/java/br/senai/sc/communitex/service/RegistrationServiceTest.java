package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.TipoConta;
import br.senai.sc.communitex.dto.UnifiedRegisterRequest;
import br.senai.sc.communitex.exception.RegistrationValidationException;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PessoaFisicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock UsuarioService usuarioService;
    @Mock PessoaFisicaRepository pessoaFisicaRepository;
    @Mock EmpresaRepository empresaRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    private RegistrationService service;

    @BeforeEach
    void setUp() {
        service = new RegistrationService(usuarioService, pessoaFisicaRepository, empresaRepository,
                passwordEncoder, jwtService);
    }

    @Test
    void registersPessoaFisicaAndReturnsAuthenticatedSession() {
        var request = pessoaRequest(" Cidadao@Email.com ", "529.982.247-25");
        when(usuarioService.findByUsername("cidadao@email.com")).thenReturn(Optional.empty());
        when(pessoaFisicaRepository.findByEmail("cidadao@email.com")).thenReturn(Optional.empty());
        when(pessoaFisicaRepository.findByCpf("52998224725")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Senha@123")).thenReturn("hash");
        when(pessoaFisicaRepository.save(any())).thenAnswer(invocation -> {
            PessoaFisica pessoa = invocation.getArgument(0);
            pessoa.setId(10L);
            return pessoa;
        });
        when(jwtService.generateToken(any())).thenReturn("access");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh");

        var result = service.register(request);

        assertEquals("access", result.accessToken());
        assertEquals("cidadao@email.com", result.email());
        assertEquals(10L, result.perfilId());
        verify(usuarioService).save(any(Usuario.class));
        verify(pessoaFisicaRepository).save(any(PessoaFisica.class));
    }

    @Test
    void registersEmpresaWithoutOptionalContactOrAddress() {
        var request = new UnifiedRegisterRequest(TipoConta.EMPRESA, null, null,
                "Empresa Teste", "11.222.333/0001-81", "Maria Silva", "maria@empresa.com",
                "Senha@123", "Senha@123", true);
        when(usuarioService.findByUsername("maria@empresa.com")).thenReturn(Optional.empty());
        when(pessoaFisicaRepository.findByEmail("maria@empresa.com")).thenReturn(Optional.empty());
        when(empresaRepository.buscarPorCnpj("11222333000181")).thenReturn(Optional.empty());
        when(empresaRepository.save(any())).thenAnswer(invocation -> {
            Empresa empresa = invocation.getArgument(0);
            empresa.setId(20L);
            return empresa;
        });

        var result = service.register(request);

        assertEquals(TipoConta.EMPRESA, result.tipoConta());
        assertEquals(20L, result.perfilId());
        verify(empresaRepository).save(any(Empresa.class));
    }

    @Test
    void rejectsWeakPasswordAndMismatchedConfirmation() {
        var request = new UnifiedRegisterRequest(TipoConta.PESSOA_FISICA, "Pessoa", "52998224725",
                null, null, null, "pessoa@email.com", "fraca", "outra", true);

        var exception = assertThrows(RegistrationValidationException.class, () -> service.register(request));

        assertEquals(true, exception.getErrors().containsKey("senha"));
        assertEquals(true, exception.getErrors().containsKey("confirmacaoSenha"));
    }

    @Test
    void rejectsDuplicateDocumentAndEmailWithFieldErrors() {
        var request = pessoaRequest("pessoa@email.com", "52998224725");
        when(usuarioService.findByUsername("pessoa@email.com")).thenReturn(Optional.of(new Usuario()));
        when(pessoaFisicaRepository.findByEmail("pessoa@email.com")).thenReturn(Optional.empty());
        when(pessoaFisicaRepository.findByCpf("52998224725")).thenReturn(Optional.of(new PessoaFisica()));

        var exception = assertThrows(RegistrationValidationException.class, () -> service.register(request));

        assertEquals("Este e-mail já possui cadastro", exception.getErrors().get("email"));
        assertEquals("Este CPF já possui cadastro", exception.getErrors().get("cpf"));
    }

    @Test
    void doesNotIssueTokensWhenProfileCreationFails() {
        var request = pessoaRequest("pessoa@email.com", "52998224725");
        when(usuarioService.findByUsername("pessoa@email.com")).thenReturn(Optional.empty());
        when(pessoaFisicaRepository.findByEmail("pessoa@email.com")).thenReturn(Optional.empty());
        when(pessoaFisicaRepository.findByCpf("52998224725")).thenReturn(Optional.empty());
        when(pessoaFisicaRepository.save(any())).thenThrow(new RuntimeException("falha ao persistir perfil"));

        assertThrows(RuntimeException.class, () -> service.register(request));

        verify(jwtService, never()).generateToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
    }

    private UnifiedRegisterRequest pessoaRequest(String email, String cpf) {
        return new UnifiedRegisterRequest(TipoConta.PESSOA_FISICA, "Pessoa Teste", cpf,
                null, null, null, email, "Senha@123", "Senha@123", true);
    }
}
