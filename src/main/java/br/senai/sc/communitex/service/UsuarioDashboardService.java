package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.DenunciaResponseDTO;
import br.senai.sc.communitex.dto.PracaResponseDTO;
import br.senai.sc.communitex.dto.UsuarioDashboardDTO;
import br.senai.sc.communitex.enums.InteractionType;
import br.senai.sc.communitex.enums.AtendimentoDenunciaStatus;
import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.model.Denuncia;
import br.senai.sc.communitex.model.DenunciaInteracao;
import br.senai.sc.communitex.repository.DenunciaInteracaoRepository;
import br.senai.sc.communitex.repository.AtendimentoDenunciaRepository;
import br.senai.sc.communitex.repository.DenunciaRepository;
import br.senai.sc.communitex.repository.PessoaFisicaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import br.senai.sc.communitex.util.ArquivoUrls;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioDashboardService {

    private static final Set<IssueStatus> STATUS_EM_ANDAMENTO = Set.of(IssueStatus.EM_ANALISE, IssueStatus.EM_ANDAMENTO);

    private final PessoaFisicaRepository pessoaFisicaRepository;
    private final PracaRepository pracaRepository;
    private final DenunciaRepository denunciaRepository;
    private final DenunciaInteracaoRepository interacaoRepository;
    private final AtendimentoDenunciaRepository atendimentoRepository;

    @Transactional(readOnly = true)
    public UsuarioDashboardDTO obterDashboard() {
        var pessoa = pessoaFisicaRepository.findByUsuarioUsername(authenticatedUsername())
                .orElseThrow(() -> new ForbiddenException("Nenhuma pessoa fisica associada ao usuario autenticado"));
        var usuario = pessoa.getUsuario();
        if (usuario == null) {
            throw new ForbiddenException("A pessoa fisica autenticada nao possui usuario associado");
        }

        var denuncias = denunciaRepository.findByAutorId(usuario.getId());
        var denunciasRealizadas = denuncias.size();
        var denunciasAbertas = countDenuncias(denuncias, Set.of(IssueStatus.ABERTA));
        var denunciasEmAndamento = countDenuncias(denuncias, STATUS_EM_ANDAMENTO);
        var denunciasResolvidas = countDenuncias(denuncias, Set.of(IssueStatus.RESOLVIDA));
        var apoiosRecebidos = denuncias.stream()
                .flatMap(denuncia -> interactions(denuncia).stream())
                .filter(interacao -> interacao.getTipo() == InteractionType.APOIO)
                .count();
        var taxaResolucao = denunciasRealizadas == 0
                ? 0
                : Math.round((denunciasResolvidas * 1000.0) / denunciasRealizadas) / 10.0;

        return new UsuarioDashboardDTO(
                pessoa.getNome(),
                pracaRepository.countByCadastradoPorId(pessoa.getId()),
                pracaRepository.countByCadastradoPorIdAndStatus(pessoa.getId(), StatusPraca.DISPONIVEL),
                pracaRepository.countByCadastradoPorIdAndStatus(pessoa.getId(), StatusPraca.EM_PROCESSO),
                pracaRepository.countByCadastradoPorIdAndStatus(pessoa.getId(), StatusPraca.ADOTADA),
                denunciasRealizadas,
                denunciasAbertas,
                denunciasEmAndamento,
                denunciasResolvidas,
                apoiosRecebidos,
                interacaoRepository.countByUsuarioIdAndTipo(usuario.getId(), InteractionType.APOIO),
                interacaoRepository.countByUsuarioIdAndTipo(usuario.getId(), InteractionType.COMENTARIO),
                taxaResolucao,
                atendimentoRepository.countByDenunciaAutorIdAndStatus(usuario.getId(), AtendimentoDenunciaStatus.CONCLUIDO_PELA_EMPRESA),
                atendimentoRepository.countByDenunciaAutorIdAndStatus(usuario.getId(), AtendimentoDenunciaStatus.CONFIRMADO_PELO_AUTOR),
                atendimentoRepository.countByDenunciaAutorIdAndStatus(usuario.getId(), AtendimentoDenunciaStatus.CONTESTADO),
                pracaRepository.findTop5ByCadastradoPorIdOrderByIdDesc(pessoa.getId()).stream().map(this::toPracaDTO).toList(),
                denunciaRepository.findTop5ByAutorIdOrderByDataCriacaoDesc(usuario.getId()).stream().map(this::toDenunciaDTO).toList()
        );
    }

    private String authenticatedUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = authentication != null ? authentication.getPrincipal() : null;
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String value) {
            return value;
        }
        throw new ForbiddenException("Usuario autenticado nao encontrado no contexto");
    }

    private long countDenuncias(List<Denuncia> denuncias, Set<IssueStatus> statuses) {
        return denuncias.stream().filter(denuncia -> statuses.contains(denuncia.getStatus())).count();
    }

    private PracaResponseDTO toPracaDTO(br.senai.sc.communitex.model.Praca praca) {
        return new PracaResponseDTO(
                praca.getId(), praca.getNome(), praca.getLogradouro(), praca.getBairro(), praca.getCidade(),
                praca.getLatitude(), praca.getLongitude(), praca.getDescricao(), ArquivoUrls.url(praca.getArquivo()),
                praca.getMetragemM2(), praca.getStatus()
        );
    }

    private DenunciaResponseDTO toDenunciaDTO(Denuncia denuncia) {
        var interacoes = interactions(denuncia);
        var apoios = interacoes.stream().filter(interacao -> interacao.getTipo() == InteractionType.APOIO).count();
        var autor = denuncia.getAutor();
        return new DenunciaResponseDTO(
                denuncia.getId(), denuncia.getTitulo(), denuncia.getDescricao(), denuncia.getLatitude(), denuncia.getLongitude(),
                ArquivoUrls.url(denuncia.getArquivo()), denuncia.getStatus(), denuncia.getTipo(), denuncia.getDataCriacao(), autor.getId(),
                autor.getNome() != null ? autor.getNome() : autor.getUsername(), interacoes.size(), (int) apoios
        );
    }

    private List<DenunciaInteracao> interactions(Denuncia denuncia) {
        return denuncia.getInteracoes() != null ? denuncia.getInteracoes() : List.of();
    }
}
