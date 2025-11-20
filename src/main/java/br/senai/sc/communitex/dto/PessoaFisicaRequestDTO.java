package br.senai.sc.communitex.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PessoaFisicaRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "CPF inválido! Deve conter 11 dígitos")
        String cpf,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @Pattern(regexp = "\\d{10,11}", message = "Telefone inválido! Use o formato (99) 99999-9999")
        String telefone,

        @NotBlank(message = "A senha é obrigatória")
        String senha
) {
}


