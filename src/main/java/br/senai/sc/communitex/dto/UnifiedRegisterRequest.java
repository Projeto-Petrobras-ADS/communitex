package br.senai.sc.communitex.dto;

public record UnifiedRegisterRequest(
        TipoConta tipoConta,
        String nome,
        String cpf,
        String razaoSocial,
        String cnpj,
        String nomeRepresentante,
        String email,
        String senha,
        String confirmacaoSenha,
        Boolean aceitouTermos
) {
}
