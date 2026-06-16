package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DenunciaRequestDTO(
    @NotBlank(message = "O título é obrigatório")
    @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
    String titulo,

    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
    String descricao,

    @NotNull(message = "A latitude é obrigatória")
    Double latitude,

    @NotNull(message = "A longitude é obrigatória")
    Double longitude,

    @NotNull(message = "O tipo da denúncia é obrigatório")
    IssueType tipo
) {}

