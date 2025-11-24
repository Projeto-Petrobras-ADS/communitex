package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.InteresseAdocaoRequestDTO;
import br.senai.sc.communitex.dto.InteresseAdocaoResponseDTO;
import br.senai.sc.communitex.dto.PropostaEmpresaDTO;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.repository.AdocaoRepository;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import br.senai.sc.communitex.service.IAdocaoService;
import br.senai.sc.communitex.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdocaoServiceImpl implements IAdocaoService {

    private static final Logger logger = LoggerFactory.getLogger(AdocaoServiceImpl.class);

    private final AdocaoRepository adocaoRepository;
    private final PracaRepository pracaRepository;
    private final EmpresaRepository empresaRepository;
    private final EmailService emailService;

    public AdocaoServiceImpl(AdocaoRepository adocaoRepository,
                             PracaRepository pracaRepository,
                             EmpresaRepository empresaRepository,
                             EmailService emailService) {
        this.adocaoRepository = adocaoRepository;
        this.pracaRepository = pracaRepository;
        this.empresaRepository = empresaRepository;
        this.emailService = emailService;
    }

    private Empresa getEmpresaFromAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            throw new ForbiddenException("Usuário autenticado não encontrado no contexto");
        }

        return empresaRepository.findByUsuarioRepresentanteUsername(username)
                .orElseThrow(() -> new ForbiddenException("Nenhuma empresa associada ao usuário autenticado: " + username));
    }

    @Override
    @Transactional
    public InteresseAdocaoResponseDTO registrarInteresse(InteresseAdocaoRequestDTO requestDTO) {
        logger.info("Registrando interesse de adoção - Praça ID: {}", requestDTO.pracaId());

        Empresa empresa = getEmpresaFromAuthenticatedUser();
        logger.info("Empresa obtida do token - ID: {}, Nome: {}", empresa.getId(), empresa.getRazaoSocial());

        Praca praca = pracaRepository.findById(requestDTO.pracaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Praça não encontrada com ID: " + requestDTO.pracaId()));


        PessoaFisica responsavel = praca.getCadastradoPor();
        if (responsavel == null) {
            logger.warn("Praça {} não possui pessoa física cadastrante", praca.getId());
            throw new ResourceNotFoundException(
                    "Não foi possível identificar o responsável pela praça");
        }

        Adocao adocao = new Adocao();
        adocao.setPraca(praca);
        adocao.setEmpresa(empresa);
        adocao.setDescricaoProjeto(requestDTO.proposta());
        adocao.setStatus(StatusAdocao.PROPOSTA);
        adocao.setDataInicio(LocalDate.now());

        Adocao adocaoSalva = adocaoRepository.save(adocao);

        emailService.enviarNotificacaoInteresse(responsavel, empresa, praca, requestDTO.proposta());

        logger.info("Interesse de adoção registrado com sucesso - ID: {}", adocaoSalva.getId());

        return new InteresseAdocaoResponseDTO(
                adocaoSalva.getId(),
                praca.getId(),
                praca.getNome(),
                empresa.getId(),
                empresa.getRazaoSocial(),
                adocaoSalva.getDescricaoProjeto(),
                adocaoSalva.getStatus(),
                adocaoSalva.getDataInicio()
        );
    }

    @Override
    public List<PropostaEmpresaDTO> listarPropostasMinhasEmpresa() {
        logger.info("Listando propostas da empresa autenticada");

        Empresa empresa = getEmpresaFromAuthenticatedUser();
        logger.info("Empresa obtida do token - ID: {}, Nome: {}", empresa.getId(), empresa.getRazaoSocial());

        List<Adocao> adocoes = adocaoRepository.findByEmpresaId(empresa.getId());
        logger.info("Total de propostas encontradas: {}", adocoes.size());

        return adocoes.stream()
                .map(adocao -> new PropostaEmpresaDTO(
                        adocao.getId(),
                        adocao.getPraca().getId(),
                        adocao.getPraca().getNome(),
                        adocao.getPraca().getCidade(),
                        adocao.getDescricaoProjeto(),
                        adocao.getStatus(),
                        adocao.getDataInicio(),
                        adocao.getDataInicio(),
                        adocao.getDataFim()
                ))
                .collect(Collectors.toList());
    }
}

