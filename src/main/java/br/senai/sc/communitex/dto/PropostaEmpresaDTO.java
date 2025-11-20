package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.StatusAdocao;

import java.time.LocalDate;

public record PropostaEmpresaDTO(
        Long id,
        Long pracaId,
        String nomePraca,
        String cidadePraca,
        String proposta,
        StatusAdocao status,
        LocalDate dataRegistro,
        LocalDate dataInicio,
        LocalDate dataFim
) {
}

