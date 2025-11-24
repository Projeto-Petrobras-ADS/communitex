package br.senai.sc.communitex.service;

import br.senai.sc.communitex.model.PessoaFisica;

public interface PessoaFisicaService {
    PessoaFisica findByUsuarioUsername(String username);
}

