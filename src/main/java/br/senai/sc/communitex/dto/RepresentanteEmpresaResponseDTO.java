package br.senai.sc.communitex.dto;

public record RepresentanteEmpresaResponseDTO(
        Long id,
        String nome,
        Boolean ativo,
        String email,
        Long empresaId,
        String empresaNomeFantasia
) {}
