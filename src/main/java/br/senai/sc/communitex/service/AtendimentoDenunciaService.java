package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.AssumirAtendimentoRequestDTO;
import br.senai.sc.communitex.dto.AtendimentoDenunciaResponseDTO;
import br.senai.sc.communitex.dto.ConcluirAtendimentoRequestDTO;
import br.senai.sc.communitex.dto.ContestarAtendimentoRequestDTO;
import br.senai.sc.communitex.dto.DenunciaResponseDTO;
import br.senai.sc.communitex.enums.AtendimentoDenunciaStatus;
import br.senai.sc.communitex.enums.InteractionType;
import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.exception.BusinessException;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.AtendimentoDenuncia;
import br.senai.sc.communitex.model.Denuncia;
import br.senai.sc.communitex.model.DenunciaInteracao;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.AtendimentoDenunciaRepository;
import br.senai.sc.communitex.repository.DenunciaRepository;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import br.senai.sc.communitex.util.ArquivoUrls;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AtendimentoDenunciaService {

    private static final Set<IssueStatus> STATUS_DISPONIVEIS = Set.of(IssueStatus.ABERTA, IssueStatus.EM_ANALISE);

    private final AtendimentoDenunciaRepository atendimentoRepository;
    private final DenunciaRepository denunciaRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ArquivoService arquivoService;

    @Transactional
    public AtendimentoDenunciaResponseDTO assumir(Long denunciaId, AssumirAtendimentoRequestDTO request) {
        var empresa = authenticatedEmpresa();
        var denuncia = denuncia(denunciaId);
        if (!STATUS_DISPONIVEIS.contains(denuncia.getStatus())) {
            throw new BusinessException("Esta denuncia nao esta disponivel para atendimento");
        }
        if (atendimentoRepository.existsByDenunciaId(denunciaId)) {
            throw new BusinessException("Esta denuncia ja possui uma empresa responsavel");
        }

        var atendimento = AtendimentoDenuncia.builder()
                .denuncia(denuncia)
                .empresa(empresa)
                .status(AtendimentoDenunciaStatus.ACEITO)
                .descricaoPlanejada(request.descricaoPlanejada().trim())
                .dataAceite(LocalDateTime.now())
                .build();
        denuncia.setStatus(IssueStatus.EM_ANALISE);
        denunciaRepository.save(denuncia);
        return toResponse(atendimentoRepository.save(atendimento));
    }

    @Transactional
    public AtendimentoDenunciaResponseDTO iniciar(Long denunciaId) {
        var atendimento = managedAtendimento(denunciaId);
        requireStatus(atendimento, AtendimentoDenunciaStatus.ACEITO);
        atendimento.setStatus(AtendimentoDenunciaStatus.EM_ANDAMENTO);
        atendimento.setDataInicio(LocalDateTime.now());
        atendimento.getDenuncia().setStatus(IssueStatus.EM_ANDAMENTO);
        denunciaRepository.save(atendimento.getDenuncia());
        return toResponse(atendimentoRepository.save(atendimento));
    }

    @Transactional
    public AtendimentoDenunciaResponseDTO concluir(Long denunciaId, ConcluirAtendimentoRequestDTO request, MultipartFile arquivo) {
        var atendimento = managedAtendimento(denunciaId);
        requireStatus(atendimento, AtendimentoDenunciaStatus.EM_ANDAMENTO);
        atendimento.setStatus(AtendimentoDenunciaStatus.CONCLUIDO_PELA_EMPRESA);
        atendimento.setDescricaoReparo(request.descricaoReparo().trim());
        atendimento.setArquivo(arquivoService.salvarImagem(arquivo));
        atendimento.setDataConclusaoEmpresa(LocalDateTime.now());
        atendimento.getDenuncia().setStatus(IssueStatus.AGUARDANDO_CONFIRMACAO);
        denunciaRepository.save(atendimento.getDenuncia());
        return toResponse(atendimentoRepository.save(atendimento));
    }

    @Transactional
    public AtendimentoDenunciaResponseDTO confirmar(Long denunciaId) {
        var atendimento = authorAtendimento(denunciaId);
        requireStatus(atendimento, AtendimentoDenunciaStatus.CONCLUIDO_PELA_EMPRESA);
        atendimento.setStatus(AtendimentoDenunciaStatus.CONFIRMADO_PELO_AUTOR);
        atendimento.setDataConfirmacaoAutor(LocalDateTime.now());
        atendimento.getDenuncia().setStatus(IssueStatus.RESOLVIDA);
        denunciaRepository.save(atendimento.getDenuncia());
        return toResponse(atendimentoRepository.save(atendimento));
    }

    @Transactional
    public AtendimentoDenunciaResponseDTO contestar(Long denunciaId, ContestarAtendimentoRequestDTO request) {
        var atendimento = authorAtendimento(denunciaId);
        requireStatus(atendimento, AtendimentoDenunciaStatus.CONCLUIDO_PELA_EMPRESA);
        atendimento.setStatus(AtendimentoDenunciaStatus.CONTESTADO);
        atendimento.setMotivoContestacao(request.motivo().trim());
        atendimento.getDenuncia().setStatus(IssueStatus.CONTESTADA);
        denunciaRepository.save(atendimento.getDenuncia());
        return toResponse(atendimentoRepository.save(atendimento));
    }

    @Transactional(readOnly = true)
    public AtendimentoDenunciaResponseDTO buscar(Long denunciaId) {
        return toResponse(atendimento(denunciaId));
    }

    @Transactional(readOnly = true)
    public List<AtendimentoDenunciaResponseDTO> listarMeusAtendimentos() {
        var empresa = authenticatedEmpresa();
        return atendimentoRepository.findByEmpresaIdOrderByDataAceiteDesc(empresa.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DenunciaResponseDTO> listarDisponiveis() {
        authenticatedEmpresa();
        return denunciaRepository.findByStatusIn(List.copyOf(STATUS_DISPONIVEIS)).stream()
                .filter(denuncia -> !atendimentoRepository.existsByDenunciaId(denuncia.getId()))
                .map(this::toDenunciaDTO)
                .toList();
    }

    private AtendimentoDenuncia managedAtendimento(Long denunciaId) {
        var atendimento = atendimento(denunciaId);
        var empresa = authenticatedEmpresa();
        if (!atendimento.getEmpresa().getId().equals(empresa.getId())) {
            throw new ForbiddenException("Apenas a empresa responsavel pode atualizar este atendimento");
        }
        return atendimento;
    }

    private AtendimentoDenuncia authorAtendimento(Long denunciaId) {
        var atendimento = atendimento(denunciaId);
        var usuario = authenticatedUser();
        if (!atendimento.getDenuncia().getAutor().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Apenas o autor da denuncia pode confirmar ou contestar o reparo");
        }
        return atendimento;
    }

    private AtendimentoDenuncia atendimento(Long denunciaId) {
        return atendimentoRepository.findByDenunciaId(denunciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Atendimento nao encontrado para a denuncia: " + denunciaId));
    }

    private Denuncia denuncia(Long id) {
        return denunciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia nao encontrada com ID: " + id));
    }

    private Empresa authenticatedEmpresa() {
        var username = authenticatedUsername();
        return empresaRepository.buscarPorUsuarioRepresentanteUsername(username)
                .orElseThrow(() -> new ForbiddenException("Nenhuma empresa associada ao usuario autenticado"));
    }

    private Usuario authenticatedUser() {
        var username = authenticatedUsername();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ForbiddenException("Usuario autenticado nao encontrado"));
    }

    private String authenticatedUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = authentication != null ? authentication.getPrincipal() : null;
        if (principal instanceof UserDetails userDetails) return userDetails.getUsername();
        if (principal instanceof String value) return value;
        throw new ForbiddenException("Usuario autenticado nao encontrado no contexto");
    }

    private void requireStatus(AtendimentoDenuncia atendimento, AtendimentoDenunciaStatus expected) {
        if (atendimento.getStatus() != expected) {
            throw new BusinessException("Acao indisponivel para o status atual do atendimento");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private AtendimentoDenunciaResponseDTO toResponse(AtendimentoDenuncia atendimento) {
        var username = authenticatedUsername();
        var empresa = atendimento.getEmpresa();
        var denuncia = atendimento.getDenuncia();
        var podeGerenciar = empresa.getUsuarioRepresentante() != null
                && username.equals(empresa.getUsuarioRepresentante().getUsername());
        var podeConfirmar = denuncia.getAutor() != null && username.equals(denuncia.getAutor().getUsername());
        var empresaNome = empresa.getNomeFantasia() != null && !empresa.getNomeFantasia().isBlank()
                ? empresa.getNomeFantasia() : empresa.getRazaoSocial();
        return new AtendimentoDenunciaResponseDTO(
                atendimento.getId(), denuncia.getId(), denuncia.getTitulo(), empresa.getId(), empresaNome,
                atendimento.getStatus(), atendimento.getDescricaoPlanejada(), atendimento.getDescricaoReparo(),
                ArquivoUrls.url(atendimento.getArquivo()), atendimento.getMotivoContestacao(), atendimento.getDataAceite(),
                atendimento.getDataInicio(), atendimento.getDataConclusaoEmpresa(), atendimento.getDataConfirmacaoAutor(),
                podeGerenciar, podeConfirmar
        );
    }

    private DenunciaResponseDTO toDenunciaDTO(Denuncia denuncia) {
        List<DenunciaInteracao> interacoes = denuncia.getInteracoes() != null ? denuncia.getInteracoes() : List.of();
        var apoios = interacoes.stream().filter(i -> i.getTipo() == InteractionType.APOIO).count();
        var autor = denuncia.getAutor();
        return new DenunciaResponseDTO(
                denuncia.getId(), denuncia.getTitulo(), denuncia.getDescricao(), denuncia.getLatitude(), denuncia.getLongitude(),
                ArquivoUrls.url(denuncia.getArquivo()), denuncia.getStatus(), denuncia.getTipo(), denuncia.getDataCriacao(), autor.getId(),
                autor.getNome() != null ? autor.getNome() : autor.getUsername(), interacoes.size(), (int) apoios
        );
    }
}
