package br.senai.sc.communitex.dto;

import java.util.List;

public record UsuarioDashboardDTO(
        String usuarioNome,
        long pracasCadastradas,
        long pracasDisponiveis,
        long pracasEmProcesso,
        long pracasAdotadas,
        long denunciasRealizadas,
        long denunciasAbertas,
        long denunciasEmAndamento,
        long denunciasResolvidas,
        long totalApoiosRecebidos,
        long apoiosRealizados,
        long comentariosRealizados,
        double taxaResolucao,
        long confirmacoesPendentes,
        long reparosConfirmados,
        long reparosContestados,
        List<PracaResponseDTO> pracasRecentes,
        List<DenunciaResponseDTO> denunciasRecentes
) {
}
