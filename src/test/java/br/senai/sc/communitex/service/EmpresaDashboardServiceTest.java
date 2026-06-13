package br.senai.sc.communitex.service;

import br.senai.sc.communitex.enums.InteractionType;
import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.model.Denuncia;
import br.senai.sc.communitex.model.DenunciaInteracao;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.AdocaoRepository;
import br.senai.sc.communitex.repository.DenunciaRepository;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaDashboardServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private PracaRepository pracaRepository;
    @Mock
    private AdocaoRepository adocaoRepository;
    @Mock
    private DenunciaRepository denunciaRepository;

    @InjectMocks
    private EmpresaDashboardService dashboardService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenEmpresaData_whenObterDashboard_thenCalculatesIndicators() {
        var usuario = Usuario.builder().id(7L).username("empresa").nome("Responsavel").build();
        var empresa = Empresa.builder().id(3L).razaoSocial("Empresa Verde LTDA").nomeFantasia("Empresa Verde").usuarioRepresentante(usuario).build();
        var praca = Praca.builder().id(2L).nome("Praca Central").cidade("Floripa").metragemM2(1250.0).status(StatusPraca.ADOTADA).build();
        var aprovada = Adocao.builder().id(10L).empresa(empresa).praca(praca).status(StatusAdocao.APROVADA).dataInicio(LocalDate.now()).dataFim(LocalDate.now().plusDays(15)).build();
        var rejeitada = Adocao.builder().id(11L).empresa(empresa).praca(praca).status(StatusAdocao.REJEITADA).dataInicio(LocalDate.now().minusDays(5)).build();
        var apoio = DenunciaInteracao.builder().tipo(InteractionType.APOIO).build();
        var denuncia = Denuncia.builder().id(9L).titulo("Iluminacao").descricao("Sem luz").latitude(-27.0).longitude(-48.0)
                .status(IssueStatus.RESOLVIDA).dataCriacao(LocalDateTime.now()).autor(usuario).interacoes(List.of(apoio)).build();

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("empresa", "secret"));
        when(empresaRepository.buscarPorUsuarioRepresentanteUsername("empresa")).thenReturn(Optional.of(empresa));
        when(adocaoRepository.findByEmpresaId(3L)).thenReturn(List.of(aprovada, rejeitada));
        when(denunciaRepository.findByAutorId(7L)).thenReturn(List.of(denuncia));
        when(denunciaRepository.countByAutorId(7L)).thenReturn(1L);
        when(denunciaRepository.countByAutorIdAndStatus(7L, IssueStatus.RESOLVIDA)).thenReturn(1L);
        when(denunciaRepository.findTop5ByAutorIdOrderByDataCriacaoDesc(7L)).thenReturn(List.of(denuncia));
        when(pracaRepository.countByStatus(StatusPraca.DISPONIVEL)).thenReturn(6L);
        when(pracaRepository.findTop4ByStatusOrderByIdDesc(StatusPraca.DISPONIVEL)).thenReturn(List.of());

        var result = dashboardService.obterDashboard();

        assertThat(result.empresaNome()).isEqualTo("Empresa Verde");
        assertThat(result.pracasDisponiveis()).isEqualTo(6);
        assertThat(result.propostasAprovadas()).isEqualTo(1);
        assertThat(result.propostasRejeitadas()).isEqualTo(1);
        assertThat(result.totalApoiosRecebidos()).isEqualTo(1);
        assertThat(result.areaTotalAdotadaM2()).isEqualTo(1250);
        assertThat(result.taxaAprovacao()).isEqualTo(50);
        assertThat(result.adocoesProximasDoFim()).isEqualTo(1);
    }
}
