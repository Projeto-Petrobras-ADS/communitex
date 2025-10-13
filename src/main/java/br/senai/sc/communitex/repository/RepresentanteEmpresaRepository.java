package br.senai.sc.communitex.repository;

import br.senai.sc.communitex.model.RepresentanteEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepresentanteEmpresaRepository extends JpaRepository<RepresentanteEmpresa, Long> {
    List<RepresentanteEmpresa> findByEmpresaId(Long empresaId);
}
