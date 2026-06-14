package br.senai.sc.communitex.repository;

import br.senai.sc.communitex.model.Arquivo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArquivoRepository extends JpaRepository<Arquivo, Long> {
}
