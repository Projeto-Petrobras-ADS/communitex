package br.senai.sc.communitex.repository;

import br.senai.sc.communitex.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    @Query("SELECT e FROM Empresa e WHERE e.cnpj = :cnpj")
    Optional<Empresa> buscarPorCnpj(String cnpj);

    @Query("SELECT e FROM Empresa e WHERE e.usuarioRepresentante.username = :username")
    Optional<Empresa> buscarPorUsuarioRepresentanteUsername(String username);
}
