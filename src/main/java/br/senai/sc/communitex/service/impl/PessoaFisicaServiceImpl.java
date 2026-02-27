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
        validarDuplicidadeCpf(dto.cpf(), null);
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
                .cpf(sanitizarNumeros(dto.cpf()))
                .email(dto.email())
                .telefone(sanitizarNumeros(dto.telefone()))
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

        if (!pessoaFisica.getCpf().equals(dto.cpf())) {
            validarDuplicidadeCpf(dto.cpf(), id);
        }

        if (!pessoaFisica.getEmail().equals(dto.email())) {
            validarDuplicidadeEmail(dto.email(), id);
        }

        pessoaFisica.setNome(dto.nome());
        pessoaFisica.setCpf(sanitizarNumeros(dto.cpf()));
        pessoaFisica.setEmail(dto.email());
        pessoaFisica.setTelefone(sanitizarNumeros(dto.telefone()));

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

    private PessoaFisicaResponseDTO toResponseDTO(PessoaFisica pessoaFisica) {
        return new PessoaFisicaResponseDTO(
                pessoaFisica.getId(),
                pessoaFisica.getNome(),
                pessoaFisica.getCpf(),
                pessoaFisica.getEmail(),
                pessoaFisica.getTelefone()
        );
    }
}
