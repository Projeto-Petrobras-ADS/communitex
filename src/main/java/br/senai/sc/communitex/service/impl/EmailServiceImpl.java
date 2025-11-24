package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviarNotificacaoInteresse(PessoaFisica responsavel, Empresa solicitante, Praca praca, String proposta) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(responsavel.getEmail());
            message.setSubject("Nova Manifestação de Interesse na Praça: " + praca.getNome());

            String corpo = String.format(
                "Olá %s,\n\n" +
                "Uma empresa manifestou interesse em adotar a praça cadastrada por você!\n\n" +
                "Detalhes:\n" +
                "- Praça: %s\n" +
                "- Empresa Solicitante: %s\n" +
                "- Email da Empresa: %s\n" +
                "- Telefone da Empresa: %s\n\n" +
                "Proposta:\n%s\n\n" +
                "Acesse o sistema para mais informações.\n\n" +
                "Atenciosamente,\n" +
                "Equipe Communitex",
                responsavel.getNome(),
                praca.getNome(),
                solicitante.getRazaoSocial(),
                solicitante.getEmail(),
                solicitante.getTelefone() != null ? solicitante.getTelefone() : "Não informado",
                proposta
            );

            message.setText(corpo);

            mailSender.send(message);
            logger.info("Email de notificação enviado para: {}", responsavel.getEmail());

        } catch (Exception e) {
            logger.error("Erro ao enviar email de notificação para: {}", responsavel.getEmail(), e);
        }
    }
}

