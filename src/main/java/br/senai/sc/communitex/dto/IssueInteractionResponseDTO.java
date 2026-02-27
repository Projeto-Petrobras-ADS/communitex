package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.InteractionType;

import java.time.LocalDateTime;

public record IssueInteractionResponseDTO(
    Long id,
    InteractionType tipo,
    String conteudo,
    LocalDateTime dataCriacao,
    Long usuarioId,
    String usuarioNome
) {}

