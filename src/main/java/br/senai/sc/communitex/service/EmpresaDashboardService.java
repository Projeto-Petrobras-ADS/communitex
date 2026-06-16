package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.EmpresaDashboardDTO;
import br.senai.sc.communitex.dto.PracaResponseDTO;
import br.senai.sc.communitex.dto.PropostaEmpresaDTO;
import br.senai.sc.communitex.dto.ReparoEmpresaResumoDTO;
import br.senai.sc.communitex.enums.AtendimentoDenunciaStatus;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.model.AtendimentoDenuncia;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.repository.AdocaoRepository;
import br.senai.sc.communitex.repository.AtendimentoDenunciaRepository;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import br.senai.sc.communitex.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import br.senai.sc.communitex.util.ArquivoUrls;
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
    private static final Set<AtendimentoDenunciaStatus> STATUS_REPAROS_ATIVOS = Set.of(
            AtendimentoDenunciaStatus.ACEITO,
            AtendimentoDenunciaStatus.EM_ANDAMENTO,
            AtendimentoDenunciaStatus.CONTESTADO
    );

    private final EmpresaRepository empresaRepository;
    private final PracaRepository pracaRepository;
    private final AdocaoRepository adocaoRepository;
    private final AtendimentoDenunciaRepository atendimentoRepository;

    @Transactional(readOnly = true)
    public EmpresaDashboardDTO obterDashboard() {
        var empresa = getEmpresaFromAuthenticatedUser();
        var propostas = adocaoRepository.findByEmpresaId(empresa.getId());
        var reparos = atendimentoRepository.findByEmpresaIdOrderByDataAceiteDesc(empresa.getId());

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
                areaTotal,
                taxaAprovacao,
                proximasDoFim,
                reparos.size(),
                countReparos(reparos, STATUS_REPAROS_ATIVOS),
                countReparos(reparos, Set.of(AtendimentoDenunciaStatus.ACEITO)),
                countReparos(reparos, Set.of(AtendimentoDenunciaStatus.EM_ANDAMENTO)),
                countReparos(reparos, Set.of(AtendimentoDenunciaStatus.CONCLUIDO_PELA_EMPRESA)),
                countReparos(reparos, Set.of(AtendimentoDenunciaStatus.CONFIRMADO_PELO_AUTOR)),
                countReparos(reparos, Set.of(AtendimentoDenunciaStatus.CONTESTADO)),
                pracaRepository.findTop4ByStatusOrderByIdDesc(StatusPraca.DISPONIVEL).stream().map(this::toPracaDTO).toList(),
                propostas.stream()
                        .sorted(Comparator.comparing(Adocao::getDataInicio, Comparator.nullsLast(Comparator.reverseOrder())))
                        .limit(5)
                        .map(this::toPropostaDTO)
                        .toList(),
                reparos.stream().limit(5).map(this::toReparoResumoDTO).toList()
        );
    }

    private long countPropostas(List<Adocao> propostas, Set<StatusAdocao> statuses) {
        return propostas.stream().filter(proposta -> statuses.contains(proposta.getStatus())).count();
    }

    private long countReparos(List<AtendimentoDenuncia> reparos, Set<AtendimentoDenunciaStatus> statuses) {
        return reparos.stream().filter(reparo -> statuses.contains(reparo.getStatus())).count();
    }

    private Empresa getEmpresaFromAuthenticatedUser() {
        var username = AuthenticatedUser.username();
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
                praca.getLatitude(), praca.getLongitude(), praca.getDescricao(), ArquivoUrls.url(praca.getArquivo()),
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

    private ReparoEmpresaResumoDTO toReparoResumoDTO(AtendimentoDenuncia reparo) {
        return new ReparoEmpresaResumoDTO(
                reparo.getId(),
                reparo.getDenuncia().getId(),
                reparo.getDenuncia().getTitulo(),
                reparo.getStatus(),
                reparo.getDataAceite(),
                reparo.getDataInicio(),
                reparo.getDataConclusaoEmpresa(),
                reparo.getDataConfirmacaoAutor()
        );
    }
}
