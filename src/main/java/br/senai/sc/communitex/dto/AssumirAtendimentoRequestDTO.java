package br.senai.sc.communitex.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssumirAtendimentoRequestDTO(
        @NotBlank(message = "A descricao planejada e obrigatoria")
        @Size(min = 10, max = 2000, message = "A descricao planejada deve ter entre 10 e 2000 caracteres")
        String descricaoPlanejada
) {
}
