package br.senai.sc.communitex.repository;

import br.senai.sc.communitex.model.Praca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PracaRepository extends JpaRepository<Praca, Long>, JpaSpecificationExecutor<Praca> {
}

