package br.senai.sc.communitex.dto;

public record PessoaFisicaResponseDTO(
        Long id,
        String nome,
        String cpf,
        String email,
        String telefone
) {
}

