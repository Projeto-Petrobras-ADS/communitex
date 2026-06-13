package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.DenunciaDetailResponseDTO;
import br.senai.sc.communitex.dto.DenunciaInteracaoRequestDTO;
import br.senai.sc.communitex.dto.DenunciaInteracaoResponseDTO;
import br.senai.sc.communitex.dto.DenunciaRequestDTO;
import br.senai.sc.communitex.dto.DenunciaResponseDTO;
import br.senai.sc.communitex.enums.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DenunciaService {

    DenunciaResponseDTO criar(DenunciaRequestDTO dto);

    DenunciaResponseDTO buscarPorId(Long id);

    DenunciaDetailResponseDTO buscarPorIdComDetalhes(Long id);

    List<DenunciaResponseDTO> listarTodas();

    Page<DenunciaResponseDTO> listarTodas(Pageable pageable);

    List<DenunciaResponseDTO> buscarPorProximidade(Double latitude, Double longitude, Double radiusMeters);

    DenunciaResponseDTO atualizarStatus(Long id, IssueStatus status);

    DenunciaInteracaoResponseDTO adicionarInteracao(Long issueId, DenunciaInteracaoRequestDTO dto);

    void removerInteracao(Long issueId, Long interactionId);

    List<DenunciaInteracaoResponseDTO> listarInteracoesDaDenuncia(Long issueId);
}
