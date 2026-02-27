package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.PessoaFisicaRequestDTO;
import br.senai.sc.communitex.dto.PessoaFisicaResponseDTO;
import br.senai.sc.communitex.model.PessoaFisica;

import java.util.List;

public interface PessoaFisicaService {

    PessoaFisica findByUsuarioUsername(String username);

    List<PessoaFisicaResponseDTO> findAll();

    PessoaFisicaResponseDTO findById(Long id);

    PessoaFisicaResponseDTO create(PessoaFisicaRequestDTO dto);

    PessoaFisicaResponseDTO update(Long id, PessoaFisicaRequestDTO dto);

    void delete(Long id);
}
