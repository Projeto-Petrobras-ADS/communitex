package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.Praca;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AdocaoRequestDTO(
        @NotBlank LocalDate dataInicio,
        LocalDate dataFim,
        @NotBlank
        @Size(max = 1000)
        String descricaoProjeto,
        @NotBlank StatusAdocao status,
        Empresa empresa,
        Praca praca

        ) {
}
