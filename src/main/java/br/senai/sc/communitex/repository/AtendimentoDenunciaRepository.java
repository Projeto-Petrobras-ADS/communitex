package br.senai.sc.communitex.repository;

import br.senai.sc.communitex.enums.AtendimentoDenunciaStatus;
import br.senai.sc.communitex.model.AtendimentoDenuncia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AtendimentoDenunciaRepository extends JpaRepository<AtendimentoDenuncia, Long> {

    boolean existsByDenunciaId(Long denunciaId);

    @EntityGraph(attributePaths = {"denuncia", "denuncia.autor", "empresa"})
    Optional<AtendimentoDenuncia> findByDenunciaId(Long denunciaId);

    @EntityGraph(attributePaths = {"denuncia", "denuncia.autor", "empresa"})
    List<AtendimentoDenuncia> findByEmpresaIdOrderByDataAceiteDesc(Long empresaId);

    List<AtendimentoDenuncia> findByStatusNot(AtendimentoDenunciaStatus status);

    long countByEmpresaIdAndStatus(Long empresaId, AtendimentoDenunciaStatus status);

    long countByDenunciaAutorIdAndStatus(Long autorId, AtendimentoDenunciaStatus status);

    long countByDenunciaAutorIdAndStatusAndDenunciaAtivaTrue(Long autorId, AtendimentoDenunciaStatus status);
}
