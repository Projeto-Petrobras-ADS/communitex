package br.senai.sc.communitex.repository;

import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.model.Adocao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Collection;

public interface AdocaoRepository extends JpaRepository<Adocao, Long> {
    List<Adocao> findByStatus(StatusAdocao adocao);

    List<Adocao> findByPraca_Id(Long idPraca);

    @EntityGraph(attributePaths = {"praca", "empresa"})
    List<Adocao> findByEmpresaId(Long empresaId);

    boolean existsByEmpresaIdAndPracaIdAndStatusIn(Long empresaId, Long pracaId, Collection<StatusAdocao> statuses);

    List<Adocao> findByDataInicioGreaterThanEqualAndDataFimLessThanEqual(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);


    @Query("SELECT a FROM Adocao a WHERE (:status IS NULL OR a.status = :status) AND a.dataFim BETWEEN :hoje AND :limite")
    List<Adocao> findAdocoesByPrazoEStatus(@Param("hoje") LocalDate hoje, @Param("limite") LocalDate limite, @Param("status") StatusAdocao status);
}
