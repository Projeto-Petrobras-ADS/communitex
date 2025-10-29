package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.model.RepresentanteEmpresa;

import java.util.List;

public record EmpresaResponseDTO(
        Long id,
        String nomeSocial,
        String cnpj,
        String nomeFantasia,
        String email,
        String telefone,
        RepresentanteEmpresa representanteEmpresa,
        List<Adocao> adocaos
) {
}
