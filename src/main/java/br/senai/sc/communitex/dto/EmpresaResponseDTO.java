package br.senai.sc.communitex.dto;

public record EmpresaResponseDTO(
        Long id,
        String nomeSocial,
        String cnpj,
        String nomeFantasia,
        String email,
        String telefone
) {
}
