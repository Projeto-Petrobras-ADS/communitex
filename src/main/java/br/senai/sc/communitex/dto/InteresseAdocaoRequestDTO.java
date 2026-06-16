package br.senai.sc.communitex.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InteresseAdocaoRequestDTO(
        @NotNull(message = "O ID da praça é obrigatório")
        Long pracaId,

        @NotBlank(message = "A proposta é obrigatória")
        @Size(min = 20, max = 2000, message = "A proposta deve ter entre 20 e 2000 caracteres")
        String proposta
) {
}

