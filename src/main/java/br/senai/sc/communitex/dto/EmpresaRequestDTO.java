package br.senai.sc.communitex.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;


public record EmpresaRequestDTO(
        @NotBlank
        String razaoSocial,
        @NotBlank
        String cnpj,
        String nomeFantasia,
        @NotBlank
        String email,
        String telefone
) {
}
