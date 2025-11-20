package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.AdocaoHistoricoDTO;
import br.senai.sc.communitex.dto.PessoaFisicaSimpleDTO;
import br.senai.sc.communitex.dto.PracaDetailResponseDTO;
import br.senai.sc.communitex.dto.PracaRequestDTO;
import br.senai.sc.communitex.dto.PracaResponseDTO;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.repository.PracaRepository;
import br.senai.sc.communitex.service.PessoaFisicaService;
import br.senai.sc.communitex.service.PracaService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PracaServiceImpl implements PracaService {
    private final PracaRepository pracaRepository;
    private final PessoaFisicaService pessoaFisicaService;

    public PracaServiceImpl(PracaRepository pracaRepository, PessoaFisicaService pessoaFisicaService) {
        this.pracaRepository = pracaRepository;
        this.pessoaFisicaService = pessoaFisicaService;
    }

    private PessoaFisica getPessoaFisicaFromAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            throw new ForbiddenException("Usuário autenticado não encontrado no contexto");
        }

        return pessoaFisicaService.findByUsuarioUsername(username);
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

    @Override
    public PracaDetailResponseDTO findByIdWithDetails(Long id) {
        Praca praca = pracaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Praça não encontrada com ID: " + id));

        return toDetailResponseDTO(praca);
    }

    public PracaResponseDTO create(PracaRequestDTO dto) {
        PessoaFisica pessoaFisica = getPessoaFisicaFromAuthenticatedUser();

        Praca praca = new Praca();
        BeanUtils.copyProperties(dto, praca);
        praca.setStatus(StatusPraca.DISPONIVEL);
        praca.setCadastradoPor(pessoaFisica);

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
                praca.getMetragemM2(),
                praca.getStatus()
        );
    }

    private PracaDetailResponseDTO toDetailResponseDTO(Praca praca) {
        PessoaFisicaSimpleDTO cadastradoPorDTO = null;
        if (praca.getCadastradoPor() != null) {
            PessoaFisica cadastradoPor = praca.getCadastradoPor();
            cadastradoPorDTO = new PessoaFisicaSimpleDTO(
                    cadastradoPor.getId(),
                    cadastradoPor.getNome(),
                    cadastradoPor.getEmail(),
                    cadastradoPor.getTelefone()
            );
        }

        List<AdocaoHistoricoDTO> historico = praca.getAdocoes().stream()
                .map(adocao -> new AdocaoHistoricoDTO(
                        adocao.getEmpresa().getId(),
                        adocao.getEmpresa().getRazaoSocial(),
                        adocao.getDescricaoProjeto()
                ))
                .collect(Collectors.toList());

        return new PracaDetailResponseDTO(
                praca.getId(),
                praca.getNome(),
                praca.getLogradouro(),
                praca.getBairro(),
                praca.getCidade(),
                praca.getLatitude(),
                praca.getLongitude(),
                praca.getDescricao(),
                praca.getFotoUrl(),
                praca.getMetragemM2(),
                praca.getStatus(),
                cadastradoPorDTO,
                historico
        );
    }
}

