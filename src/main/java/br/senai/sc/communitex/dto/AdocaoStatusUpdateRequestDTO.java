package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.StatusAdocao;
import jakarta.validation.constraints.NotNull;

public record AdocaoStatusUpdateRequestDTO(
        @NotNull(message = "O status é obrigatório")
        StatusAdocao status
) {
}
