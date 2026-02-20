package br.senai.sc.communitex.repository;

import br.senai.sc.communitex.enums.InteractionType;
import br.senai.sc.communitex.model.IssueInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IssueInteractionRepository extends JpaRepository<IssueInteraction, Long> {

    List<IssueInteraction> findByIssueIdOrderByDataCriacaoDesc(Long issueId);

    long countByIssueIdAndTipo(Long issueId, InteractionType tipo);

    Optional<IssueInteraction> findByIssueIdAndUsuarioIdAndTipo(Long issueId, Long usuarioId, InteractionType tipo);

    @Query("SELECT i FROM IssueInteraction i WHERE i.issue.id = :issueId AND i.tipo = 'COMENTARIO' ORDER BY i.dataCriacao DESC")
    List<IssueInteraction> findComentariosByIssueId(@Param("issueId") Long issueId);
}
