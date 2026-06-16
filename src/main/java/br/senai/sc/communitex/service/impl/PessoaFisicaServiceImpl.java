package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.PessoaFisicaRequestDTO;
import br.senai.sc.communitex.dto.PessoaFisicaResponseDTO;
import br.senai.sc.communitex.exception.BusinessException;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.PessoaFisicaRepository;
import br.senai.sc.communitex.service.PessoaFisicaService;
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
public class PessoaFisicaServiceImpl implements PessoaFisicaService {

    private final PessoaFisicaRepository pessoaFisicaRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PessoaFisica findByUsuarioUsername(String username) {
        return pessoaFisicaRepository.findByUsuarioUsername(username)
                .orElseThrow(() -> new ForbiddenException(
                        "Nenhuma pessoa física associada ao usuário: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PessoaFisicaResponseDTO> findAll() {
        return pessoaFisicaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PessoaFisicaResponseDTO findById(Long id) {
        return pessoaFisicaRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa Física não encontrada com ID: " + id));
    }

    @Override
    @Transactional
    public PessoaFisicaResponseDTO create(PessoaFisicaRequestDTO dto) {
        var cpf = sanitizarNumeros(dto.cpf());
        validarDuplicidadeCpf(cpf, null);
        validarDuplicidadeEmail(dto.email(), null);

        usuarioService.findByUsername(dto.email())
                .ifPresent(existing -> {
                    throw new BusinessException("Já existe um usuário cadastrado com o email: " + dto.email());
                });

        var usuario = Usuario.builder()
                .username(dto.email())
                .password(passwordEncoder.encode(dto.senha()))
                .role("ROLE_USER")
                .nome(dto.nome())
                .build();
        var usuarioSalvo = usuarioService.save(usuario);

        var pessoaFisica = PessoaFisica.builder()
                .nome(dto.nome())
                .cpf(cpf)
                .email(dto.email())
                .telefone(sanitizarNumeros(dto.telefone()))
                .cep(sanitizarNumeros(dto.cep()))
                .logradouro(dto.logradouro())
                .numero(dto.numero())
                .complemento(dto.complemento())
                .bairro(dto.bairro())
                .cidade(dto.cidade())
                .estado(normalizarEstado(dto.estado()))
                .usuario(usuarioSalvo)
                .build();

        var saved = pessoaFisicaRepository.save(pessoaFisica);
        log.info("Pessoa Física criada com ID: {}", saved.getId());
        return toResponseDTO(saved);
    }

    @Override
    @Transactional
    public PessoaFisicaResponseDTO update(Long id, PessoaFisicaRequestDTO dto) {
        var pessoaFisica = pessoaFisicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa Física não encontrada com ID: " + id));

        var cpf = sanitizarNumeros(dto.cpf());
        if (!Objects.equals(pessoaFisica.getCpf(), cpf)) {
            validarDuplicidadeCpf(cpf, id);
        }

        if (!Objects.equals(pessoaFisica.getEmail(), dto.email())) {
            validarDuplicidadeEmail(dto.email(), id);
            usuarioService.findByUsername(dto.email())
                    .filter(existing -> pessoaFisica.getUsuario() == null
                            || !Objects.equals(existing.getId(), pessoaFisica.getUsuario().getId()))
                    .ifPresent(existing -> {
                        throw new BusinessException("Ja existe um usuario cadastrado com o email: " + dto.email());
                    });
        }

        pessoaFisica.setNome(dto.nome());
        pessoaFisica.setCpf(cpf);
        pessoaFisica.setEmail(dto.email());
        pessoaFisica.setTelefone(sanitizarNumeros(dto.telefone()));
        pessoaFisica.setCep(sanitizarNumeros(dto.cep()));
        pessoaFisica.setLogradouro(dto.logradouro());
        pessoaFisica.setNumero(dto.numero());
        pessoaFisica.setComplemento(dto.complemento());
        pessoaFisica.setBairro(dto.bairro());
        pessoaFisica.setCidade(dto.cidade());
        pessoaFisica.setEstado(normalizarEstado(dto.estado()));

        var usuario = pessoaFisica.getUsuario();
        if (usuario == null) {
            throw new BusinessException("Pessoa fisica nao possui usuario associado");
        }
        usuario.setNome(dto.nome());
        usuario.setUsername(dto.email());
        usuario.setPassword(passwordEncoder.encode(dto.senha()));
        usuarioService.save(usuario);

        log.info("Pessoa Física ID: {} atualizada", id);
        return toResponseDTO(pessoaFisicaRepository.save(pessoaFisica));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!pessoaFisicaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pessoa Física não encontrada com ID: " + id);
        }
        pessoaFisicaRepository.deleteById(id);
        log.info("Pessoa Física ID: {} excluída", id);
    }


    private void validarDuplicidadeCpf(String cpf, Long idIgnorar) {
        pessoaFisicaRepository.findByCpf(cpf)
                .filter(existing -> idIgnorar == null || !existing.getId().equals(idIgnorar))
                .ifPresent(existing -> {
                    throw new BusinessException("Já existe uma pessoa cadastrada com o CPF: " + cpf);
                });
    }

    private void validarDuplicidadeEmail(String email, Long idIgnorar) {
        pessoaFisicaRepository.findByEmail(email)
                .filter(existing -> idIgnorar == null || !existing.getId().equals(idIgnorar))
                .ifPresent(existing -> {
                    throw new BusinessException("Já existe uma pessoa cadastrada com o email: " + email);
                });
    }

    private String sanitizarNumeros(String valor) {
        return valor != null && !valor.isEmpty() ? valor.replaceAll("\\D", "") : null;
    }

    private String normalizarEstado(String estado) {
        return estado != null && !estado.isBlank() ? estado.toUpperCase() : null;
    }

    private PessoaFisicaResponseDTO toResponseDTO(PessoaFisica pessoaFisica) {
        return new PessoaFisicaResponseDTO(
                pessoaFisica.getId(),
                pessoaFisica.getNome(),
                pessoaFisica.getCpf(),
                pessoaFisica.getEmail(),
                pessoaFisica.getTelefone(),
                pessoaFisica.getCep(),
                pessoaFisica.getLogradouro(),
                pessoaFisica.getNumero(),
                pessoaFisica.getComplemento(),
                pessoaFisica.getBairro(),
                pessoaFisica.getCidade(),
                pessoaFisica.getEstado()
        );
    }
}
