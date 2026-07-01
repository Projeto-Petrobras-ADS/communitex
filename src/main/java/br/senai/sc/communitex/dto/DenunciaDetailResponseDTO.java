package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.enums.IssueType;

import java.time.LocalDateTime;
import java.util.List;

public record DenunciaDetailResponseDTO(
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
    int totalApoios,
    int totalCurtidas,
    List<DenunciaInteracaoResponseDTO> interacoes
) {}
