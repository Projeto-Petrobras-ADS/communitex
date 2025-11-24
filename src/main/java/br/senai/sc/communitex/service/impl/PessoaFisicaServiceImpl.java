package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.PessoaFisicaRequestDTO;
import br.senai.sc.communitex.dto.PessoaFisicaResponseDTO;
import br.senai.sc.communitex.exception.BusinessExpection;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.PessoaFisicaRepository;
import br.senai.sc.communitex.service.PessoaFisicaService;
import br.senai.sc.communitex.service.UsuarioService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PessoaFisicaServiceImpl implements PessoaFisicaService {

    private final PessoaFisicaRepository pessoaFisicaRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public PessoaFisicaServiceImpl(PessoaFisicaRepository pessoaFisicaRepository,
                                   UsuarioService usuarioService,
                                   PasswordEncoder passwordEncoder) {
        this.pessoaFisicaRepository = pessoaFisicaRepository;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PessoaFisica findByUsuarioUsername(String username) {
        return pessoaFisicaRepository.findByUsuarioUsername(username)
                .orElseThrow(() -> new ForbiddenException(
                        "Nenhuma pessoa física associada ao usuário: " + username));
    }

    public List<PessoaFisicaResponseDTO> findAll() {
        return pessoaFisicaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public PessoaFisicaResponseDTO findById(Long id) {
        return pessoaFisicaRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa Física não encontrada com ID: " + id));
    }

    @Transactional
    public PessoaFisicaResponseDTO create(PessoaFisicaRequestDTO dto) {
        Optional<PessoaFisica> existenteByCpf = pessoaFisicaRepository.findByCpf(dto.cpf());
        if (existenteByCpf.isPresent()) {
            throw new BusinessExpection("Já existe uma pessoa cadastrada com o CPF: " + dto.cpf());
        }

        Optional<PessoaFisica> existenteByEmail = pessoaFisicaRepository.findByEmail(dto.email());
        if (existenteByEmail.isPresent()) {
            throw new BusinessExpection("Já existe uma pessoa cadastrada com o email: " + dto.email());
        }

        Optional<Usuario> usuarioExistente = usuarioService.findByUsername(dto.email());
        if (usuarioExistente.isPresent()) {
            throw new BusinessExpection("Já existe um usuário cadastrado com o email: " + dto.email());
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.email());
        usuario.setPassword(passwordEncoder.encode(dto.senha()));
        usuario.setRole("ROLE_USER");
        usuario.setNome(dto.nome());
        Usuario usuarioSalvo = usuarioService.save(usuario);

        PessoaFisica pessoaFisica = new PessoaFisica();
        BeanUtils.copyProperties(dto, pessoaFisica, "usuario", "senha");
        pessoaFisica.setCpf(dto.cpf().replaceAll("\\D", ""));
        if (dto.telefone() != null && !dto.telefone().isEmpty()) {
            pessoaFisica.setTelefone(dto.telefone().replaceAll("\\D", ""));
        }
        pessoaFisica.setUsuario(usuarioSalvo);

        return toResponseDTO(pessoaFisicaRepository.save(pessoaFisica));
    }

    public PessoaFisicaResponseDTO update(Long id, PessoaFisicaRequestDTO dto) {
        PessoaFisica pessoaFisica = pessoaFisicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa Física não encontrada com ID: " + id));

        if (!pessoaFisica.getCpf().equals(dto.cpf())) {
            Optional<PessoaFisica> existenteByCpf = pessoaFisicaRepository.findByCpf(dto.cpf());
            if (existenteByCpf.isPresent()) {
                throw new BusinessExpection("Já existe uma pessoa cadastrada com o CPF: " + dto.cpf());
            }
        }

        if (!pessoaFisica.getEmail().equals(dto.email())) {
            Optional<PessoaFisica> existenteByEmail = pessoaFisicaRepository.findByEmail(dto.email());
            if (existenteByEmail.isPresent()) {
                throw new BusinessExpection("Já existe uma pessoa cadastrada com o email: " + dto.email());
            }
        }

        BeanUtils.copyProperties(dto, pessoaFisica, "id", "usuario", "senha");
        pessoaFisica.setCpf(dto.cpf().replaceAll("\\D", ""));
        if (dto.telefone() != null && !dto.telefone().isEmpty()) {
            pessoaFisica.setTelefone(dto.telefone().replaceAll("\\D", ""));
        }

        return toResponseDTO(pessoaFisicaRepository.save(pessoaFisica));
    }

    public void delete(Long id) {
        if (!pessoaFisicaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pessoa Física não encontrada com ID: " + id);
        }
        pessoaFisicaRepository.deleteById(id);
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

