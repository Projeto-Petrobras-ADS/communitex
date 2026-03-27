package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.NotificationMessageDTO;
import br.senai.sc.communitex.enums.NotificationChannel;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQNotificationServiceImpl implements NotificationService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.notification.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.notification.routing-key}")
    private String routingKey;

    @Override
    public void notificarInteresseAdocao(PessoaFisica responsavel,
                                         Empresa solicitante,
                                         Praca praca,
                                         String proposta,
                                         NotificationChannel channel) {

        var subject = "Nova Manifestação de Interesse na Praça: " + praca.getNome();

        var body = """
                Olá %s,

                A empresa "%s" manifestou interesse em adotar a praça cadastrada por você!

                Detalhes:
                - Praça: %s
                - Empresa Solicitante: %s
                - Email da Empresa: %s
                - Telefone da Empresa: %s

                Proposta:
                %s

                Acesse o sistema para mais informações.

                Atenciosamente,
                Equipe Communitex
                """.formatted(
                responsavel.getNome(),
                solicitante.getRazaoSocial(),
                praca.getNome(),
                solicitante.getRazaoSocial(),
                solicitante.getEmail(),
                solicitante.getTelefone() != null ? solicitante.getTelefone() : "Não informado",
                proposta
        );

        // data: structured info the consumer can use to render a rich template
        var data = Map.of(
                "recipientName",  responsavel.getNome(),
                "pracaNome",      praca.getNome(),
                "empresaNome",    solicitante.getRazaoSocial(),
                "empresaEmail",   solicitante.getEmail(),
                "proposta",       proposta
        );

        // metadata: routing / deep-link ids
        var metadata = Map.of(
                "pracaId",    String.valueOf(praca.getId()),
                "empresaId",  String.valueOf(solicitante.getId()),
                "recipientId", String.valueOf(responsavel.getId())
        );

        var message = NotificationMessageDTO.of(
                channel,
                responsavel.getEmail(),   // recipient = email/push-token/phone depending on channel
                subject,
                body,
                "interesse-adocao",       // template name the notification service will use
                data,
                metadata
        );

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("Notificação publicada no RabbitMQ — destinatário ID: {}, canal: {}",
                    responsavel.getId(), channel);
        } catch (Exception e) {
            log.error("Erro ao publicar notificação no RabbitMQ — destinatário ID: {}",
                    responsavel.getId(), e);
        }
    }
}
