package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.Praca;

import java.time.LocalDate;

public record AdocaoStatusResponseDTO(
        Long id,
        String descricaoProjeto,
        LocalDate dataInicio,
        LocalDate dataFim,
        StatusAdocao status,
        String nomeEmpresa,
        String nomePraca
) {
}
