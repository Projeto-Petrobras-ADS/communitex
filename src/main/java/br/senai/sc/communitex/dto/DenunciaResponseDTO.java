package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.enums.IssueType;

import java.time.LocalDateTime;

public record DenunciaResponseDTO(
    Long id,
    String titulo,
    String descricao,
    Double latitude,
    Double longitude,
    String fotoUrl,
    IssueStatus status,
    IssueType tipo,
    LocalDateTime dataCriacao,
    Boolean ativa,
    Long autorId,
    String autorNome,
    int totalInteracoes,
    int totalApoios
) {}
