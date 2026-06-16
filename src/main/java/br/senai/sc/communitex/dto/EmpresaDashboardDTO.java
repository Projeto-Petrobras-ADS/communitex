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
        double areaTotalAdotadaM2,
        double taxaAprovacao,
        long adocoesProximasDoFim,
        long totalReparos,
        long reparosAtivos,
        long reparosAceitos,
        long reparosEmAndamento,
        long reparosAguardandoConfirmacao,
        long reparosConfirmados,
        long reparosContestados,
        List<PracaResponseDTO> pracasRecomendadas,
        List<PropostaEmpresaDTO> propostasRecentes,
        List<ReparoEmpresaResumoDTO> reparosRecentes
) {
}
