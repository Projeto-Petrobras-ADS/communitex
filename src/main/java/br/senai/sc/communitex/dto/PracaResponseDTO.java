package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.StatusPraca;

public record PracaResponseDTO(
    Long id,
    String nome,
    String logradouro,
    String bairro,
    String cidade,
    Double latitude,
    Double longitude,
    String descricao,
    String fotoUrl,
    StatusPraca status
) {}

