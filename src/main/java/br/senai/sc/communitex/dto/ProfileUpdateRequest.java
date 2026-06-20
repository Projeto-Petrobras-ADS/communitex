package br.senai.sc.communitex.dto;

public record ProfileUpdateRequest(
        String telefone,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        String nomeFantasia,
        String emailInstitucional
) {
}
