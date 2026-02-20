package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.InteresseAdocaoRequestDTO;
import br.senai.sc.communitex.dto.InteresseAdocaoResponseDTO;
import br.senai.sc.communitex.dto.PropostaEmpresaDTO;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.repository.AdocaoRepository;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import br.senai.sc.communitex.service.EmailService;
import br.senai.sc.communitex.service.IAdocaoService;
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
public class AdocaoServiceImpl implements IAdocaoService {

    private final AdocaoRepository adocaoRepository;
    private final PracaRepository pracaRepository;
    private final EmpresaRepository empresaRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public InteresseAdocaoResponseDTO registrarInteresse(InteresseAdocaoRequestDTO requestDTO) {
        log.info("Registrando interesse de adoção - Praça ID: {}", requestDTO.pracaId());

        var empresa = getEmpresaFromAuthenticatedUser();
        log.info("Empresa obtida do token - ID: {}, Nome: {}", empresa.getId(), empresa.getRazaoSocial());

        var praca = pracaRepository.findById(requestDTO.pracaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Praça não encontrada com ID: " + requestDTO.pracaId()));

        var responsavel = praca.getCadastradoPor();
        if (responsavel == null) {
            log.warn("Praça {} não possui pessoa física cadastrante", praca.getId());
            throw new ResourceNotFoundException(
                    "Não foi possível identificar o responsável pela praça");
        }

        var adocao = Adocao.builder()
                .praca(praca)
                .empresa(empresa)
                .descricaoProjeto(requestDTO.proposta())
                .status(StatusAdocao.PROPOSTA)
                .dataInicio(LocalDate.now())
                .build();

        var adocaoSalva = adocaoRepository.save(adocao);

        emailService.enviarNotificacaoInteresse(responsavel, empresa, praca, requestDTO.proposta());

        log.info("Interesse de adoção registrado com sucesso - ID: {}", adocaoSalva.getId());

        return toInteresseResponseDTO(adocaoSalva, praca, empresa);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropostaEmpresaDTO> listarPropostasMinhasEmpresa() {
        log.info("Listando propostas da empresa autenticada");

        var empresa = getEmpresaFromAuthenticatedUser();
        log.info("Empresa obtida do token - ID: {}, Nome: {}", empresa.getId(), empresa.getRazaoSocial());

        var adocoes = adocaoRepository.findByEmpresaId(empresa.getId());
        log.info("Total de propostas encontradas: {}", adocoes.size());

        return adocoes.stream()
                .map(this::toPropostaDTO)
                .toList();
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

    private InteresseAdocaoResponseDTO toInteresseResponseDTO(Adocao adocao, Praca praca, Empresa empresa) {
        return new InteresseAdocaoResponseDTO(
                adocao.getId(),
                praca.getId(),
                praca.getNome(),
                empresa.getId(),
                empresa.getRazaoSocial(),
                adocao.getDescricaoProjeto(),
                adocao.getStatus(),
                adocao.getDataInicio()
        );
    }

    private PropostaEmpresaDTO toPropostaDTO(Adocao adocao) {
        var praca = adocao.getPraca();
        return new PropostaEmpresaDTO(
                adocao.getId(),
                praca.getId(),
                praca.getNome(),
                praca.getCidade(),
                adocao.getDescricaoProjeto(),
                adocao.getStatus(),
                adocao.getDataInicio(),
                adocao.getDataInicio(),
                adocao.getDataFim()
        );
    }
}
