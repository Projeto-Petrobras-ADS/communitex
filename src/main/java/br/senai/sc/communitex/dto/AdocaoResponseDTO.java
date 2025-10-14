package br.senai.sc.communitex.dto;


import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.Praca;

import java.time.LocalDate;

public record AdocaoResponseDTO (
        Long id,
        LocalDate dataInicio,
        LocalDate dataFim,
        String descricaoProjeto,
        StatusAdocao status,
        Empresa empresa,
        Praca praca
){
}
