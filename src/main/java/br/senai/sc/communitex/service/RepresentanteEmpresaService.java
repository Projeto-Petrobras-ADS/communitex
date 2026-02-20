package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.RepresentanteEmpresaRequestDTO;
import br.senai.sc.communitex.dto.RepresentanteEmpresaResponseDTO;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.RepresentanteEmpresa;
import br.senai.sc.communitex.repository.RepresentanteEmpresaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepresentanteEmpresaService {

    private final RepresentanteEmpresaRepository representanteRepository;
    private final EmpresaService empresaService;

    @Transactional
    public RepresentanteEmpresaResponseDTO create(RepresentanteEmpresaRequestDTO dto) {
        var empresa = empresaService.findEntityById(dto.empresaId());

        var representante = RepresentanteEmpresa.builder()
                .nome(dto.nome())
                .ativo(dto.ativo())
                .email(dto.email())
                .empresa(empresa)
                .build();

        var saved = representanteRepository.save(representante);
        log.info("Representante criado com ID: {} para empresa ID: {}", saved.getId(), empresa.getId());
        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<RepresentanteEmpresaResponseDTO> findAll() {
        return representanteRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public RepresentanteEmpresaResponseDTO findById(Long id) {
        var representante = representanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Representante não encontrado com ID: " + id));

        return toResponseDTO(representante);
    }

    @Transactional
    public RepresentanteEmpresaResponseDTO update(Long id, RepresentanteEmpresaRequestDTO dto) {
        var representante = representanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Representante não encontrado com ID: " + id));

        var empresa = empresaService.findEntityById(dto.empresaId());

        representante.setNome(dto.nome());
        representante.setAtivo(dto.ativo());
        representante.setEmail(dto.email());
        representante.setEmpresa(empresa);

        log.info("Representante ID: {} atualizado", id);
        return toResponseDTO(representanteRepository.save(representante));
    }

    @Transactional
    public void delete(Long id) {
        if (!representanteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Representante não encontrado com ID: " + id);
        }
        representanteRepository.deleteById(id);
        log.info("Representante ID: {} excluído", id);
    }

    private RepresentanteEmpresaResponseDTO toResponseDTO(RepresentanteEmpresa representante) {
        return new RepresentanteEmpresaResponseDTO(
                representante.getId(),
                representante.getNome(),
                representante.getAtivo(),
                representante.getEmail(),
                representante.getEmpresa().getId(),
                representante.getEmpresa().getNomeFantasia()
        );
    }
}
