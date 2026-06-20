package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.model.RepresentanteEmpresa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmpresaRequestDTO(
        @NotBlank String razaoSocial,
        @NotBlank String cnpj,
        String nomeFantasia,
        @NotBlank @Email(message = "Email da empresa inválido") String email,
        String telefone,
        RepresentanteEmpresa representanteEmpresa,
        @NotBlank(message = "O nome do representante é obrigatório") String nomeRepresentante,
        @NotBlank(message = "O email do representante é obrigatório") @Email(message = "Email do representante inválido") String emailRepresentante,
        @NotBlank(message = "A senha do representante é obrigatória")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "A senha deve ter 8 caracteres, incluindo maiúscula, minúscula, número e símbolo") String senhaRepresentante,
        @Pattern(regexp = "\\d{8}", message = "CEP inválido! Deve conter 8 dígitos") String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        @Pattern(regexp = "[A-Za-z]{2}", message = "Estado inválido! Use a sigla com 2 letras") String estado
) {
    public EmpresaRequestDTO(
            String razaoSocial, String cnpj, String nomeFantasia, String email, String telefone,
            RepresentanteEmpresa representanteEmpresa, String nomeRepresentante,
            String emailRepresentante, String senhaRepresentante) {
        this(razaoSocial, cnpj, nomeFantasia, email, telefone, representanteEmpresa, nomeRepresentante,
                emailRepresentante, senhaRepresentante, null, null, null, null, null, null, null);
    }
}
