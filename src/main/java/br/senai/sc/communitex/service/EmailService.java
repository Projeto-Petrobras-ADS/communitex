package br.senai.sc.communitex.service;

import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Praca;

public interface EmailService {
    void enviarNotificacaoInteresse(PessoaFisica responsavel, Empresa solicitante, Praca praca, String proposta);
}

