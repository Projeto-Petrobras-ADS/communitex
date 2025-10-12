package br.senai.sc.communitex.dto;

import jakarta.validation.constraints.NotBlank;


public record EmpresaRequestDTO(
        @NotBlank
        String razaoSocial,
        @NotBlank
        String cnpj,
        @NotBlank
        String email,
        String telefone

) {
}
