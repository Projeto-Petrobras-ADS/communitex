package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.StatusAdocao;

import java.time.LocalDate;

public record InteresseAdocaoResponseDTO(
        Long id,
        Long pracaId,
        String nomePraca,
        Long empresaId,
        String nomeEmpresa,
        String proposta,
        StatusAdocao status,
        LocalDate dataRegistro
) {
}

