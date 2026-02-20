package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.AdocaoHistoricoDTO;
import br.senai.sc.communitex.dto.PessoaFisicaSimpleDTO;
import br.senai.sc.communitex.dto.PracaDetailResponseDTO;
import br.senai.sc.communitex.dto.PracaPesquisaDTO;
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
import br.senai.sc.communitex.specification.PracaSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PracaServiceImpl implements PracaService {

    private final PracaRepository pracaRepository;
    private final PessoaFisicaService pessoaFisicaService;

    @Override
    @Transactional(readOnly = true)
    public List<PracaResponseDTO> findAll(PracaPesquisaDTO pesquisaDTO) {
        return pracaRepository.findAll(PracaSpecification.comFiltros(pesquisaDTO)).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PracaResponseDTO findById(Long id) {
        return pracaRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Praça não encontrada com ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PracaDetailResponseDTO findByIdWithDetails(Long id) {
        var praca = pracaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Praça não encontrada com ID: " + id));

        return toDetailResponseDTO(praca);
    }

    @Override
    @Transactional
    public PracaResponseDTO create(PracaRequestDTO dto) {
        var pessoaFisica = getPessoaFisicaFromAuthenticatedUser();

        var praca = Praca.builder()
                .nome(dto.nome())
                .logradouro(dto.logradouro())
                .bairro(dto.bairro())
                .cidade(dto.cidade())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .descricao(dto.descricao())
                .fotoUrl(dto.fotoUrl())
                .metragemM2(dto.metragemM2())
                .status(StatusPraca.DISPONIVEL)
                .cadastradoPor(pessoaFisica)
                .build();

        var saved = pracaRepository.save(praca);
        log.info("Praça criada com ID: {} pelo usuário: {}", saved.getId(), pessoaFisica.getNome());
        return toResponseDTO(saved);
    }

    @Override
    @Transactional
    public PracaResponseDTO update(Long id, PracaRequestDTO dto) {
        var praca = pracaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Praça não encontrada com ID: " + id));

        BeanUtils.copyProperties(dto, praca, "id", "cadastradoPor", "adocoes");

        log.info("Praça ID: {} atualizada", id);
        return toResponseDTO(pracaRepository.save(praca));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!pracaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Praça não encontrada com ID: " + id);
        }
        pracaRepository.deleteById(id);
        log.info("Praça ID: {} excluída", id);
    }


    private PessoaFisica getPessoaFisicaFromAuthenticatedUser() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof String str) {
            username = str;
        } else {
            throw new ForbiddenException("Usuário autenticado não encontrado no contexto");
        }

        return pessoaFisicaService.findByUsuarioUsername(username);
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
        var cadastradoPor = praca.getCadastradoPor();

        if (cadastradoPor != null) {
            cadastradoPorDTO = new PessoaFisicaSimpleDTO(
                    cadastradoPor.getId(),
                    cadastradoPor.getNome(),
                    cadastradoPor.getEmail(),
                    cadastradoPor.getTelefone()
            );
        }

        var historico = praca.getAdocoes().stream()
                .map(adocao -> new AdocaoHistoricoDTO(
                        adocao.getEmpresa().getId(),
                        adocao.getEmpresa().getRazaoSocial(),
                        adocao.getDescricaoProjeto()
                ))
                .toList();

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
