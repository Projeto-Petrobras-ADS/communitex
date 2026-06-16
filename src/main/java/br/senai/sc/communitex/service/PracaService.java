package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.PracaDetailResponseDTO;
import br.senai.sc.communitex.dto.PracaPesquisaDTO;
import br.senai.sc.communitex.dto.PracaRequestDTO;
import br.senai.sc.communitex.dto.PracaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PracaService {
    List<PracaResponseDTO> findAll(PracaPesquisaDTO pesquisaDTO);
    Page<PracaResponseDTO> findAll(PracaPesquisaDTO pesquisaDTO, Pageable pageable);
    PracaResponseDTO findById(Long id);
    PracaDetailResponseDTO findByIdWithDetails(Long id);
    PracaResponseDTO create(PracaRequestDTO dto, MultipartFile arquivo);
    PracaResponseDTO update(Long id, PracaRequestDTO dto);
    void delete(Long id);
}

