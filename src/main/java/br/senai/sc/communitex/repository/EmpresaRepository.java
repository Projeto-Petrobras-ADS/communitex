package br.senai.sc.communitex.repository;

import br.senai.sc.communitex.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByCnpj(String cnpj);

    Optional<Empresa> findByUsuarioRepresentanteUsername(String username);
}
