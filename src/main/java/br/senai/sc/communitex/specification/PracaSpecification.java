package br.senai.sc.communitex.specification;

import br.senai.sc.communitex.dto.PracaPesquisaDTO;
import br.senai.sc.communitex.model.Praca;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PracaSpecification {

    public static Specification<Praca> comFiltros(PracaPesquisaDTO filtros) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtros != null) {
                if (filtros.id() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("id"), filtros.id()));
                }

                if (filtros.nome() != null && !filtros.nome().trim().isEmpty()) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("nome")),
                            "%" + filtros.nome().toLowerCase() + "%"
                    ));
                }

                if (filtros.cidade() != null && !filtros.cidade().trim().isEmpty()) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("cidade")),
                            "%" + filtros.cidade().toLowerCase() + "%"
                    ));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

