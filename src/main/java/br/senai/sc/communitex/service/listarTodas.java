package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.EmpresaRequestDTO;
import br.senai.sc.communitex.dto.EmpresaResponseDTO;
import br.senai.sc.communitex.model.Empresa;

import java.util.List;

public interface listarTodas {

    Empresa buscarEntidadePorId(Long id);

    List<EmpresaResponseDTO> listarTodas();

    EmpresaResponseDTO buscarPorId(Long id);

    EmpresaResponseDTO criar(EmpresaRequestDTO dto);

    EmpresaResponseDTO atualizar(Long id, EmpresaRequestDTO dto);

    void excluir(Long id);
}
