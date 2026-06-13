package br.senai.sc.communitex.dto;

import java.util.List;

public record EmpresaResponseDTO(
        Long id, String razaoSocial, String cnpj, String nomeFantasia, String email, String telefone,
        String cep, String logradouro, String numero, String complemento, String bairro, String cidade, String estado,
        RepresentanteEmpresaResponseDTO representante, List<AdocaoResponseDTO> adocoes
) {
    public EmpresaResponseDTO(
            Long id, String razaoSocial, String cnpj, String nomeFantasia, String email, String telefone,
            RepresentanteEmpresaResponseDTO representante, List<AdocaoResponseDTO> adocoes) {
        this(id, razaoSocial, cnpj, nomeFantasia, email, telefone, null, null, null, null, null, null, null, representante, adocoes);
    }
}
