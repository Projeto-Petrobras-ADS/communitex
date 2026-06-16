package br.senai.sc.communitex.service;

import br.senai.sc.communitex.enums.AtendimentoDenunciaStatus;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.model.AtendimentoDenuncia;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.repository.AdocaoRepository;
import br.senai.sc.communitex.repository.AtendimentoDenunciaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicDashboardServiceTest {

    @Mock
    private PracaRepository pracaRepository;
    @Mock
    private AdocaoRepository adocaoRepository;
    @Mock
    private AtendimentoDenunciaRepository atendimentoRepository;

    @InjectMocks
    private PublicDashboardService dashboardService;

    @Test
    void givenPlatformData_whenObterDashboard_thenCalculatesImpactAndFillsEmptyMonths() {
        var firstMonth = YearMonth.now().minusMonths(2);
        var adoptedSquare = Praca.builder().status(StatusPraca.ADOTADA).metragemM2(1250.5).build();
        var adoption = Adocao.builder().status(StatusAdocao.APROVADA).dataInicio(firstMonth.atDay(10)).build();
        var confirmed = AtendimentoDenuncia.builder()
                .status(AtendimentoDenunciaStatus.CONFIRMADO_PELO_AUTOR)
                .dataAceite(firstMonth.atDay(15).atTime(8, 0))
                .dataConfirmacaoAutor(firstMonth.atDay(16).atTime(20, 0))
                .build();
        var active = AtendimentoDenuncia.builder().status(AtendimentoDenunciaStatus.EM_ANDAMENTO).build();

        when(pracaRepository.count()).thenReturn(4L);
        when(pracaRepository.findByStatus(StatusPraca.ADOTADA)).thenReturn(List.of(adoptedSquare));
        when(adocaoRepository.findByStatusIn(Set.of(StatusAdocao.APROVADA, StatusAdocao.CONCLUIDA, StatusAdocao.FINALIZADA)))
                .thenReturn(List.of(adoption));
        when(atendimentoRepository.findByStatusNot(AtendimentoDenunciaStatus.CANCELADO))
                .thenReturn(List.of(confirmed, active));

        var result = dashboardService.obterDashboard();

        assertThat(result.totalPracas()).isEqualTo(4);
        assertThat(result.pracasAdotadas()).isEqualTo(1);
        assertThat(result.areaAdotadaM2()).isEqualTo(1250.5);
        assertThat(result.reparosConfirmados()).isEqualTo(1);
        assertThat(result.tempoMedioReparoHoras()).isEqualTo(36);
        assertThat(result.taxaAdocao()).isEqualTo(25);
        assertThat(result.taxaConfirmacaoReparos()).isEqualTo(50);
        assertThat(result.evolucaoMensal()).hasSize(3);
        assertThat(result.evolucaoMensal().get(1).adocoesAcumuladas()).isEqualTo(1);
        assertThat(result.evolucaoMensal().get(1).reparosConfirmadosAcumulados()).isEqualTo(1);
    }

    @Test
    void givenNoData_whenObterDashboard_thenReturnsZerosAndEmptyEvolution() {
        when(pracaRepository.findByStatus(StatusPraca.ADOTADA)).thenReturn(List.of());
        when(adocaoRepository.findByStatusIn(Set.of(StatusAdocao.APROVADA, StatusAdocao.CONCLUIDA, StatusAdocao.FINALIZADA)))
                .thenReturn(List.of());
        when(atendimentoRepository.findByStatusNot(AtendimentoDenunciaStatus.CANCELADO)).thenReturn(List.of());

        var result = dashboardService.obterDashboard();

        assertThat(result.totalPracas()).isZero();
        assertThat(result.tempoMedioReparoHoras()).isZero();
        assertThat(result.taxaAdocao()).isZero();
        assertThat(result.taxaConfirmacaoReparos()).isZero();
        assertThat(result.evolucaoMensal()).isEmpty();
    }

    @Test
    void givenTwoConfirmedRepairs_whenObterDashboard_thenAveragesDuration() {
        var now = LocalDateTime.now();
        var first = confirmed(now.minusHours(10), now);
        var second = confirmed(now.minusHours(30), now);

        when(pracaRepository.findByStatus(StatusPraca.ADOTADA)).thenReturn(List.of());
        when(adocaoRepository.findByStatusIn(Set.of(StatusAdocao.APROVADA, StatusAdocao.CONCLUIDA, StatusAdocao.FINALIZADA)))
                .thenReturn(List.of());
        when(atendimentoRepository.findByStatusNot(AtendimentoDenunciaStatus.CANCELADO)).thenReturn(List.of(first, second));

        assertThat(dashboardService.obterDashboard().tempoMedioReparoHoras()).isEqualTo(20);
    }

    private AtendimentoDenuncia confirmed(LocalDateTime accepted, LocalDateTime confirmed) {
        return AtendimentoDenuncia.builder()
                .status(AtendimentoDenunciaStatus.CONFIRMADO_PELO_AUTOR)
                .dataAceite(accepted)
                .dataConfirmacaoAutor(confirmed)
                .build();
    }
}
