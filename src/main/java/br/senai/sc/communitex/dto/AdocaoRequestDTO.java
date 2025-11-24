package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.StatusAdocao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AdocaoRequestDTO(
        @NotNull LocalDate dataInicio,
        LocalDate dataFim,
        @NotNull
        @Size(max = 1000)
        String descricaoProjeto,
        StatusAdocao status,
        @NotNull Long pracaId

        ) {
}
