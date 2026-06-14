package br.senai.sc.communitex.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConcluirAtendimentoRequestDTO(
        @NotBlank(message = "A descricao do reparo e obrigatoria")
        @Size(min = 10, max = 2000, message = "A descricao do reparo deve ter entre 10 e 2000 caracteres")
        String descricaoReparo
) {
}
