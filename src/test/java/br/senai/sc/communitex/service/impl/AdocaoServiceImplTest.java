package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.InteresseAdocaoRequestDTO;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.repository.AdocaoRepository;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import br.senai.sc.communitex.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdocaoServiceImplTest {

    @Mock
    private AdocaoRepository adocaoRepository;

    @Mock
    private PracaRepository pracaRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AdocaoServiceImpl adocaoService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenEmpresaEPracaComResponsavel_whenRegistrarInteresse_thenCriaProposta() {
        authenticate("empresa@communitex.com");

        var empresa = empresa(10L, "Empresa Teste");
        var responsavel = pessoaFisica(30L, "Murilo", "murilo@email.com");
        var praca = praca(20L, "Praca Central", responsavel);

        when(empresaRepository.findByUsuarioRepresentanteUsername("empresa@communitex.com"))
                .thenReturn(Optional.of(empresa));
        when(pracaRepository.findById(20L)).thenReturn(Optional.of(praca));
        when(adocaoRepository.save(any(Adocao.class))).thenAnswer(invocation -> {
            var adocao = invocation.getArgument(0, Adocao.class);
            adocao.setId(99L);
            return adocao;
        });

        var response = adocaoService.registrarInteresse(new InteresseAdocaoRequestDTO(20L, "Projeto verde"));

        assertEquals(99L, response.id());
        assertEquals(StatusAdocao.PROPOSTA, response.status());
        assertEquals(10L, response.empresaId());
        assertEquals(20L, response.pracaId());
        verify(notificationService).notificarInteresseAdocao(eq(responsavel), eq(empresa), eq(praca), eq("Projeto verde"), any());
    }

    @Test
    void givenPracaInexistente_whenRegistrarInteresse_thenLancaResourceNotFoundException() {
        authenticate("empresa@communitex.com");

        when(empresaRepository.findByUsuarioRepresentanteUsername("empresa@communitex.com"))
                .thenReturn(Optional.of(empresa(10L, "Empresa Teste")));
        when(pracaRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adocaoService.registrarInteresse(new InteresseAdocaoRequestDTO(77L, "Projeto")));

        verify(adocaoRepository, never()).save(any());
        verify(notificationService, never()).notificarInteresseAdocao(any(), any(), any(), any(), any());
    }

    @Test
    void givenPracaSemResponsavel_whenRegistrarInteresse_thenLancaResourceNotFoundException() {
        authenticate("empresa@communitex.com");

        var empresa = empresa(10L, "Empresa Teste");
        var pracaSemResponsavel = praca(20L, "Praca Sem Responsavel", null);

        when(empresaRepository.findByUsuarioRepresentanteUsername("empresa@communitex.com"))
                .thenReturn(Optional.of(empresa));
        when(pracaRepository.findById(20L)).thenReturn(Optional.of(pracaSemResponsavel));

        assertThrows(ResourceNotFoundException.class,
                () -> adocaoService.registrarInteresse(new InteresseAdocaoRequestDTO(20L, "Projeto")));

        verify(adocaoRepository, never()).save(any());
    }

    @Test
    void givenEmpresaAutenticada_whenListarPropostas_thenRetornaLista() {
        authenticate("empresa@communitex.com");

        var empresa = empresa(10L, "Empresa Teste");
        var praca = praca(20L, "Praca Central", pessoaFisica(30L, "Murilo", "murilo@email.com"));

        var adocao = Adocao.builder()
                .id(1L)
                .empresa(empresa)
                .praca(praca)
                .descricaoProjeto("Projeto 1")
                .status(StatusAdocao.PROPOSTA)
                .dataInicio(LocalDate.now())
                .build();

        when(empresaRepository.findByUsuarioRepresentanteUsername("empresa@communitex.com"))
                .thenReturn(Optional.of(empresa));
        when(adocaoRepository.findByEmpresaId(10L)).thenReturn(List.of(adocao));

        var propostas = adocaoService.listarPropostasMinhasEmpresa();

        assertEquals(1, propostas.size());
        assertEquals("Projeto 1", propostas.get(0).proposta());
        assertEquals("Praca Central", propostas.get(0).nomePraca());
    }

    @Test
    void givenUsuarioSemEmpresa_whenListarPropostas_thenLancaForbiddenException() {
        authenticate("sem-empresa@communitex.com");

        when(empresaRepository.findByUsuarioRepresentanteUsername("sem-empresa@communitex.com"))
                .thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> adocaoService.listarPropostasMinhasEmpresa());
    }

    @Test
    void givenPrincipalInvalido_whenListarPropostas_thenLancaForbiddenException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new Object(), "N/A")
        );

        assertThrows(ForbiddenException.class, () -> adocaoService.listarPropostasMinhasEmpresa());
    }

    @Test
    void givenFalhaNaNotificacao_whenRegistrarInteresse_thenNaoPropagaExcecao() {
        authenticate("empresa@communitex.com");

        var empresa = empresa(10L, "Empresa Teste");
        var responsavel = pessoaFisica(30L, "Murilo", "murilo@email.com");
        var praca = praca(20L, "Praca Central", responsavel);

        when(empresaRepository.findByUsuarioRepresentanteUsername("empresa@communitex.com"))
                .thenReturn(Optional.of(empresa));
        when(pracaRepository.findById(20L)).thenReturn(Optional.of(praca));
        when(adocaoRepository.save(any(Adocao.class))).thenReturn(
                Adocao.builder()
                        .id(99L)
                        .praca(praca)
                        .empresa(empresa)
                        .descricaoProjeto("Projeto verde")
                        .status(StatusAdocao.PROPOSTA)
                        .dataInicio(LocalDate.now())
                        .build()
        );

        assertDoesNotThrow(() -> adocaoService.registrarInteresse(new InteresseAdocaoRequestDTO(20L, "Projeto verde")));
    }

    private void authenticate(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, "secret")
        );
    }

    private Empresa empresa(Long id, String razaoSocial) {
        var empresa = new Empresa();
        empresa.setId(id);
        empresa.setRazaoSocial(razaoSocial);
        empresa.setEmail("contato@empresa.com");
        empresa.setTelefone("48999990000");
        return empresa;
    }

    private PessoaFisica pessoaFisica(Long id, String nome, String email) {
        var pessoa = new PessoaFisica();
        pessoa.setId(id);
        pessoa.setNome(nome);
        pessoa.setEmail(email);
        return pessoa;
    }

    private Praca praca(Long id, String nome, PessoaFisica responsavel) {
        var praca = new Praca();
        praca.setId(id);
        praca.setNome(nome);
        praca.setCidade("Florianopolis");
        praca.setMetragemM2(1200.0);
        praca.setCadastradoPor(responsavel);
        return praca;
    }
}


