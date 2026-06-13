package br.senai.sc.communitex.repository;

import br.senai.sc.communitex.model.Praca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface PracaRepository extends JpaRepository<Praca, Long>, JpaSpecificationExecutor<Praca> {

    @Override
    @EntityGraph(attributePaths = {"cadastradoPor", "cadastradoPor.usuario"})
    Page<Praca> findAll(Specification<Praca> spec, Pageable pageable);
}

