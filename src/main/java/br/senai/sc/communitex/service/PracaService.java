package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.PracaRequestDTO;
import br.senai.sc.communitex.dto.PracaResponseDTO;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.repository.PracaRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PracaService {
    private final PracaRepository pracaRepository;

    public PracaService(PracaRepository pracaRepository) {
        this.pracaRepository = pracaRepository;
    }

    public List<PracaResponseDTO> findAll() {
        return pracaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public PracaResponseDTO findById(Long id) {
        return pracaRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Praça não encontrada com ID: " + id));
    }

    public PracaResponseDTO create(PracaRequestDTO dto) {
        Praca praca = new Praca();
        BeanUtils.copyProperties(dto, praca);
        praca.setStatus(StatusPraca.DISPONIVEL);
        return toResponseDTO(pracaRepository.save(praca));
    }

    public PracaResponseDTO update(Long id, PracaRequestDTO dto) {
        Praca praca = pracaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Praça não encontrada com ID: " + id));

        BeanUtils.copyProperties(dto, praca);
        praca.setId(id);

        return toResponseDTO(pracaRepository.save(praca));
    }

    public void delete(Long id) {
        if (!pracaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Praça não encontrada com ID: " + id);
        }
        pracaRepository.deleteById(id);
    }

    private PracaResponseDTO toResponseDTO(Praca praca) {
        return new PracaResponseDTO(
                praca.getId(),
                praca.getNome(),
                praca.getLogradouro(),
                praca.getBairro(),
                praca.getCidade(),
                praca.getLatitude(),
                praca.getLongitude(),
                praca.getDescricao(),
                praca.getFotoUrl(),
                praca.getStatus()
        );
    }
}

