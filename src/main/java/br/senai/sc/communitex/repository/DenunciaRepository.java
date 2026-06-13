package br.senai.sc.communitex.repository;

import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.enums.IssueType;
import br.senai.sc.communitex.model.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {

    List<Denuncia> findByTipoAndStatusNot(IssueType tipo, IssueStatus status);

    @Query("SELECT i FROM Issue i WHERE i.tipo = :tipo AND i.status NOT IN (:statusResolvidos)")
    List<Denuncia> findUnresolvedByType(
        @Param("tipo") IssueType tipo,
        @Param("statusResolvidos") List<IssueStatus> statusResolvidos
    );

    List<Denuncia> findByAutorId(Long autorId);

    List<Denuncia> findByStatus(IssueStatus status);
}
