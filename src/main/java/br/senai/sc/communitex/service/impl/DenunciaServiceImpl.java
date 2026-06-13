package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.DenunciaDetailResponseDTO;
import br.senai.sc.communitex.dto.DenunciaInteracaoRequestDTO;
import br.senai.sc.communitex.dto.DenunciaInteracaoResponseDTO;
import br.senai.sc.communitex.dto.DenunciaRequestDTO;
import br.senai.sc.communitex.dto.DenunciaResponseDTO;
import br.senai.sc.communitex.enums.InteractionType;
import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.exception.BusinessException;
import br.senai.sc.communitex.exception.DuplicateIssueException;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.gateway.PhotoStorageGateway;
import br.senai.sc.communitex.model.Denuncia;
import br.senai.sc.communitex.model.DenunciaInteracao;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.DenunciaInteracaoRepository;
import br.senai.sc.communitex.repository.DenunciaRepository;
import br.senai.sc.communitex.repository.UsuarioRepository;
import br.senai.sc.communitex.service.DenunciaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DenunciaServiceImpl implements DenunciaService {

    private static final double EARTH_RADIUS_METERS = 6_371_000;
    private static final double DUPLICATE_RADIUS_METERS = 20.0;
    private static final List<IssueStatus> RESOLVED_STATUSES = List.of(IssueStatus.RESOLVIDA, IssueStatus.REJEITADA);

    private final DenunciaRepository issueRepository;
    private final DenunciaInteracaoRepository interactionRepository;
    private final UsuarioRepository usuarioRepository;
    private final Optional<PhotoStorageGateway> photoStorageGateway;

    @Override
    @Transactional
    public DenunciaResponseDTO criar(DenunciaRequestDTO dto) {
        var autor = getAuthenticatedUser();

        verificarDuplicidadeDaDenuncia(dto);

        var issue = Denuncia.builder()
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
    public DenunciaResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarDenunciaPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public DenunciaDetailResponseDTO buscarPorIdComDetalhes(Long id) {
        return toDetailResponseDTO(buscarDenunciaPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DenunciaResponseDTO> listarTodas() {
        return issueRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DenunciaResponseDTO> listarTodas(Pageable pageable) {
        return issueRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DenunciaResponseDTO> buscarPorProximidade(Double latitude, Double longitude, Double radiusMeters) {
        validarBuscaPorProximidade(latitude, longitude, radiusMeters);

        var latitudeDelta = radiusMeters / 111_320.0;
        var longitudeScale = Math.max(Math.cos(Math.toRadians(latitude)), 0.01);
        var longitudeDelta = radiusMeters / (111_320.0 * longitudeScale);

        return issueRepository.findByLatitudeBetweenAndLongitudeBetween(
                        latitude - latitudeDelta,
                        latitude + latitudeDelta,
                        longitude - longitudeDelta,
                        longitude + longitudeDelta
                ).stream()
            .filter(issue -> calcularDistanciaHaversine(
                        latitude, longitude,
                        issue.getLatitude(), issue.getLongitude()) <= radiusMeters)
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public DenunciaResponseDTO atualizarStatus(Long id, IssueStatus status) {
        var issue = buscarDenunciaPorId(id);

        issue.setStatus(status);
        log.info("Status da denúncia ID: {} atualizado para: {}", id, status);
        return toResponseDTO(issueRepository.save(issue));
    }

    @Override
    @Transactional
    public DenunciaInteracaoResponseDTO adicionarInteracao(Long issueId, DenunciaInteracaoRequestDTO dto) {
        var issue = buscarDenunciaPorId(issueId);

        var usuario = getAuthenticatedUser();

        validarInteracaoUnica(issueId, usuario, dto);
        validarConteudoComentario(dto);

        var interaction = DenunciaInteracao.builder()
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
    public void removerInteracao(Long issueId, Long interactionId) {
        var interaction = buscarInteracaoPorId(interactionId);

        validarInteracaoPertenceDaDenuncia(issueId, interaction);

        var usuario = getAuthenticatedUser();
        if (!interaction.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Você não tem permissão para remover esta interação");
        }

        interactionRepository.delete(interaction);
        log.info("Interação ID: {} removida da denúncia ID: {} pelo usuário: {}", interactionId, issueId, usuario.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DenunciaInteracaoResponseDTO> listarInteracoesDaDenuncia(Long issueId) {
        buscarDenunciaPorId(issueId);

        return interactionRepository.findByIssueIdOrderByDataCriacaoDesc(issueId).stream()
                .map(this::toInteractionResponseDTO)
                .toList();
    }

    private void verificarDuplicidadeDaDenuncia(DenunciaRequestDTO dto) {
        var unresolvedSameType = issueRepository.findUnresolvedByType(dto.tipo(), RESOLVED_STATUSES);

        for (var existing : unresolvedSameType) {
            var distance = calcularDistanciaHaversine(
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

    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
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

    private void validarBuscaPorProximidade(Double latitude, Double longitude, Double radiusMeters) {
        if (latitude == null || latitude < -90 || latitude > 90) {
            throw new BusinessException("Latitude inválida");
        }
        if (longitude == null || longitude < -180 || longitude > 180) {
            throw new BusinessException("Longitude inválida");
        }
        if (radiusMeters == null || radiusMeters <= 0 || radiusMeters > 50_000) {
            throw new BusinessException("O raio deve estar entre 1 e 50000 metros");
        }
    }

    private Denuncia buscarDenunciaPorId(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Denúncia não encontrada com ID: " + id));
    }

    private DenunciaInteracao buscarInteracaoPorId(Long interactionId) {
        return interactionRepository.findById(interactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interação não encontrada com ID: " + interactionId));
    }

    private void validarInteracaoUnica(Long issueId, Usuario usuario, DenunciaInteracaoRequestDTO dto) {
        if (dto.tipo() != InteractionType.APOIO && dto.tipo() != InteractionType.CURTIDA) {
            return;
        }

        interactionRepository.findByIssueIdAndUsuarioIdAndTipo(issueId, usuario.getId(), dto.tipo())
                .ifPresent(existing -> {
                    throw new BusinessException("Você já registrou um(a) " + dto.tipo().name().toLowerCase() + " para esta denúncia");
                });
    }

    private void validarConteudoComentario(DenunciaInteracaoRequestDTO dto) {
        if (dto.tipo() == InteractionType.COMENTARIO && (dto.conteudo() == null || dto.conteudo().isBlank())) {
            throw new BusinessException("O conteúdo é obrigatório para comentários");
        }
    }

    private void validarInteracaoPertenceDaDenuncia(Long issueId, DenunciaInteracao interaction) {
        if (!interaction.getIssue().getId().equals(issueId)) {
            throw new BusinessException("A interação não pertence à denúncia informada");
        }
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

    private DenunciaResponseDTO toResponseDTO(Denuncia issue) {
        var interacoes = obterInteracoesDaDenuncia(issue);

        return new DenunciaResponseDTO(
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
                obterNomeUsuario(issue.getAutor()),
                interacoes.size(),
                contarInteracoesPorTipo(interacoes, InteractionType.APOIO)
        );
    }

    private DenunciaDetailResponseDTO toDetailResponseDTO(Denuncia issue) {
            var interacoes = obterInteracoesDaDenuncia(issue).stream()
                .map(this::toInteractionResponseDTO)
                .toList();

            var interacoesDaDenuncia = obterInteracoesDaDenuncia(issue);

        return new DenunciaDetailResponseDTO(
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
                obterNomeUsuario(issue.getAutor()),
                contarInteracoesPorTipo(interacoesDaDenuncia, InteractionType.APOIO),
                contarInteracoesPorTipo(interacoesDaDenuncia, InteractionType.CURTIDA),
                interacoes
        );
    }

    private DenunciaInteracaoResponseDTO toInteractionResponseDTO(DenunciaInteracao interaction) {
        return new DenunciaInteracaoResponseDTO(
                interaction.getId(),
                interaction.getTipo(),
                interaction.getConteudo(),
                interaction.getDataCriacao(),
                interaction.getUsuario().getId(),
                obterNomeUsuario(interaction.getUsuario())
        );
    }

    private List<DenunciaInteracao> obterInteracoesDaDenuncia(Denuncia issue) {
        return issue.getInteracoes() != null ? issue.getInteracoes() : List.of();
    }

    private int contarInteracoesPorTipo(List<DenunciaInteracao> interacoes, InteractionType tipo) {
        return (int) interacoes.stream()
                .filter(interacao -> interacao.getTipo() == tipo)
                .count();
    }

    private String obterNomeUsuario(Usuario usuario) {
        return usuario.getNome() != null ? usuario.getNome() : usuario.getUsername();
    }
}

