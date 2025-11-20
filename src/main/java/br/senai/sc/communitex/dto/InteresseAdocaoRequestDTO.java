package br.senai.sc.communitex.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InteresseAdocaoRequestDTO(
        @NotNull(message = "O ID da praça é obrigatório")
        Long pracaId,

        @NotBlank(message = "A proposta é obrigatória")
        String proposta
) {
}

