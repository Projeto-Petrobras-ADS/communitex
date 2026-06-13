package br.senai.sc.communitex.dto;

import java.util.List;

public record EmpresaResponseDTO(
        Long id,
        String razaoSocial,
        String cnpj,
        String nomeFantasia,
        String email,
        String telefone,
        RepresentanteEmpresaResponseDTO representante,
        List<AdocaoResponseDTO> adocoes
) {
}
