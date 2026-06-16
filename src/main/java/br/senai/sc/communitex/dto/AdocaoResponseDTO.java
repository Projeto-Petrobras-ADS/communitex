package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.StatusAdocao;

import java.time.LocalDate;

public record AdocaoResponseDTO(
        Long id,
        LocalDate dataInicio,
        LocalDate dataFim,
        String descricaoProjeto,
        StatusAdocao status,
        Long empresaId,
        String empresaNomeFantasia,
        Long pracaId,
        String pracaNome,
        String pracaCidade
) {
}
