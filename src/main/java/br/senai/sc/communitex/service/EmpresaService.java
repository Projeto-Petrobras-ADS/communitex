package br.senai.sc.communitex.service;

import br.senai.sc.communitex.model.Empresa;

public interface EmpresaService {
    /**
     * Busca uma empresa por ID e retorna a entidade
     *
     * @param id ID da empresa
     * @return Empresa encontrada
     */
    Empresa findEntityById(Long id);
}

