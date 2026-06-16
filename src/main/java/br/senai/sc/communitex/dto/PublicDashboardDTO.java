package br.senai.sc.communitex.dto;

import java.util.List;

public record PublicDashboardDTO(
        long totalPracas,
        long pracasAdotadas,
        double areaAdotadaM2,
        long reparosConfirmados,
        double tempoMedioReparoHoras,
        double taxaAdocao,
        double taxaConfirmacaoReparos,
        List<PublicDashboardMonthlyDTO> evolucaoMensal
) {
}
