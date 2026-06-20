package br.senai.sc.communitex.dto;

public record PasswordChangeRequest(
        String senhaAtual,
        String novaSenha,
        String confirmacaoSenha
) {
}
