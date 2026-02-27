package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void enviarNotificacaoInteresse(PessoaFisica responsavel, Empresa solicitante, Praca praca, String proposta) {
        try {
            var message = new SimpleMailMessage();
            message.setTo(responsavel.getEmail());
            message.setSubject("Nova Manifestação de Interesse na Praça: " + praca.getNome());

            var corpo = """
                Olá %s,
                
                Uma empresa manifestou interesse em adotar a praça cadastrada por você!
                
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
                    praca.getNome(),
                    solicitante.getRazaoSocial(),
                    solicitante.getEmail(),
                    solicitante.getTelefone() != null ? solicitante.getTelefone() : "Não informado",
                    proposta
                );

            message.setText(corpo);

            mailSender.send(message);
            log.info("Email de notificação enviado para: {}", responsavel.getEmail());

        } catch (Exception e) {
            log.error("Erro ao enviar email de notificação para: {}", responsavel.getEmail(), e);
        }
    }
}
