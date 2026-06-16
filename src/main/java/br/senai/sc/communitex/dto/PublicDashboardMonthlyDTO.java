package br.senai.sc.communitex.dto;

public record PublicDashboardMonthlyDTO(
        String mes,
        long adocoesAcumuladas,
        long reparosConfirmadosAcumulados
) {
}
