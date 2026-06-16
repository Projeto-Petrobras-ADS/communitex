package br.senai.sc.communitex.repository;

import br.senai.sc.communitex.enums.InteractionType;
import br.senai.sc.communitex.model.DenunciaInteracao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DenunciaInteracaoRepository extends JpaRepository<DenunciaInteracao, Long> {

    List<DenunciaInteracao> findByIssueIdOrderByDataCriacaoDesc(Long issueId);

    long countByIssueIdAndTipo(Long issueId, InteractionType tipo);

    long countByUsuarioIdAndTipo(Long usuarioId, InteractionType tipo);

    Optional<DenunciaInteracao> findByIssueIdAndUsuarioIdAndTipo(Long issueId, Long usuarioId, InteractionType tipo);

    @Query("SELECT i FROM DenunciaInteracao i WHERE i.issue.id = :issueId AND i.tipo = 'COMENTARIO' ORDER BY i.dataCriacao DESC")
    List<DenunciaInteracao> findComentariosByIssueId(@Param("issueId") Long issueId);
}
