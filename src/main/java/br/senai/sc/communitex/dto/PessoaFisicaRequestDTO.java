package br.senai.sc.communitex.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PessoaFisicaRequestDTO(
        @NotBlank(message = "O nome é obrigatório") String nome,
        @NotBlank(message = "O CPF é obrigatório") @Pattern(regexp = "\\d{11}", message = "CPF inválido! Deve conter 11 dígitos") String cpf,
        @NotBlank(message = "O email é obrigatório") @Email(message = "Email inválido") String email,
        @Pattern(regexp = "\\d{10,11}", message = "Telefone inválido") String telefone,
        @NotBlank(message = "A senha é obrigatória")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "A senha deve ter 8 caracteres, incluindo maiúscula, minúscula, número e símbolo") String senha,
        @Pattern(regexp = "\\d{8}", message = "CEP inválido! Deve conter 8 dígitos") String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        @Pattern(regexp = "[A-Za-z]{2}", message = "Estado inválido! Use a sigla com 2 letras") String estado
) {
    public PessoaFisicaRequestDTO(String nome, String cpf, String email, String telefone, String senha) {
        this(nome, cpf, email, telefone, senha, null, null, null, null, null, null, null);
    }
}
