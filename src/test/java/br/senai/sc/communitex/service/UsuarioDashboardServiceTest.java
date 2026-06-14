package br.senai.sc.communitex.service;

import br.senai.sc.communitex.enums.InteractionType;
import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.model.Denuncia;
import br.senai.sc.communitex.model.DenunciaInteracao;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.DenunciaInteracaoRepository;
import br.senai.sc.communitex.repository.AtendimentoDenunciaRepository;
import br.senai.sc.communitex.repository.DenunciaRepository;
import br.senai.sc.communitex.repository.PessoaFisicaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDashboardServiceTest {

    @Mock
    private PessoaFisicaRepository pessoaFisicaRepository;
    @Mock
    private PracaRepository pracaRepository;
    @Mock
    private DenunciaRepository denunciaRepository;
    @Mock
    private DenunciaInteracaoRepository interacaoRepository;
    @Mock
    private AtendimentoDenunciaRepository atendimentoRepository;

    @InjectMocks
    private UsuarioDashboardService dashboardService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenUsuarioData_whenObterDashboard_thenCalculatesCommunityIndicators() {
        var usuario = Usuario.builder().id(7L).username("maria@email.com").nome("Maria").build();
        var pessoa = PessoaFisica.builder().id(3L).nome("Maria Silva").usuario(usuario).build();
        var apoio = DenunciaInteracao.builder().tipo(InteractionType.APOIO).build();
        var resolvida = Denuncia.builder().id(1L).titulo("Iluminacao").descricao("Sem luz").latitude(-27.0).longitude(-48.0)
                .status(IssueStatus.RESOLVIDA).dataCriacao(LocalDateTime.now()).autor(usuario).interacoes(List.of(apoio)).build();
        var aberta = Denuncia.builder().id(2L).titulo("Buraco").descricao("Via danificada").latitude(-27.0).longitude(-48.0)
                .status(IssueStatus.ABERTA).dataCriacao(LocalDateTime.now()).autor(usuario).interacoes(List.of()).build();

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("maria@email.com", "secret"));
        when(pessoaFisicaRepository.findByUsuarioUsername("maria@email.com")).thenReturn(Optional.of(pessoa));
        when(denunciaRepository.findByAutorId(7L)).thenReturn(List.of(resolvida, aberta));
        when(denunciaRepository.findTop5ByAutorIdOrderByDataCriacaoDesc(7L)).thenReturn(List.of(resolvida, aberta));
        when(pracaRepository.countByCadastradoPorId(3L)).thenReturn(2L);
        when(pracaRepository.countByCadastradoPorIdAndStatus(3L, StatusPraca.DISPONIVEL)).thenReturn(1L);
        when(pracaRepository.countByCadastradoPorIdAndStatus(3L, StatusPraca.EM_PROCESSO)).thenReturn(1L);
        when(pracaRepository.countByCadastradoPorIdAndStatus(3L, StatusPraca.ADOTADA)).thenReturn(0L);
        when(pracaRepository.findTop5ByCadastradoPorIdOrderByIdDesc(3L)).thenReturn(List.of());
        when(interacaoRepository.countByUsuarioIdAndTipo(7L, InteractionType.APOIO)).thenReturn(4L);
        when(interacaoRepository.countByUsuarioIdAndTipo(7L, InteractionType.COMENTARIO)).thenReturn(2L);

        var result = dashboardService.obterDashboard();

        assertThat(result.usuarioNome()).isEqualTo("Maria Silva");
        assertThat(result.pracasCadastradas()).isEqualTo(2);
        assertThat(result.denunciasRealizadas()).isEqualTo(2);
        assertThat(result.denunciasResolvidas()).isEqualTo(1);
        assertThat(result.totalApoiosRecebidos()).isEqualTo(1);
        assertThat(result.apoiosRealizados()).isEqualTo(4);
        assertThat(result.taxaResolucao()).isEqualTo(50);
    }
}
