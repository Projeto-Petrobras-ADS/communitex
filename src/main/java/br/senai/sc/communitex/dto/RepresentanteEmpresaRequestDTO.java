package br.senai.sc.communitex.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RepresentanteEmpresaRequestDTO(
        @NotBlank(message = "O campo Nome é obrigatório!")
        String nome,

        @NotNull(message = "O campo Ativo é obrigatório!")
        Boolean ativo,

        @NotNull(message = "O campo E-mail é obrigatório!")
        String email,

        @NotNull(message = "O campo Empresa ID é obrigatório!")
        Long empresaId
) {}
