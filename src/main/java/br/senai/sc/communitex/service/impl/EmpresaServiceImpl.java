package br.senai.sc.communitex.service.impl;


import br.senai.sc.communitex.dto.EmpresaRequestDTO;
import br.senai.sc.communitex.dto.EmpresaResponseDTO;
import br.senai.sc.communitex.exception.BusinessExpection;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.service.EmpresaService;
import br.senai.sc.communitex.service.UsuarioService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public EmpresaServiceImpl(EmpresaRepository empresaRepository, UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Empresa findEntityById(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));
    }

    public List<EmpresaResponseDTO> findAll() {
        return empresaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public EmpresaResponseDTO findById(Long id) {
        return empresaRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));

    }

    @Transactional
    public EmpresaResponseDTO create(EmpresaRequestDTO dto) {

        Optional<Empresa> existente = empresaRepository.findByCnpj(dto.cnpj());
        if (existente.isPresent()) {
            throw new BusinessExpection("Já existe uma empresa cadastrada com o CNPJ: " + dto.cnpj());
        }

        // Criar e persistir o Usuario do Representante
        Optional<Usuario> usuarioExistente = usuarioService.findByUsername(dto.emailRepresentante());
        if (usuarioExistente.isPresent()) {
            throw new BusinessExpection("Já existe um usuário cadastrado com o email: " + dto.emailRepresentante());
        }

        Usuario usuarioRepresentante = new Usuario();
        usuarioRepresentante.setUsername(dto.emailRepresentante());
        usuarioRepresentante.setPassword(passwordEncoder.encode(dto.senhaRepresentante()));
        usuarioRepresentante.setRole("ROLE_EMPRESA");
        usuarioRepresentante.setNome(dto.nomeRepresentante());
        Usuario usuarioSalvo = usuarioService.save(usuarioRepresentante);

        // Criar e persistir a Empresa
        Empresa empresa = new Empresa();
        BeanUtils.copyProperties(dto, empresa, "usuarioRepresentante");
        empresa.setCnpj(dto.cnpj().replaceAll("\\D", ""));
        empresa.setTelefone(dto.telefone().replaceAll("\\D", ""));
        empresa.setUsuarioRepresentante(usuarioSalvo);

        return toResponseDTO(empresaRepository.save(empresa));
    }

    public EmpresaResponseDTO update(Long id, EmpresaRequestDTO dto) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));

        BeanUtils.copyProperties(dto, empresa);
        empresa.setId(id);

        return toResponseDTO(empresaRepository.save(empresa));

    }

    public void delete(Long id) {
        if (!empresaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Empresa não encontrada com ID: " + id);
        }
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
