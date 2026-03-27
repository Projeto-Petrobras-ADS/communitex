package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.NotificationMessageDTO;
import br.senai.sc.communitex.enums.NotificationChannel;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Praca;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQNotificationServiceImplTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQNotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "exchange", "notification.exchange");
        ReflectionTestUtils.setField(notificationService, "routingKey", "notification.key");
    }

    @Test
    void givenMensagemValida_whenNotificarInteresseAdocao_thenPublicaNoRabbit() {
        var responsavel = pessoaFisica(10L, "Murilo", "murilo@email.com");
        var empresa = empresa(20L, "Empresa Teste", "contato@empresa.com");
        var praca = praca(30L, "Praca Central");

        notificationService.notificarInteresseAdocao(
                responsavel,
                empresa,
                praca,
                "Projeto de revitalizacao",
                NotificationChannel.PUSH
        );

        var captor = ArgumentCaptor.forClass(NotificationMessageDTO.class);
        verify(rabbitTemplate).convertAndSend(
                eq("notification.exchange"),
                eq("notification.key"),
                captor.capture()
        );

        var message = captor.getValue();
        assertEquals(NotificationChannel.PUSH, message.notificationType());
        assertEquals("murilo@email.com", message.recipient());
        assertEquals("interesse-adocao", message.template());
    }

    @Test
    void givenFalhaNaPublicacao_whenNotificarInteresseAdocao_thenNaoPropagaExcecao() {
        var responsavel = pessoaFisica(10L, "Murilo", "murilo@email.com");
        var empresa = empresa(20L, "Empresa Teste", "contato@empresa.com");
        var praca = praca(30L, "Praca Central");

        doThrow(new RuntimeException("Falha no broker")).when(rabbitTemplate)
                .convertAndSend(eq("notification.exchange"), eq("notification.key"), any(NotificationMessageDTO.class));

        assertDoesNotThrow(() -> notificationService.notificarInteresseAdocao(
                responsavel,
                empresa,
                praca,
                "Projeto de revitalizacao",
                NotificationChannel.EMAIL
        ));
    }

    private PessoaFisica pessoaFisica(Long id, String nome, String email) {
        var pessoa = new PessoaFisica();
        pessoa.setId(id);
        pessoa.setNome(nome);
        pessoa.setEmail(email);
        return pessoa;
    }

    private Empresa empresa(Long id, String razaoSocial, String email) {
        var empresa = new Empresa();
        empresa.setId(id);
        empresa.setRazaoSocial(razaoSocial);
        empresa.setEmail(email);
        empresa.setTelefone("48999990000");
        return empresa;
    }

    private Praca praca(Long id, String nome) {
        var praca = new Praca();
        praca.setId(id);
        praca.setNome(nome);
        praca.setCidade("Florianopolis");
        praca.setMetragemM2(1000.0);
        return praca;
    }
}



