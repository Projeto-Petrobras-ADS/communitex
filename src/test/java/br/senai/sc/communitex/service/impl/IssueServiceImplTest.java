package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.IssueInteractionRequestDTO;
import br.senai.sc.communitex.dto.IssueRequestDTO;
import br.senai.sc.communitex.enums.InteractionType;
import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.enums.IssueType;
import br.senai.sc.communitex.exception.BusinessException;
import br.senai.sc.communitex.exception.DuplicateIssueException;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Issue;
import br.senai.sc.communitex.model.IssueInteraction;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.IssueInteractionRepository;
import br.senai.sc.communitex.repository.IssueRepository;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueServiceImplTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private IssueInteractionRepository interactionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private IssueServiceImpl issueService;

    @BeforeEach
    void setUp() {
        issueService = new IssueServiceImpl(issueRepository, interactionRepository, usuarioRepository, Optional.empty());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenUsuarioAutenticadoEIssueValida_whenCreate_thenCriaIssue() {
        authenticate("cidadao@communitex.com");
        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");

        when(usuarioRepository.findByUsername("cidadao@communitex.com")).thenReturn(Optional.of(autor));
        when(issueRepository.findUnresolvedByType(eq(IssueType.BURACO), anyList())).thenReturn(List.of());
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            var issue = invocation.getArgument(0, Issue.class);
            issue.setId(99L);
            issue.setDataCriacao(LocalDateTime.now());
            return issue;
        });

        var response = issueService.create(new IssueRequestDTO(
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
    void givenIssueDuplicada_whenCreate_thenLancaDuplicateIssueException() {
        authenticate("cidadao@communitex.com");
        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");
        var issueExistente = issue(10L, "Existente", -27.5969, -48.5495, autor, IssueStatus.EM_ANALISE);

        when(usuarioRepository.findByUsername("cidadao@communitex.com")).thenReturn(Optional.of(autor));
        when(issueRepository.findUnresolvedByType(eq(IssueType.BURACO), anyList())).thenReturn(List.of(issueExistente));

        assertThrows(DuplicateIssueException.class, () -> issueService.create(new IssueRequestDTO(
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
    void givenIssueInexistente_whenFindById_thenLancaResourceNotFoundException() {
        when(issueRepository.findById(55L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> issueService.findById(55L));
    }

    @Test
    void givenStatusInvalido_whenUpdateStatus_thenLancaBusinessException() {
        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");
        when(issueRepository.findById(10L)).thenReturn(Optional.of(issue(10L, "Titulo", -27.6, -48.5, autor, IssueStatus.ABERTA)));

        assertThrows(BusinessException.class, () -> issueService.updateStatus(10L, "status-xyz"));
    }

    @Test
    void givenComentarioSemConteudo_whenAddInteraction_thenLancaBusinessException() {
        authenticate("cidadao@communitex.com");

        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");
        var issue = issue(10L, "Titulo", -27.6, -48.5, autor, IssueStatus.ABERTA);

        when(issueRepository.findById(10L)).thenReturn(Optional.of(issue));
        when(usuarioRepository.findByUsername("cidadao@communitex.com")).thenReturn(Optional.of(autor));

        assertThrows(BusinessException.class,
                () -> issueService.addInteraction(10L, new IssueInteractionRequestDTO(InteractionType.COMENTARIO, "   ")));
    }

    @Test
    void givenApoioDuplicado_whenAddInteraction_thenLancaBusinessException() {
        authenticate("cidadao@communitex.com");

        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");
        var issue = issue(10L, "Titulo", -27.6, -48.5, autor, IssueStatus.ABERTA);

        when(issueRepository.findById(10L)).thenReturn(Optional.of(issue));
        when(usuarioRepository.findByUsername("cidadao@communitex.com")).thenReturn(Optional.of(autor));
        when(interactionRepository.findByIssueIdAndUsuarioIdAndTipo(10L, 1L, InteractionType.APOIO))
                .thenReturn(Optional.of(new IssueInteraction()));

        assertThrows(BusinessException.class,
                () -> issueService.addInteraction(10L, new IssueInteractionRequestDTO(InteractionType.APOIO, null)));

        verify(interactionRepository, never()).save(any());
    }

    @Test
    void givenInteracaoValida_whenAddInteraction_thenCriaInteracao() {
        authenticate("cidadao@communitex.com");

        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");
        var issue = issue(10L, "Titulo", -27.6, -48.5, autor, IssueStatus.ABERTA);

        when(issueRepository.findById(10L)).thenReturn(Optional.of(issue));
        when(usuarioRepository.findByUsername("cidadao@communitex.com")).thenReturn(Optional.of(autor));
        when(interactionRepository.save(any(IssueInteraction.class))).thenAnswer(invocation -> {
            var interaction = invocation.getArgument(0, IssueInteraction.class);
            interaction.setId(101L);
            interaction.setDataCriacao(LocalDateTime.now());
            return interaction;
        });

        var response = issueService.addInteraction(10L, new IssueInteractionRequestDTO(InteractionType.COMENTARIO, "Concordo"));

        assertEquals(101L, response.id());
        assertEquals(InteractionType.COMENTARIO, response.tipo());
        assertEquals("Cidadao", response.usuarioNome());
    }

    @Test
    void givenInteractionDeOutraIssue_whenRemoveInteraction_thenLancaBusinessException() {
        authenticate("cidadao@communitex.com");

        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");
        var issueCorreta = issue(99L, "Titulo", -27.6, -48.5, autor, IssueStatus.ABERTA);

        var interaction = IssueInteraction.builder()
                .id(20L)
                .tipo(InteractionType.CURTIDA)
                .issue(issueCorreta)
                .usuario(autor)
                .build();

        when(interactionRepository.findById(20L)).thenReturn(Optional.of(interaction));

        assertThrows(BusinessException.class, () -> issueService.removeInteraction(10L, 20L));
    }

    @Test
    void givenUsuarioDiferente_whenRemoveInteraction_thenLancaForbiddenException() {
        authenticate("outro@communitex.com");

        var autorInteracao = usuario(1L, "cidadao@communitex.com", "Cidadao");
        var usuarioAutenticado = usuario(2L, "outro@communitex.com", "Outro");
        var issue = issue(10L, "Titulo", -27.6, -48.5, autorInteracao, IssueStatus.ABERTA);

        var interaction = IssueInteraction.builder()
                .id(20L)
                .tipo(InteractionType.CURTIDA)
                .issue(issue)
                .usuario(autorInteracao)
                .build();

        when(interactionRepository.findById(20L)).thenReturn(Optional.of(interaction));
        when(usuarioRepository.findByUsername("outro@communitex.com")).thenReturn(Optional.of(usuarioAutenticado));

        assertThrows(ForbiddenException.class, () -> issueService.removeInteraction(10L, 20L));
    }

    @Test
    void givenInteractionDoUsuario_whenRemoveInteraction_thenRemoveInteracao() {
        authenticate("cidadao@communitex.com");

        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");
        var issue = issue(10L, "Titulo", -27.6, -48.5, autor, IssueStatus.ABERTA);

        var interaction = IssueInteraction.builder()
                .id(20L)
                .tipo(InteractionType.CURTIDA)
                .issue(issue)
                .usuario(autor)
                .build();

        when(interactionRepository.findById(20L)).thenReturn(Optional.of(interaction));
        when(usuarioRepository.findByUsername("cidadao@communitex.com")).thenReturn(Optional.of(autor));

        issueService.removeInteraction(10L, 20L);

        verify(interactionRepository).delete(interaction);
    }

    @Test
    void givenIssueInexistente_whenFindInteractionsByIssueId_thenLancaResourceNotFoundException() {
        when(issueRepository.existsById(10L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> issueService.findInteractionsByIssueId(10L));
    }

    @Test
    void givenIssuesComDistanciasDiferentes_whenFindByProximity_thenRetornaApenasProximas() {
        var autor = usuario(1L, "cidadao@communitex.com", "Cidadao");

        var perto = issue(1L, "Perto", -27.5969, -48.5495, autor, IssueStatus.ABERTA);
        var longe = issue(2L, "Longe", -27.7000, -48.7000, autor, IssueStatus.ABERTA);

        when(issueRepository.findAll()).thenReturn(List.of(perto, longe));

        var issues = issueService.findByProximity(-27.5969, -48.5495, 100.0);

        assertEquals(1, issues.size());
        assertEquals("Perto", issues.get(0).titulo());
    }

    private void authenticate(String username) {
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

    private Issue issue(Long id, String titulo, Double latitude, Double longitude, Usuario autor, IssueStatus status) {
        return Issue.builder()
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

