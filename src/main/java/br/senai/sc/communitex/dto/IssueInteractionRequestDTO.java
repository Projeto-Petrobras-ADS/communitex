package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.InteractionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IssueInteractionRequestDTO(
    @NotNull(message = "O tipo de interação é obrigatório")
    InteractionType tipo,

    @Size(max = 1000, message = "O conteúdo deve ter no máximo 1000 caracteres")
    String conteudo
) {}

