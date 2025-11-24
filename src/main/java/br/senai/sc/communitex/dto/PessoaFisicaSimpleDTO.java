package br.senai.sc.communitex.dto;

public record PessoaFisicaSimpleDTO(
        Long id,
        String nome,
        String email,
        String telefone
) {
}

