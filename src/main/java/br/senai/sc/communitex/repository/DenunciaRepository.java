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
import java.util.Optional;

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

    @EntityGraph(attributePaths = {"autor", "interacoes", "interacoes.usuario"})
    List<Denuncia> findByAutorId(Long autorId);

    long countByAutorId(Long autorId);

    long countByAutorIdAndStatus(Long autorId, IssueStatus status);

    @EntityGraph(attributePaths = {"autor", "interacoes", "interacoes.usuario"})
    List<Denuncia> findTop5ByAutorIdOrderByDataCriacaoDesc(Long autorId);

    List<Denuncia> findByStatus(IssueStatus status);

    @EntityGraph(attributePaths = {"autor", "interacoes", "interacoes.usuario"})
    List<Denuncia> findByStatusIn(List<IssueStatus> statuses);

    @EntityGraph(attributePaths = {"autor", "interacoes", "interacoes.usuario"})
    List<Denuncia> findByLatitudeBetweenAndLongitudeBetween(
            Double minLatitude,
            Double maxLatitude,
            Double minLongitude,
            Double maxLongitude
    );

    Optional<Denuncia> findByTituloAndAutorId(String titulo, Long autorId);
}
