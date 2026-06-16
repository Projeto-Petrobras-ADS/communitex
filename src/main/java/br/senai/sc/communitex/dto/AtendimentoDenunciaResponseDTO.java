package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.AtendimentoDenunciaStatus;

import java.time.LocalDateTime;

public record AtendimentoDenunciaResponseDTO(
        Long id,
        Long denunciaId,
        String denunciaTitulo,
        Long empresaId,
        String empresaNome,
        AtendimentoDenunciaStatus status,
        String descricaoPlanejada,
        String descricaoReparo,
        String fotoDepoisUrl,
        String motivoContestacao,
        LocalDateTime dataAceite,
        LocalDateTime dataInicio,
        LocalDateTime dataConclusaoEmpresa,
        LocalDateTime dataConfirmacaoAutor,
        boolean podeGerenciar,
        boolean podeConfirmar
) {
}
