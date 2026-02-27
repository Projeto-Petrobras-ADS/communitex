package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.AdocaoRequestDTO;
import br.senai.sc.communitex.dto.AdocaoResponseDTO;
import br.senai.sc.communitex.dto.AdocaoStatusResponseDTO;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.exception.BusinessException;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.exception.InvalidAdocaoException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.repository.AdocaoRepository;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdocaoService {

    private final AdocaoRepository adocaoRepository;
    private final EmpresaRepository empresaRepository;
    private final PracaRepository pracaRepository;

    @Transactional(readOnly = true)
    public List<AdocaoResponseDTO> findAll() {
        return adocaoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public AdocaoResponseDTO create(AdocaoRequestDTO dto) {
        var empresa = getEmpresaFromAuthenticatedUser();

        var praca = pracaRepository.findById(dto.pracaId())
                .orElseThrow(() -> new ResourceNotFoundException("Praça não encontrada com ID: " + dto.pracaId()));

        if (praca.getStatus() == StatusPraca.ADOTADA) {
            throw new InvalidAdocaoException("Esta praça já está adotada por outra empresa!");
        }

        var adocao = Adocao.builder()
                .dataInicio(dto.dataInicio())
                .dataFim(dto.dataFim())
                .descricaoProjeto(dto.descricaoProjeto())
                .status(dto.status())
                .empresa(empresa)
                .praca(praca)
                .build();

        atualizarStatusPraca(praca, dto.status());
        pracaRepository.save(praca);

        var saved = adocaoRepository.save(adocao);
        log.info("Adoção criada com ID: {} para praça ID: {} pela empresa ID: {}",
                saved.getId(), praca.getId(), empresa.getId());
        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public AdocaoResponseDTO findById(Long id) {
        var adocao = adocaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adoção não encontrada com ID: " + id));

        return toResponseDTO(adocao);
    }

    @Transactional(readOnly = true)
    public List<AdocaoStatusResponseDTO> findByStatus(StatusAdocao status) {
        return adocaoRepository.findByStatus(status).stream()
                .map(adocao -> new AdocaoStatusResponseDTO(
                        adocao.getId(),
                        adocao.getDescricaoProjeto(),
                        adocao.getDataInicio(),
                        adocao.getDataFim(),
                        adocao.getStatus(),
                        adocao.getEmpresa() != null ? adocao.getEmpresa().getNomeFantasia() : null,
                        adocao.getPraca() != null ? adocao.getPraca().getNome() : null
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdocaoResponseDTO> findByPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new BusinessException("As datas de início e fim são obrigatórias");
        }

        if (fim.isBefore(inicio)) {
            throw new BusinessException("A data final não pode ser anterior à data inicial");
        }

        return adocaoRepository.findByDataInicioGreaterThanEqualAndDataFimLessThanEqual(inicio, fim).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdocaoResponseDTO> findByEmpresa(Long empresaId) {
        return adocaoRepository.findByEmpresaId(empresaId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdocaoResponseDTO> findByAuthenticatedUserEmpresa() {
        var empresa = getEmpresaFromAuthenticatedUser();
        return findByEmpresa(empresa.getId());
    }

    @Transactional(readOnly = true)
    public List<AdocaoResponseDTO> findAdocoesByPrazoEStatus(Integer dias, StatusAdocao status) {
        var diasEfetivo = (dias == null || dias <= 0) ? 7 : dias;
        var hoje = LocalDate.now();
        var limite = hoje.plusDays(diasEfetivo);

        return adocaoRepository.findAdocoesByPrazoEStatus(hoje, limite, status).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdocaoResponseDTO> findByPraca(Long pracaId) {
        return adocaoRepository.findByPraca_Id(pracaId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public AdocaoResponseDTO update(Long id, AdocaoRequestDTO dto) {
        var adocao = adocaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adoção não encontrada com ID: " + id));

        var praca = pracaRepository.findById(dto.pracaId())
                .orElseThrow(() -> new ResourceNotFoundException("Praça não encontrada com ID: " + dto.pracaId()));

        adocao.setDataInicio(dto.dataInicio());
        adocao.setDataFim(dto.dataFim());
        adocao.setDescricaoProjeto(dto.descricaoProjeto());
        adocao.setPraca(praca);

        log.info("Adoção ID: {} atualizada", id);
        return toResponseDTO(adocaoRepository.save(adocao));
    }

    @Transactional
    public void delete(Long id) {
        if (!adocaoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Adoção não encontrada com ID: " + id);
        }
        adocaoRepository.deleteById(id);
        log.info("Adoção ID: {} excluída", id);
    }

    @Transactional
    public AdocaoResponseDTO finalizeAdoption(Long id) {
        var adocao = adocaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adoção não encontrada com ID: " + id));

        adocao.setStatus(StatusAdocao.FINALIZADA);
        adocao.setDataFim(LocalDate.now());

        var praca = adocao.getPraca();
        praca.setStatus(StatusPraca.DISPONIVEL);
        pracaRepository.save(praca);

        log.info("Adoção ID: {} finalizada", id);
        return toResponseDTO(adocaoRepository.save(adocao));
    }


    private Empresa getEmpresaFromAuthenticatedUser() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof String str) {
            username = str;
        } else {
            throw new ForbiddenException("Usuário autenticado não encontrado no contexto");
        }

        return empresaRepository.findByUsuarioRepresentanteUsername(username)
                .orElseThrow(() -> new ForbiddenException("Nenhuma empresa associada ao usuário autenticado: " + username));
    }

    private void atualizarStatusPraca(br.senai.sc.communitex.model.Praca praca, StatusAdocao status) {
        if (status == StatusAdocao.EM_ANALISE || status == StatusAdocao.PROPOSTA) {
            praca.setStatus(StatusPraca.EM_PROCESSO);
        } else if (status == StatusAdocao.APROVADA || status == StatusAdocao.CONCLUIDA) {
            praca.setStatus(StatusPraca.ADOTADA);
        }
    }

    private AdocaoResponseDTO toResponseDTO(Adocao adocao) {
        return new AdocaoResponseDTO(
                adocao.getId(),
                adocao.getDataInicio(),
                adocao.getDataFim(),
                adocao.getDescricaoProjeto(),
                adocao.getStatus(),
                adocao.getEmpresa(),
                adocao.getPraca()
        );
    }
}
