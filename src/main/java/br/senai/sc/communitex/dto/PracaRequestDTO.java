package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.StatusPraca;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PracaRequestDTO(
    @NotBlank String nome,
    String logradouro,
    String bairro,
    @NotBlank String cidade,
    Double latitude,
    Double longitude,
    @Size(max = 1000) String descricao,
    String fotoUrl,
    Double metragemM2,
    StatusPraca status
) {}

