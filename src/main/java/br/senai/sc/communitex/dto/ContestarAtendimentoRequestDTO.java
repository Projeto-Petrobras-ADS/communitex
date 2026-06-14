package br.senai.sc.communitex.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContestarAtendimentoRequestDTO(
        @NotBlank(message = "O motivo da contestacao e obrigatorio")
        @Size(min = 10, max = 2000, message = "O motivo deve ter entre 10 e 2000 caracteres")
        String motivo
) {
}
