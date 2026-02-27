package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.enums.IssueType;

import java.time.LocalDateTime;
import java.util.List;

public record IssueDetailResponseDTO(
    Long id,
    String titulo,
    String descricao,
    Double latitude,
    Double longitude,
    String fotoUrl,
    IssueStatus status,
    IssueType tipo,
    LocalDateTime dataCriacao,
    Long autorId,
    String autorNome,
    int totalApoios,
    int totalCurtidas,
    List<IssueInteractionResponseDTO> interacoes
) {}

