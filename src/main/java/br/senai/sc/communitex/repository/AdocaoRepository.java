package br.senai.sc.communitex.repository;

import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.model.Adocao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdocaoRepository extends JpaRepository<Adocao, Long> {
    List<Adocao> findByStatus(StatusAdocao adocao);

    List<Adocao> findByEmpresaId(Long empresaId);
}
