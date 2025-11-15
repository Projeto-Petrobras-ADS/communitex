package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.model.RepresentanteEmpresa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record EmpresaRequestDTO(
        @NotBlank
        String razaoSocial,
        @NotBlank
        String cnpj,
        String nomeFantasia,
        @NotBlank
        String email,
        String telefone,
        RepresentanteEmpresa representanteEmpresa,
        @NotBlank(message = "O nome do representante é obrigatório")
        String nomeRepresentante,
        @NotBlank(message = "O email do representante é obrigatório")
        @Email(message = "Email do representante inválido")
        String emailRepresentante,
        @NotBlank(message = "A senha do representante é obrigatória")
        String senhaRepresentante
) {
}
