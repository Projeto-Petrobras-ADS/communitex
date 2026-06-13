package br.senai.sc.communitex.repository;

import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.enums.IssueType;
import br.senai.sc.communitex.model.Denuncia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {

    @Override
    @EntityGraph(attributePaths = {"autor", "interacoes", "interacoes.usuario"})
    Page<Denuncia> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"autor", "interacoes", "interacoes.usuario"})
    List<Denuncia> findAll();

    List<Denuncia> findByTipoAndStatusNot(IssueType tipo, IssueStatus status);

    @Query("SELECT d FROM Denuncia d WHERE d.tipo = :tipo AND d.status NOT IN :statusResolvidos")
    List<Denuncia> findUnresolvedByType(
        @Param("tipo") IssueType tipo,
        @Param("statusResolvidos") List<IssueStatus> statusResolvidos
    );

    List<Denuncia> findByAutorId(Long autorId);

    List<Denuncia> findByStatus(IssueStatus status);

    @EntityGraph(attributePaths = {"autor", "interacoes", "interacoes.usuario"})
    List<Denuncia> findByLatitudeBetweenAndLongitudeBetween(
            Double minLatitude,
            Double maxLatitude,
            Double minLongitude,
            Double maxLongitude
    );
}
