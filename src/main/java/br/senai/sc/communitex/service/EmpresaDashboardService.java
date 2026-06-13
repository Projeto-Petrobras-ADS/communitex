package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.DenunciaResponseDTO;
import br.senai.sc.communitex.dto.EmpresaDashboardDTO;
import br.senai.sc.communitex.dto.PracaResponseDTO;
import br.senai.sc.communitex.dto.PropostaEmpresaDTO;
import br.senai.sc.communitex.enums.InteractionType;
import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.model.Denuncia;
import br.senai.sc.communitex.model.DenunciaInteracao;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.repository.AdocaoRepository;
import br.senai.sc.communitex.repository.DenunciaRepository;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmpresaDashboardService {

    private static final Set<StatusAdocao> STATUS_EM_ANALISE = Set.of(StatusAdocao.PROPOSTA, StatusAdocao.EM_ANALISE);
    private static final Set<StatusAdocao> STATUS_ADOTADA = Set.of(StatusAdocao.APROVADA, StatusAdocao.CONCLUIDA);

    private final EmpresaRepository empresaRepository;
    private final PracaRepository pracaRepository;
    private final AdocaoRepository adocaoRepository;
    private final DenunciaRepository denunciaRepository;

    @Transactional(readOnly = true)
    public EmpresaDashboardDTO obterDashboard() {
        var empresa = getEmpresaFromAuthenticatedUser();
        var usuario = empresa.getUsuarioRepresentante();
        if (usuario == null) {
            throw new ForbiddenException("A empresa autenticada nao possui usuario representante associado");
        }
        var propostas = adocaoRepository.findByEmpresaId(empresa.getId());
        var denuncias = denunciaRepository.findByAutorId(usuario.getId());

        var propostasEmAnalise = countPropostas(propostas, STATUS_EM_ANALISE);
        var propostasAprovadas = countPropostas(propostas, STATUS_ADOTADA);
        var propostasRejeitadas = countPropostas(propostas, Set.of(StatusAdocao.REJEITADA));
        var areaTotal = propostas.stream()
                .filter(proposta -> STATUS_ADOTADA.contains(proposta.getStatus()))
                .map(Adocao::getPraca)
                .filter(praca -> praca != null && praca.getMetragemM2() != null)
                .mapToDouble(praca -> praca.getMetragemM2())
                .sum();
        var proximasDoFim = propostas.stream()
                .filter(proposta -> STATUS_ADOTADA.contains(proposta.getStatus()))
                .filter(proposta -> proposta.getDataFim() != null)
                .filter(proposta -> !proposta.getDataFim().isBefore(LocalDate.now()))
                .filter(proposta -> !proposta.getDataFim().isAfter(LocalDate.now().plusDays(30)))
                .count();
        var totalApoios = denuncias.stream()
                .flatMap(denuncia -> interactions(denuncia).stream())
                .filter(interacao -> interacao.getTipo() == InteractionType.APOIO)
                .count();
        var taxaAprovacao = propostas.isEmpty()
                ? 0
                : Math.round((propostasAprovadas * 1000.0) / propostas.size()) / 10.0;

        return new EmpresaDashboardDTO(
                displayName(empresa),
                pracaRepository.countByStatus(StatusPraca.DISPONIVEL),
                propostas.size(),
                propostasEmAnalise,
                propostasAprovadas,
                propostasRejeitadas,
                propostasAprovadas,
                denunciaRepository.countByAutorId(usuario.getId()),
                denunciaRepository.countByAutorIdAndStatus(usuario.getId(), IssueStatus.RESOLVIDA),
                totalApoios,
                areaTotal,
                taxaAprovacao,
                proximasDoFim,
                pracaRepository.findTop4ByStatusOrderByIdDesc(StatusPraca.DISPONIVEL).stream().map(this::toPracaDTO).toList(),
                propostas.stream()
                        .sorted(Comparator.comparing(Adocao::getDataInicio, Comparator.nullsLast(Comparator.reverseOrder())))
                        .limit(5)
                        .map(this::toPropostaDTO)
                        .toList(),
                denunciaRepository.findTop5ByAutorIdOrderByDataCriacaoDesc(usuario.getId()).stream().map(this::toDenunciaDTO).toList()
        );
    }

    private long countPropostas(List<Adocao> propostas, Set<StatusAdocao> statuses) {
        return propostas.stream().filter(proposta -> statuses.contains(proposta.getStatus())).count();
    }

    private Empresa getEmpresaFromAuthenticatedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = authentication != null ? authentication.getPrincipal() : null;
        String username;

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof String value) {
            username = value;
        } else {
            throw new ForbiddenException("Usuario autenticado nao encontrado no contexto");
        }

        return empresaRepository.buscarPorUsuarioRepresentanteUsername(username)
                .orElseThrow(() -> new ForbiddenException("Nenhuma empresa associada ao usuario autenticado: " + username));
    }

    private String displayName(Empresa empresa) {
        return empresa.getNomeFantasia() != null && !empresa.getNomeFantasia().isBlank()
                ? empresa.getNomeFantasia()
                : empresa.getRazaoSocial();
    }

    private PracaResponseDTO toPracaDTO(br.senai.sc.communitex.model.Praca praca) {
        return new PracaResponseDTO(
                praca.getId(), praca.getNome(), praca.getLogradouro(), praca.getBairro(), praca.getCidade(),
                praca.getLatitude(), praca.getLongitude(), praca.getDescricao(), praca.getFotoUrl(),
                praca.getMetragemM2(), praca.getStatus()
        );
    }

    private PropostaEmpresaDTO toPropostaDTO(Adocao adocao) {
        var praca = adocao.getPraca();
        return new PropostaEmpresaDTO(
                adocao.getId(), praca.getId(), praca.getNome(), praca.getCidade(), adocao.getDescricaoProjeto(),
                adocao.getStatus(), adocao.getDataInicio(), adocao.getDataInicio(), adocao.getDataFim()
        );
    }

    private DenunciaResponseDTO toDenunciaDTO(Denuncia denuncia) {
        var interacoes = interactions(denuncia);
        var apoios = interacoes.stream().filter(interacao -> interacao.getTipo() == InteractionType.APOIO).count();
        var autor = denuncia.getAutor();
        return new DenunciaResponseDTO(
                denuncia.getId(), denuncia.getTitulo(), denuncia.getDescricao(), denuncia.getLatitude(), denuncia.getLongitude(),
                denuncia.getFotoUrl(), denuncia.getStatus(), denuncia.getTipo(), denuncia.getDataCriacao(), autor.getId(),
                autor.getNome() != null ? autor.getNome() : autor.getUsername(), interacoes.size(), (int) apoios
        );
    }

    private List<DenunciaInteracao> interactions(Denuncia denuncia) {
        return denuncia.getInteracoes() != null ? denuncia.getInteracoes() : List.of();
    }
}
