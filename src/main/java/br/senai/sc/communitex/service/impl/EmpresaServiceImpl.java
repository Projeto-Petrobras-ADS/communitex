package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.EmpresaRequestDTO;
import br.senai.sc.communitex.dto.EmpresaResponseDTO;
import br.senai.sc.communitex.dto.AdocaoResponseDTO;
import br.senai.sc.communitex.dto.RepresentanteEmpresaResponseDTO;
import br.senai.sc.communitex.exception.BusinessException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.model.RepresentanteEmpresa;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.service.EmpresaService;
import br.senai.sc.communitex.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Empresa buscarEntidadePorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> listarTodas() {
        return empresaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaResponseDTO buscarPorId(Long id) {
        return empresaRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));
    }

    @Override
    @Transactional
    public EmpresaResponseDTO criar(EmpresaRequestDTO dto) {
        var cnpj = sanitizarNumeros(dto.cnpj());
        empresaRepository.buscarPorCnpj(cnpj)
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
                .cnpj(cnpj)
                .nomeFantasia(dto.nomeFantasia())
                .email(dto.email())
                .telefone(sanitizarNumeros(dto.telefone()))
                .cep(sanitizarNumeros(dto.cep()))
                .logradouro(dto.logradouro())
                .numero(dto.numero())
                .complemento(dto.complemento())
                .bairro(dto.bairro())
                .cidade(dto.cidade())
                .estado(normalizarEstado(dto.estado()))
                .usuarioRepresentante(usuarioSalvo)
                .build();

        var saved = empresaRepository.save(empresa);
        log.info("Empresa criada com ID: {}", saved.getId());
        return toResponseDTO(saved);
    }

    @Override
    @Transactional
    public EmpresaResponseDTO atualizar(Long id, EmpresaRequestDTO dto) {
        var empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com ID: " + id));

        var cnpj = sanitizarNumeros(dto.cnpj());
        if (!Objects.equals(empresa.getCnpj(), cnpj)) {
            empresaRepository.buscarPorCnpj(cnpj)
                    .filter(existing -> !Objects.equals(existing.getId(), id))
                    .ifPresent(existing -> {
                        throw new BusinessException("Ja existe uma empresa cadastrada com o CNPJ: " + dto.cnpj());
                    });
        }

        var usuario = empresa.getUsuarioRepresentante();
        if (usuario == null) {
            throw new BusinessException("Empresa nao possui usuario representante associado");
        }
        if (!Objects.equals(usuario.getUsername(), dto.emailRepresentante())) {
            usuarioService.findByUsername(dto.emailRepresentante())
                    .filter(existing -> !Objects.equals(existing.getId(), usuario.getId()))
                    .ifPresent(existing -> {
                        throw new BusinessException("Ja existe um usuario cadastrado com o email: " + dto.emailRepresentante());
                    });
        }

        empresa.setRazaoSocial(dto.razaoSocial());
        empresa.setCnpj(cnpj);
        empresa.setNomeFantasia(dto.nomeFantasia());
        empresa.setEmail(dto.email());
        empresa.setTelefone(sanitizarNumeros(dto.telefone()));
        empresa.setCep(sanitizarNumeros(dto.cep()));
        empresa.setLogradouro(dto.logradouro());
        empresa.setNumero(dto.numero());
        empresa.setComplemento(dto.complemento());
        empresa.setBairro(dto.bairro());
        empresa.setCidade(dto.cidade());
        empresa.setEstado(normalizarEstado(dto.estado()));

        usuario.setNome(dto.nomeRepresentante());
        usuario.setUsername(dto.emailRepresentante());
        usuario.setPassword(passwordEncoder.encode(dto.senhaRepresentante()));
        usuarioService.save(usuario);

        log.info("Empresa ID: {} atualizada", id);
        return toResponseDTO(empresaRepository.save(empresa));
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        if (!empresaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Empresa não encontrada com ID: " + id);
        }
        empresaRepository.deleteById(id);
        log.info("Empresa ID: {} excluída", id);
    }

    private EmpresaResponseDTO toResponseDTO(Empresa empresa) {
        RepresentanteEmpresa representante = empresa.getRepresentanteEmpresas();

        var representanteDTO = representante == null ? null : new RepresentanteEmpresaResponseDTO(
            representante.getId(),
            representante.getNome(),
            representante.getAtivo(),
            representante.getEmail(),
            empresa.getId(),
            empresa.getNomeFantasia()
        );

        var adocoes = (empresa.getAdocaos() == null ? List.<Adocao>of() : empresa.getAdocaos()).stream()
            .map(this::toAdocaoDTO)
            .toList();

        return new EmpresaResponseDTO(
                empresa.getId(),
                empresa.getRazaoSocial(),
                empresa.getCnpj(),
                empresa.getNomeFantasia(),
                empresa.getEmail(),
                empresa.getTelefone(),
                empresa.getCep(),
                empresa.getLogradouro(),
                empresa.getNumero(),
                empresa.getComplemento(),
                empresa.getBairro(),
                empresa.getCidade(),
                empresa.getEstado(),
            representanteDTO,
            adocoes
        );
    }

    private AdocaoResponseDTO toAdocaoDTO(Adocao adocao) {
        var praca = adocao.getPraca();

        return new AdocaoResponseDTO(
            adocao.getId(),
            adocao.getDataInicio(),
            adocao.getDataFim(),
            adocao.getDescricaoProjeto(),
            adocao.getStatus(),
            adocao.getEmpresa() != null ? adocao.getEmpresa().getId() : null,
            adocao.getEmpresa() != null ? adocao.getEmpresa().getNomeFantasia() : null,
            praca != null ? praca.getId() : null,
            praca != null ? praca.getNome() : null,
            praca != null ? praca.getCidade() : null
        );
    }

    private String sanitizarNumeros(String valor) {
        return valor == null || valor.isBlank() ? null : valor.replaceAll("\\D", "");
    }

    private String normalizarEstado(String estado) {
        return estado == null || estado.isBlank() ? null : estado.toUpperCase();
    }
}
