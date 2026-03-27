package br.senai.sc.communitex.service;

import br.senai.sc.communitex.enums.NotificationChannel;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Praca;

public interface NotificationService {

    /**
     * Publishes a notification to RabbitMQ so the notification microservice
     * can deliver it via the channel chosen by the recipient.
     *
     * @param responsavel the person who will receive the notification
     * @param solicitante the company that expressed interest
     * @param praca       the square being adopted
     * @param proposta    the adoption proposal text
     * @param channel     the delivery channel preferred by the recipient (PUSH, EMAIL, SMS)
     */
    void notificarInteresseAdocao(PessoaFisica responsavel,
                                  Empresa solicitante,
                                  Praca praca,
                                  String proposta,
                                  NotificationChannel channel);
}

