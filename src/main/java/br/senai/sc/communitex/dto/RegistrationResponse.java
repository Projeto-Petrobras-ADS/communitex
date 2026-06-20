package br.senai.sc.communitex.dto;

public record RegistrationResponse(
        String accessToken,
        String refreshToken,
        TipoConta tipoConta,
        Long perfilId,
        String nome,
        String email
) {
}
