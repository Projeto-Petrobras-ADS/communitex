package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.StatusPraca;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.databind.JsonNode;

public record PracaRequestDTO(
    @NotBlank String nome,
    String logradouro,
    String bairro,
    @NotBlank String cidade,
    @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
    @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
    JsonNode poligono,
    @Size(max = 1000) String descricao,
    @Positive Double metragemM2,
    StatusPraca status
) {}

