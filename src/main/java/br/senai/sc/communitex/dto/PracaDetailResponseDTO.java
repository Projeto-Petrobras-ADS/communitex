package br.senai.sc.communitex.dto;


import br.senai.sc.communitex.enums.StatusPraca;

import java.util.List;

public record PracaDetailResponseDTO(
        Long id,
        String nome,
        String logradouro,
        String bairro,
        String cidade,
        Double latitude,
        Double longitude,
        String descricao,
        String fotoUrl,
        Double metragemM2,
        StatusPraca status,
        PessoaFisicaSimpleDTO cadastradoPor,
        List<AdocaoHistoricoDTO> historicoInteresses
) {
}

