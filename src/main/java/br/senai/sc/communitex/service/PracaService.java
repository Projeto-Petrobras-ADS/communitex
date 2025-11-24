package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.PracaDetailResponseDTO;
import br.senai.sc.communitex.dto.PracaPesquisaDTO;
import br.senai.sc.communitex.dto.PracaRequestDTO;
import br.senai.sc.communitex.dto.PracaResponseDTO;

import java.util.List;

public interface PracaService {
    List<PracaResponseDTO> findAll(PracaPesquisaDTO pesquisaDTO);
    PracaResponseDTO findById(Long id);
    PracaDetailResponseDTO findByIdWithDetails(Long id);
    PracaResponseDTO create(PracaRequestDTO dto);
    PracaResponseDTO update(Long id, PracaRequestDTO dto);
    void delete(Long id);
}

