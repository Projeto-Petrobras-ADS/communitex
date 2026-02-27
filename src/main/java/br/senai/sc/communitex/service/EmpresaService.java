package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.EmpresaRequestDTO;
import br.senai.sc.communitex.dto.EmpresaResponseDTO;
import br.senai.sc.communitex.model.Empresa;

import java.util.List;

public interface EmpresaService {

    Empresa findEntityById(Long id);

    List<EmpresaResponseDTO> findAll();

    EmpresaResponseDTO findById(Long id);

    EmpresaResponseDTO create(EmpresaRequestDTO dto);

    EmpresaResponseDTO update(Long id, EmpresaRequestDTO dto);

    void delete(Long id);
}
