package br.senai.sc.communitex.service;


import br.senai.sc.communitex.dto.EmpresaRequestDTO;
import br.senai.sc.communitex.dto.EmpresaResponseDTO;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.repository.EmpresaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository){
        this.empresaRepository = empresaRepository;
    }

   public List<EmpresaResponseDTO> findAll(){
        return empresaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
   }

   public EmpresaResponseDTO findById(Long id){
        return empresaRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));

   }

   public EmpresaResponseDTO create(EmpresaRequestDTO dto){
        Empresa empresa = new Empresa();
       BeanUtils.copyProperties(dto,empresa);
       return toResponseDTO(empresaRepository.save(empresa));
   }

   public EmpresaResponseDTO update(Long id,  EmpresaRequestDTO dto){
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));

        BeanUtils.copyProperties(dto, empresa);
        empresa.setId(id);

        return toResponseDTO(empresaRepository.save(empresa));

   }

   public void delete(Long id){
        if(!empresaRepository.existsById(id)){
            throw new ResourceNotFoundException("Empresa não encontrada com ID: " + id);
        }
   }

   private EmpresaResponseDTO toResponseDTO(Empresa empresa){
        return new EmpresaResponseDTO(
                empresa.getId(),
                empresa.getRazaoSocial(),
                empresa.getCnpj(),
                empresa.getNomeFantasia(),
                empresa.getEmail(),
                empresa.getTelefone(),
                empresa.getAdocaos()
        );
   }
}
