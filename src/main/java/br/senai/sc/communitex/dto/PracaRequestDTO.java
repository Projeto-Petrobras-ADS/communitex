package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.StatusPraca;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PracaRequestDTO(
    @NotBlank String nome,
    String logradouro,
    String bairro,
    @NotBlank String cidade,
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
    @Size(max = 1000) String descricao,
    String fotoUrl,
    @NotNull @Positive Double metragemM2,
    StatusPraca status
) {}

