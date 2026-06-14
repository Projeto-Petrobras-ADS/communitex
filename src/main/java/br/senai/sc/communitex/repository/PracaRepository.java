package br.senai.sc.communitex.repository;

import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.model.Praca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface PracaRepository extends JpaRepository<Praca, Long>, JpaSpecificationExecutor<Praca> {

    @Override
    @EntityGraph(attributePaths = {"cadastradoPor", "cadastradoPor.usuario"})
    Page<Praca> findAll(Specification<Praca> spec, Pageable pageable);

    long countByStatus(StatusPraca status);

    List<Praca> findTop4ByStatusOrderByIdDesc(StatusPraca status);

    long countByCadastradoPorId(Long pessoaFisicaId);

    long countByCadastradoPorIdAndStatus(Long pessoaFisicaId, StatusPraca status);

    List<Praca> findTop5ByCadastradoPorIdOrderByIdDesc(Long pessoaFisicaId);

    Optional<Praca> findByNomeAndCidade(String nome, String cidade);
}

