package br.senai.sc.communitex.dto;

import java.util.List;

public record EmpresaDashboardDTO(
        String empresaNome,
        long pracasDisponiveis,
        long totalPropostas,
        long propostasEmAnalise,
        long propostasAprovadas,
        long propostasRejeitadas,
        long pracasAdotadas,
        long denunciasRealizadas,
        long denunciasResolvidas,
        long totalApoiosRecebidos,
        double areaTotalAdotadaM2,
        double taxaAprovacao,
        long adocoesProximasDoFim,
        long reparosEmAndamento,
        long reparosAguardandoConfirmacao,
        long reparosConfirmados,
        long reparosContestados,
        List<PracaResponseDTO> pracasRecomendadas,
        List<PropostaEmpresaDTO> propostasRecentes,
        List<DenunciaResponseDTO> denunciasRecentes
) {
}
