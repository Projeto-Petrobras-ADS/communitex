package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.RepresentanteEmpresaRequestDTO;
import br.senai.sc.communitex.dto.RepresentanteEmpresaResponseDTO;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.RepresentanteEmpresa;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.RepresentanteEmpresaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RepresentanteEmpresaService {

    private final RepresentanteEmpresaRepository representanteRepository;
    private final EmpresaRepository empresaRepository;

    public RepresentanteEmpresaService(RepresentanteEmpresaRepository representanteRepository,
                                       EmpresaRepository empresaRepository) {
        this.representanteRepository = representanteRepository;
        this.empresaRepository = empresaRepository;
    }

    public RepresentanteEmpresaResponseDTO create(RepresentanteEmpresaRequestDTO dto) {
        Empresa empresa = empresaRepository.findById(dto.empresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + dto.empresaId()));

        RepresentanteEmpresa representante = new RepresentanteEmpresa();
        representante.setNome(dto.nome());
        representante.setAtivo(dto.ativo());
        representante.setEmpresa(empresa);

        representanteRepository.save(representante);

        return toResponseDTO(representante);
    }

    public List<RepresentanteEmpresaResponseDTO> findAll() {
        return representanteRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public RepresentanteEmpresaResponseDTO findById(Long id) {
        RepresentanteEmpresa representante = representanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Representante não encontrado com ID: " + id));

        return toResponseDTO(representante);
    }

    public RepresentanteEmpresaResponseDTO update(Long id, RepresentanteEmpresaRequestDTO dto) {
        RepresentanteEmpresa representante = representanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Representante não encontrado com ID: " + id));

        Empresa empresa = empresaRepository.findById(dto.empresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + dto.empresaId()));

        representante.setNome(dto.nome());
        representante.setAtivo(dto.ativo());
        representante.setEmail(dto.email());
        representante.setEmpresa(empresa);

        representanteRepository.save(representante);

        return toResponseDTO(representante);
    }

    public void delete(Long id) {
        if (!representanteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Representante não encontrado com ID: " + id);
        }
        representanteRepository.deleteById(id);
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
