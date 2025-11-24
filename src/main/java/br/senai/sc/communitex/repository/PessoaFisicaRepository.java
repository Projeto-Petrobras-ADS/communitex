package br.senai.sc.communitex.repository;

import br.senai.sc.communitex.model.PessoaFisica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PessoaFisicaRepository extends JpaRepository<PessoaFisica, Long> {
    Optional<PessoaFisica> findByCpf(String cpf);
    Optional<PessoaFisica> findByEmail(String email);
    Optional<PessoaFisica> findByUsuarioUsername(String username);
}

