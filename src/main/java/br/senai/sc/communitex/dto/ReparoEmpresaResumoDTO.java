package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.AtendimentoDenunciaStatus;

import java.time.LocalDateTime;

public record ReparoEmpresaResumoDTO(
        Long id,
        Long denunciaId,
        String denunciaTitulo,
        AtendimentoDenunciaStatus status,
        LocalDateTime dataAceite,
        LocalDateTime dataInicio,
        LocalDateTime dataConclusaoEmpresa,
        LocalDateTime dataConfirmacaoAutor
) {
}
