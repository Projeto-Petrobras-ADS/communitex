package br.senai.sc.communitex.repository;

import br.senai.sc.communitex.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}
