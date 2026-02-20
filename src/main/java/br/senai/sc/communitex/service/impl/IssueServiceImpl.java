package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.IssueDetailResponseDTO;
import br.senai.sc.communitex.dto.IssueInteractionRequestDTO;
import br.senai.sc.communitex.dto.IssueInteractionResponseDTO;
import br.senai.sc.communitex.dto.IssueRequestDTO;
import br.senai.sc.communitex.dto.IssueResponseDTO;
import br.senai.sc.communitex.enums.InteractionType;
import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.exception.BusinessException;
import br.senai.sc.communitex.exception.DuplicateIssueException;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.gateway.PhotoStorageGateway;
import br.senai.sc.communitex.model.Issue;
import br.senai.sc.communitex.model.IssueInteraction;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.IssueInteractionRepository;
import br.senai.sc.communitex.repository.IssueRepository;
import br.senai.sc.communitex.repository.UsuarioRepository;
import br.senai.sc.communitex.service.IssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueServiceImpl implements IssueService {

    private static final double EARTH_RADIUS_METERS = 6_371_000;
    private static final double DUPLICATE_RADIUS_METERS = 20.0;
    private static final List<IssueStatus> RESOLVED_STATUSES = List.of(IssueStatus.RESOLVIDA, IssueStatus.REJEITADA);

    private final IssueRepository issueRepository;
    private final IssueInteractionRepository interactionRepository;
    private final UsuarioRepository usuarioRepository;
    private final Optional<PhotoStorageGateway> photoStorageGateway;

    @Override
    @Transactional
    public IssueResponseDTO create(IssueRequestDTO dto) {
        var autor = getAuthenticatedUser();

        checkForDuplicateIssue(dto);

        var issue = Issue.builder()
                .titulo(dto.titulo())
                .descricao(dto.descricao())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .fotoUrl(dto.fotoUrl())
                .tipo(dto.tipo())
                .status(IssueStatus.ABERTA)
                .autor(autor)
                .build();

        var saved = issueRepository.save(issue);
        log.info("Denúncia criada com ID: {} pelo usuário: {}", saved.getId(), autor.getUsername());
        return toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public IssueResponseDTO findById(Long id) {
        var issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Denúncia não encontrada com ID: " + id));
        return toResponseDTO(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public IssueDetailResponseDTO findByIdWithDetails(Long id) {
        var issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Denúncia não encontrada com ID: " + id));
        return toDetailResponseDTO(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponseDTO> findAll() {
        return issueRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponseDTO> findByProximity(Double latitude, Double longitude, Double radiusMeters) {
        return issueRepository.findAll().stream()
                .filter(issue -> calculateHaversineDistance(
                        latitude, longitude,
                        issue.getLatitude(), issue.getLongitude()) <= radiusMeters)
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public IssueResponseDTO updateStatus(Long id, String status) {
        var issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Denúncia não encontrada com ID: " + id));

        try {
            var newStatus = IssueStatus.valueOf(status.toUpperCase());
            issue.setStatus(newStatus);
            log.info("Status da denúncia ID: {} atualizado para: {}", id, newStatus);
            return toResponseDTO(issueRepository.save(issue));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Status inválido: " + status);
        }
    }

    @Override
    @Transactional
    public IssueInteractionResponseDTO addInteraction(Long issueId, IssueInteractionRequestDTO dto) {
        var issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Denúncia não encontrada com ID: " + issueId));

        var usuario = getAuthenticatedUser();

        if (dto.tipo() == InteractionType.APOIO || dto.tipo() == InteractionType.CURTIDA) {
            interactionRepository.findByIssueIdAndUsuarioIdAndTipo(issueId, usuario.getId(), dto.tipo())
                    .ifPresent(existing -> {
                        throw new BusinessException("Você já registrou um(a) " + dto.tipo().name().toLowerCase() + " para esta denúncia");
                    });
        }

        if (dto.tipo() == InteractionType.COMENTARIO && (dto.conteudo() == null || dto.conteudo().isBlank())) {
            throw new BusinessException("O conteúdo é obrigatório para comentários");
        }

        var interaction = IssueInteraction.builder()
                .tipo(dto.tipo())
                .conteudo(dto.conteudo())
                .issue(issue)
                .usuario(usuario)
                .build();

        var saved = interactionRepository.save(interaction);
        log.info("Interação {} adicionada à denúncia ID: {} pelo usuário: {}", dto.tipo(), issueId, usuario.getUsername());
        return toInteractionResponseDTO(saved);
    }

    @Override
    @Transactional
    public void removeInteraction(Long issueId, Long interactionId) {
        var interaction = interactionRepository.findById(interactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interação não encontrada com ID: " + interactionId));

        if (!interaction.getIssue().getId().equals(issueId)) {
            throw new BusinessException("A interação não pertence à denúncia informada");
        }

        var usuario = getAuthenticatedUser();
        if (!interaction.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Você não tem permissão para remover esta interação");
        }

        interactionRepository.delete(interaction);
        log.info("Interação ID: {} removida da denúncia ID: {} pelo usuário: {}", interactionId, issueId, usuario.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueInteractionResponseDTO> findInteractionsByIssueId(Long issueId) {
        if (!issueRepository.existsById(issueId)) {
            throw new ResourceNotFoundException("Denúncia não encontrada com ID: " + issueId);
        }

        return interactionRepository.findByIssueIdOrderByDataCriacaoDesc(issueId).stream()
                .map(this::toInteractionResponseDTO)
                .toList();
    }

    private void checkForDuplicateIssue(IssueRequestDTO dto) {
        var unresolvedSameType = issueRepository.findUnresolvedByType(dto.tipo(), RESOLVED_STATUSES);

        for (var existing : unresolvedSameType) {
            var distance = calculateHaversineDistance(
                    dto.latitude(), dto.longitude(),
                    existing.getLatitude(), existing.getLongitude()
            );

            if (distance <= DUPLICATE_RADIUS_METERS) {
                throw new DuplicateIssueException(
                        String.format("Já existe uma denúncia do tipo '%s' não resolvida a %.1f metros de distância (ID: %d). " +
                                        "Considere apoiar a denúncia existente.",
                                dto.tipo().name(), distance, existing.getId())
                );
            }
        }
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        var lat1Rad = Math.toRadians(lat1);
        var lat2Rad = Math.toRadians(lat2);
        var deltaLat = Math.toRadians(lat2 - lat1);
        var deltaLon = Math.toRadians(lon2 - lon1);

        var a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    private Usuario getAuthenticatedUser() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof String str) {
            username = str;
        } else {
            throw new ForbiddenException("Usuário autenticado não encontrado no contexto");
        }

        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ForbiddenException("Usuário não encontrado: " + username));
    }

    private IssueResponseDTO toResponseDTO(Issue issue) {
        var interacoes = issue.getInteracoes();
        var totalInteracoes = interacoes != null ? interacoes.size() : 0;
        var totalApoios = interacoes != null
                ? (int) interacoes.stream()
                        .filter(i -> i.getTipo() == InteractionType.APOIO)
                        .count()
                : 0;

        var autorNome = issue.getAutor().getNome() != null
                ? issue.getAutor().getNome()
                : issue.getAutor().getUsername();

        return new IssueResponseDTO(
                issue.getId(),
                issue.getTitulo(),
                issue.getDescricao(),
                issue.getLatitude(),
                issue.getLongitude(),
                issue.getFotoUrl(),
                issue.getStatus(),
                issue.getTipo(),
                issue.getDataCriacao(),
                issue.getAutor().getId(),
                autorNome,
                totalInteracoes,
                totalApoios
        );
    }

    private IssueDetailResponseDTO toDetailResponseDTO(Issue issue) {
        var interacoes = issue.getInteracoes().stream()
                .map(this::toInteractionResponseDTO)
                .toList();

        var totalApoios = (int) issue.getInteracoes().stream()
                .filter(i -> i.getTipo() == InteractionType.APOIO)
                .count();

        var totalCurtidas = (int) issue.getInteracoes().stream()
                .filter(i -> i.getTipo() == InteractionType.CURTIDA)
                .count();

        var autorNome = issue.getAutor().getNome() != null
                ? issue.getAutor().getNome()
                : issue.getAutor().getUsername();

        return new IssueDetailResponseDTO(
                issue.getId(),
                issue.getTitulo(),
                issue.getDescricao(),
                issue.getLatitude(),
                issue.getLongitude(),
                issue.getFotoUrl(),
                issue.getStatus(),
                issue.getTipo(),
                issue.getDataCriacao(),
                issue.getAutor().getId(),
                autorNome,
                totalApoios,
                totalCurtidas,
                interacoes
        );
    }

    private IssueInteractionResponseDTO toInteractionResponseDTO(IssueInteraction interaction) {
        var usuarioNome = interaction.getUsuario().getNome() != null
                ? interaction.getUsuario().getNome()
                : interaction.getUsuario().getUsername();

        return new IssueInteractionResponseDTO(
                interaction.getId(),
                interaction.getTipo(),
                interaction.getConteudo(),
                interaction.getDataCriacao(),
                interaction.getUsuario().getId(),
                usuarioNome
        );
    }
}

