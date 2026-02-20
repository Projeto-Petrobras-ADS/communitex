package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.EmpresaRequestDTO;
import br.senai.sc.communitex.dto.EmpresaResponseDTO;
import br.senai.sc.communitex.exception.BusinessException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.service.EmpresaService;
import br.senai.sc.communitex.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Empresa findEntityById(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> findAll() {
        return empresaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaResponseDTO findById(Long id) {
        return empresaRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));
    }

    @Override
    @Transactional
    public EmpresaResponseDTO create(EmpresaRequestDTO dto) {
        empresaRepository.findByCnpj(dto.cnpj())
                .ifPresent(existing -> {
                    throw new BusinessException("Já existe uma empresa cadastrada com o CNPJ: " + dto.cnpj());
                });

        usuarioService.findByUsername(dto.emailRepresentante())
                .ifPresent(existing -> {
                    throw new BusinessException("Já existe um usuário cadastrado com o email: " + dto.emailRepresentante());
                });

        var usuarioRepresentante = Usuario.builder()
                .username(dto.emailRepresentante())
                .password(passwordEncoder.encode(dto.senhaRepresentante()))
                .role("ROLE_EMPRESA")
                .nome(dto.nomeRepresentante())
                .build();
        var usuarioSalvo = usuarioService.save(usuarioRepresentante);

        var empresa = Empresa.builder()
                .razaoSocial(dto.razaoSocial())
                .cnpj(dto.cnpj().replaceAll("\\D", ""))
                .nomeFantasia(dto.nomeFantasia())
                .email(dto.email())
                .telefone(dto.telefone() != null ? dto.telefone().replaceAll("\\D", "") : null)
                .usuarioRepresentante(usuarioSalvo)
                .build();

        var saved = empresaRepository.save(empresa);
        log.info("Empresa criada com ID: {}", saved.getId());
        return toResponseDTO(saved);
    }

    @Override
    @Transactional
    public EmpresaResponseDTO update(Long id, EmpresaRequestDTO dto) {
        var empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));

        BeanUtils.copyProperties(dto, empresa, "id", "usuarioRepresentante", "representantes", "adocaos");

        log.info("Empresa ID: {} atualizada", id);
        return toResponseDTO(empresaRepository.save(empresa));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!empresaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Empresa não encontrada com ID: " + id);
        }
        empresaRepository.deleteById(id);
        log.info("Empresa ID: {} excluída", id);
    }

    private EmpresaResponseDTO toResponseDTO(Empresa empresa) {
        return new EmpresaResponseDTO(
                empresa.getId(),
                empresa.getRazaoSocial(),
                empresa.getCnpj(),
                empresa.getNomeFantasia(),
                empresa.getEmail(),
                empresa.getTelefone(),
                empresa.getRepresentanteEmpresas(),
                empresa.getAdocaos()
        );
    }
}
