package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.PessoaFisicaRequestDTO;
import br.senai.sc.communitex.exception.BusinessException;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.PessoaFisicaRepository;
import br.senai.sc.communitex.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PessoaFisicaServiceImplTest {

    @Mock
    private PessoaFisicaRepository pessoaFisicaRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PessoaFisicaServiceImpl pessoaFisicaService;

    @Test
    void givenUsuarioSemPessoaFisica_whenFindByUsuarioUsername_thenLancaForbiddenException() {
        when(pessoaFisicaRepository.findByUsuarioUsername("user@test.com")).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> pessoaFisicaService.findByUsuarioUsername("user@test.com"));
    }

    @Test
    void givenDadosValidos_whenCreate_thenCriaPessoaFisica() {
        var dto = new PessoaFisicaRequestDTO(
                "Murilo",
                "123.456.789-01",
                "murilo@email.com",
                "(48) 99999-9999",
                "senha123"
        );

        when(pessoaFisicaRepository.findByCpf("123.456.789-01")).thenReturn(Optional.empty());
        when(pessoaFisicaRepository.findByEmail("murilo@email.com")).thenReturn(Optional.empty());
        when(usuarioService.findByUsername("murilo@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("senha-hash");
        when(usuarioService.save(any(Usuario.class))).thenAnswer(invocation -> {
            var usuario = invocation.getArgument(0, Usuario.class);
            usuario.setId(77L);
            return usuario;
        });
        when(pessoaFisicaRepository.save(any(PessoaFisica.class))).thenAnswer(invocation -> {
            var pessoa = invocation.getArgument(0, PessoaFisica.class);
            pessoa.setId(99L);
            return pessoa;
        });

        var response = pessoaFisicaService.create(dto);

        assertEquals(99L, response.id());
        assertEquals("12345678901", response.cpf());
        assertEquals("48999999999", response.telefone());
        assertEquals("murilo@email.com", response.email());
    }

    @Test
    void givenCpfDuplicado_whenCreate_thenLancaBusinessException() {
        var dto = new PessoaFisicaRequestDTO("Murilo", "12345678901", "murilo@email.com", "48999999999", "senha123");
        when(pessoaFisicaRepository.findByCpf("12345678901")).thenReturn(Optional.of(pessoaFisica(1L, "12345678901", "outro@email.com")));

        assertThrows(BusinessException.class, () -> pessoaFisicaService.create(dto));

        verify(pessoaFisicaRepository, never()).save(any());
    }

    @Test
    void givenEmailDeUsuarioExistente_whenCreate_thenLancaBusinessException() {
        var dto = new PessoaFisicaRequestDTO("Murilo", "12345678901", "murilo@email.com", "48999999999", "senha123");

        when(pessoaFisicaRepository.findByCpf("12345678901")).thenReturn(Optional.empty());
        when(pessoaFisicaRepository.findByEmail("murilo@email.com")).thenReturn(Optional.empty());
        when(usuarioService.findByUsername("murilo@email.com")).thenReturn(Optional.of(new Usuario()));

        assertThrows(BusinessException.class, () -> pessoaFisicaService.create(dto));

        verify(usuarioService, never()).save(any());
    }

    @Test
    void givenPessoaFisicaExistente_whenUpdate_thenAtualizaPessoaFisica() {
        var existente = pessoaFisica(1L, "12345678901", "antigo@email.com");
        existente.setNome("Nome Antigo");
        existente.setTelefone("48999998888");

        var dto = new PessoaFisicaRequestDTO(
                "Nome Novo",
                "987.654.321-00",
                "novo@email.com",
                "(48) 99999-7777",
                "senha-nao-usada"
        );

        when(pessoaFisicaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(pessoaFisicaRepository.findByCpf("987.654.321-00")).thenReturn(Optional.empty());
        when(pessoaFisicaRepository.findByEmail("novo@email.com")).thenReturn(Optional.empty());
        when(pessoaFisicaRepository.save(any(PessoaFisica.class))).thenAnswer(invocation -> invocation.getArgument(0, PessoaFisica.class));

        var response = pessoaFisicaService.update(1L, dto);

        assertEquals("Nome Novo", response.nome());
        assertEquals("98765432100", response.cpf());
        assertEquals("48999997777", response.telefone());
        assertEquals("novo@email.com", response.email());
    }

    @Test
    void givenPessoaFisicaInexistente_whenUpdate_thenLancaResourceNotFoundException() {
        var dto = new PessoaFisicaRequestDTO("Nome", "12345678901", "email@test.com", "48999999999", "senha");
        when(pessoaFisicaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pessoaFisicaService.update(1L, dto));
    }

    @Test
    void givenPessoaFisicaInexistente_whenDelete_thenLancaResourceNotFoundException() {
        when(pessoaFisicaRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> pessoaFisicaService.delete(1L));
    }

    private PessoaFisica pessoaFisica(Long id, String cpf, String email) {
        var pessoa = new PessoaFisica();
        pessoa.setId(id);
        pessoa.setNome("Pessoa Teste");
        pessoa.setCpf(cpf);
        pessoa.setEmail(email);
        pessoa.setTelefone("48999999999");
        return pessoa;
    }
}

