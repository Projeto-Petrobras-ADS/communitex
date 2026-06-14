package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.DenunciaInteracaoRequestDTO;
import br.senai.sc.communitex.dto.DenunciaRequestDTO;
import br.senai.sc.communitex.enums.InteractionType;
import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.enums.IssueType;
import br.senai.sc.communitex.exception.BusinessException;
import br.senai.sc.communitex.exception.DuplicateIssueException;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Denuncia;
import br.senai.sc.communitex.model.DenunciaInteracao;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.DenunciaInteracaoRepository;
import br.senai.sc.communitex.repository.DenunciaRepository;
import br.senai.sc.communitex.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueServiceImplTest {

    @Mock
    private DenunciaRepository issueRepository;

    @Mock
    private DenunciaInteracaoRepository interactionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private DenunciaServiceImpl issueService;

    @BeforeEach
    void setUp() {
        issueService = new DenunciaServiceImpl(issueRepository, interactionRepository, usuarioRepository, Optional.empty());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void dadoUsuarioAutenticadoEDenunciaValida_aoCriar_deveCriarDenuncia() {
        autenticar("cidadao@communitex.com");
        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");

        when(usuarioRepository.findByUsername("cidadao@communitex.com")).thenReturn(Optional.of(autor));
        when(issueRepository.findUnresolvedByType(eq(IssueType.BURACO), anyList())).thenReturn(List.of());
        when(issueRepository.save(any(Denuncia.class))).thenAnswer(invocation -> {
            var issue = invocation.getArgument(0, Denuncia.class);
            issue.setId(99L);
            issue.setDataCriacao(LocalDateTime.now());
            return issue;
        });

        var response = issueService.criar(new DenunciaRequestDTO(
                "Buraco grande",
                "Existe um buraco perigoso",
                -27.5969,
                -48.5495,
                null,
                IssueType.BURACO
        ));

        assertEquals(99L, response.id());
        assertEquals(IssueStatus.ABERTA, response.status());
        assertEquals("Cidadao", response.autorNome());
    }

    @Test
    void dadaEmpresaAutenticada_aoCriar_deveLancarForbiddenException() {
        autenticar("empresa@communitex.com");
        var empresa = usuario(2L, "empresa@communitex.com", "Empresa");
        empresa.setRole("ROLE_EMPRESA");

        when(usuarioRepository.findByUsername("empresa@communitex.com")).thenReturn(Optional.of(empresa));

        assertThrows(ForbiddenException.class, () -> issueService.criar(new DenunciaRequestDTO(
                "Buraco grande",
                "Existe um buraco perigoso",
                -27.5969,
                -48.5495,
                null,
                IssueType.BURACO
        )));

        verify(issueRepository, never()).save(any());
    }

    @Test
    void dadaDenunciaDuplicada_aoCriar_deveLancarDuplicateIssueException() {
        autenticar("cidadao@communitex.com");
        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");
        var issueExistente = issue(10L, "Existente", -27.5969, -48.5495, autor, IssueStatus.EM_ANALISE);

        when(usuarioRepository.findByUsername("cidadao@communitex.com")).thenReturn(Optional.of(autor));
        when(issueRepository.findUnresolvedByType(eq(IssueType.BURACO), anyList())).thenReturn(List.of(issueExistente));

        assertThrows(DuplicateIssueException.class, () -> issueService.criar(new DenunciaRequestDTO(
                "Buraco novo",
                "Muito perto",
                -27.5969,
                -48.5495,
                null,
                IssueType.BURACO
        )));

        verify(issueRepository, never()).save(any());
    }

    @Test
    void dadaDenunciaInexistente_aoBuscarPorId_deveLancarResourceNotFoundException() {
        when(issueRepository.findById(55L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> issueService.buscarPorId(55L));
    }

    @Test
    void dadoStatusValido_aoAtualizar_deveRetornarStatusAtualizado() {
        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");
        when(issueRepository.findById(10L)).thenReturn(Optional.of(issue(10L, "Titulo", -27.6, -48.5, autor, IssueStatus.ABERTA)));
        when(issueRepository.save(any(Denuncia.class))).thenAnswer(invocation -> invocation.getArgument(0, Denuncia.class));

        var response = issueService.atualizarStatus(10L, IssueStatus.EM_ANALISE);

        assertEquals(IssueStatus.EM_ANALISE, response.status());
    }

    @Test
    void dadoStatusResolvida_aoAtualizarDiretamente_deveExigirConfirmacaoDupla() {
        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");
        when(issueRepository.findById(10L)).thenReturn(Optional.of(issue(10L, "Titulo", -27.6, -48.5, autor, IssueStatus.EM_ANDAMENTO)));

        assertThrows(BusinessException.class, () -> issueService.atualizarStatus(10L, IssueStatus.RESOLVIDA));
        verify(issueRepository, never()).save(any());
    }

    @Test
    void dadoComentarioSemConteudo_aoAdicionarInteracao_deveLancarBusinessException() {
        autenticar("cidadao@communitex.com");

        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");
        var issue = issue(10L, "Titulo", -27.6, -48.5, autor, IssueStatus.ABERTA);

        when(issueRepository.findById(10L)).thenReturn(Optional.of(issue));
        when(usuarioRepository.findByUsername("cidadao@communitex.com")).thenReturn(Optional.of(autor));

        assertThrows(BusinessException.class,
                () -> issueService.adicionarInteracao(10L, new DenunciaInteracaoRequestDTO(InteractionType.COMENTARIO, "   ")));
    }

    @Test
    void dadoApoioDuplicado_aoAdicionarInteracao_deveLancarBusinessException() {
        autenticar("cidadao@communitex.com");

        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");
        var issue = issue(10L, "Titulo", -27.6, -48.5, autor, IssueStatus.ABERTA);

        when(issueRepository.findById(10L)).thenReturn(Optional.of(issue));
        when(usuarioRepository.findByUsername("cidadao@communitex.com")).thenReturn(Optional.of(autor));
        when(interactionRepository.findByIssueIdAndUsuarioIdAndTipo(10L, 1L, InteractionType.APOIO))
                .thenReturn(Optional.of(new DenunciaInteracao()));

        assertThrows(BusinessException.class,
                () -> issueService.adicionarInteracao(10L, new DenunciaInteracaoRequestDTO(InteractionType.APOIO, null)));

        verify(interactionRepository, never()).save(any());
    }

    @Test
    void dadaInteracaoValida_aoAdicionarInteracao_deveCriarInteracao() {
        autenticar("cidadao@communitex.com");

        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");
        var issue = issue(10L, "Titulo", -27.6, -48.5, autor, IssueStatus.ABERTA);

        when(issueRepository.findById(10L)).thenReturn(Optional.of(issue));
        when(usuarioRepository.findByUsername("cidadao@communitex.com")).thenReturn(Optional.of(autor));
        when(interactionRepository.save(any(DenunciaInteracao.class))).thenAnswer(invocation -> {
            var interaction = invocation.getArgument(0, DenunciaInteracao.class);
            interaction.setId(101L);
            interaction.setDataCriacao(LocalDateTime.now());
            return interaction;
        });

        var response = issueService.adicionarInteracao(10L, new DenunciaInteracaoRequestDTO(InteractionType.COMENTARIO, "Concordo"));

        assertEquals(101L, response.id());
        assertEquals(InteractionType.COMENTARIO, response.tipo());
        assertEquals("Cidadao", response.usuarioNome());
    }

    @Test
    void dadaInteracaoDeOutraDenuncia_aoRemover_deveLancarBusinessException() {
        autenticar("cidadao@communitex.com");

        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");
        var issueCorreta = issue(99L, "Titulo", -27.6, -48.5, autor, IssueStatus.ABERTA);

        var interaction = DenunciaInteracao.builder()
                .id(20L)
                .tipo(InteractionType.CURTIDA)
                .issue(issueCorreta)
                .usuario(autor)
                .build();

        when(interactionRepository.findById(20L)).thenReturn(Optional.of(interaction));

        assertThrows(BusinessException.class, () -> issueService.removerInteracao(10L, 20L));
    }

    @Test
    void dadoUsuarioDiferente_aoRemoverInteracao_deveLancarForbiddenException() {
        autenticar("outro@communitex.com");

        var autorInteracao = usuario(1L, "cidadao@communitex.com", "Cidadao");
        var usuarioAutenticado = usuario(2L, "outro@communitex.com", "Outro");
        var issue = issue(10L, "Titulo", -27.6, -48.5, autorInteracao, IssueStatus.ABERTA);

        var interaction = DenunciaInteracao.builder()
                .id(20L)
                .tipo(InteractionType.CURTIDA)
                .issue(issue)
                .usuario(autorInteracao)
                .build();

        when(interactionRepository.findById(20L)).thenReturn(Optional.of(interaction));
        when(usuarioRepository.findByUsername("outro@communitex.com")).thenReturn(Optional.of(usuarioAutenticado));

        assertThrows(ForbiddenException.class, () -> issueService.removerInteracao(10L, 20L));
    }

    @Test
    void dadaInteracaoDoUsuario_aoRemoverInteracao_deveRemoverInteracao() {
        autenticar("cidadao@communitex.com");

        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");
        var issue = issue(10L, "Titulo", -27.6, -48.5, autor, IssueStatus.ABERTA);

        var interaction = DenunciaInteracao.builder()
                .id(20L)
                .tipo(InteractionType.CURTIDA)
                .issue(issue)
                .usuario(autor)
                .build();

        when(interactionRepository.findById(20L)).thenReturn(Optional.of(interaction));
        when(usuarioRepository.findByUsername("cidadao@communitex.com")).thenReturn(Optional.of(autor));

        issueService.removerInteracao(10L, 20L);

        verify(interactionRepository).delete(interaction);
    }

    @Test
    void dadaDenunciaInexistente_aoListarInteracoes_deveLancarResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> issueService.listarInteracoesDaDenuncia(10L));
    }

    @Test
    void dadasDenunciasComDistanciasDiferentes_aoBuscarPorProximidade_deveRetornarApenasProximas() {
        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");

        var perto = issue(1L, "Perto", -27.5969, -48.5495, autor, IssueStatus.ABERTA);
        var longe = issue(2L, "Longe", -27.7000, -48.7000, autor, IssueStatus.ABERTA);

        when(issueRepository.findByLatitudeBetweenAndLongitudeBetween(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        )).thenReturn(List.of(perto, longe));

        var issues = issueService.buscarPorProximidade(-27.5969, -48.5495, 100.0);

        assertEquals(1, issues.size());
        assertEquals("Perto", issues.get(0).titulo());
    }

    private void autenticar(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, "secret")
        );
    }

    private Usuario usuario(Long id, String username, String nome) {
        var usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setNome(nome);
        usuario.setRole("ROLE_USER");
        return usuario;
    }

    private Denuncia issue(Long id, String titulo, Double latitude, Double longitude, Usuario autor, IssueStatus status) {
        return Denuncia.builder()
                .id(id)
                .titulo(titulo)
                .descricao("Descricao")
                .latitude(latitude)
                .longitude(longitude)
                .status(status)
                .tipo(IssueType.BURACO)
                .fotoUrl(null)
                .autor(autor)
                .interacoes(List.of())
                .dataCriacao(LocalDateTime.now())
                .build();
    }
}

