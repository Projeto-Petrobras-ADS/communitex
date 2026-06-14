package br.senai.sc.communitex.service;

import br.senai.sc.communitex.enums.AtendimentoDenunciaStatus;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.model.AtendimentoDenuncia;
import br.senai.sc.communitex.model.Denuncia;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.AdocaoRepository;
import br.senai.sc.communitex.repository.AtendimentoDenunciaRepository;
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
    private AtendimentoDenunciaRepository atendimentoRepository;

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
        var denuncia = Denuncia.builder().id(9L).titulo("Iluminacao").build();
        var aceito = reparo(20L, denuncia, empresa, AtendimentoDenunciaStatus.ACEITO);
        var emAndamento = reparo(21L, denuncia, empresa, AtendimentoDenunciaStatus.EM_ANDAMENTO);
        var aguardandoConfirmacao = reparo(22L, denuncia, empresa, AtendimentoDenunciaStatus.CONCLUIDO_PELA_EMPRESA);
        var confirmado = reparo(23L, denuncia, empresa, AtendimentoDenunciaStatus.CONFIRMADO_PELO_AUTOR);
        var contestado = reparo(24L, denuncia, empresa, AtendimentoDenunciaStatus.CONTESTADO);

        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("empresa", "secret", List.of())
        );
        when(empresaRepository.buscarPorUsuarioRepresentanteUsername("empresa")).thenReturn(Optional.of(empresa));
        when(adocaoRepository.findByEmpresaId(3L)).thenReturn(List.of(aprovada, rejeitada));
        when(atendimentoRepository.findByEmpresaIdOrderByDataAceiteDesc(3L))
                .thenReturn(List.of(contestado, aguardandoConfirmacao, emAndamento, aceito, confirmado));
        when(pracaRepository.countByStatus(StatusPraca.DISPONIVEL)).thenReturn(6L);
        when(pracaRepository.findTop4ByStatusOrderByIdDesc(StatusPraca.DISPONIVEL)).thenReturn(List.of());

        var result = dashboardService.obterDashboard();

        assertThat(result.empresaNome()).isEqualTo("Empresa Verde");
        assertThat(result.pracasDisponiveis()).isEqualTo(6);
        assertThat(result.propostasAprovadas()).isEqualTo(1);
        assertThat(result.propostasRejeitadas()).isEqualTo(1);
        assertThat(result.areaTotalAdotadaM2()).isEqualTo(1250);
        assertThat(result.taxaAprovacao()).isEqualTo(50);
        assertThat(result.adocoesProximasDoFim()).isEqualTo(1);
        assertThat(result.totalReparos()).isEqualTo(5);
        assertThat(result.reparosAtivos()).isEqualTo(3);
        assertThat(result.reparosAceitos()).isEqualTo(1);
        assertThat(result.reparosEmAndamento()).isEqualTo(1);
        assertThat(result.reparosAguardandoConfirmacao()).isEqualTo(1);
        assertThat(result.reparosConfirmados()).isEqualTo(1);
        assertThat(result.reparosContestados()).isEqualTo(1);
        assertThat(result.reparosRecentes()).hasSize(5);
        assertThat(result.reparosRecentes().get(0).status()).isEqualTo(AtendimentoDenunciaStatus.CONTESTADO);
    }

    @Test
    void givenEmpresaWithoutRepairs_whenObterDashboard_thenReturnsEmptyRepairIndicators() {
        var usuario = Usuario.builder().id(7L).username("empresa").build();
        var empresa = Empresa.builder().id(3L).razaoSocial("Empresa Verde LTDA").usuarioRepresentante(usuario).build();

        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("empresa", "secret", List.of())
        );
        when(empresaRepository.buscarPorUsuarioRepresentanteUsername("empresa")).thenReturn(Optional.of(empresa));
        when(adocaoRepository.findByEmpresaId(3L)).thenReturn(List.of());
        when(atendimentoRepository.findByEmpresaIdOrderByDataAceiteDesc(3L)).thenReturn(List.of());
        when(pracaRepository.findTop4ByStatusOrderByIdDesc(StatusPraca.DISPONIVEL)).thenReturn(List.of());

        var result = dashboardService.obterDashboard();

        assertThat(result.totalReparos()).isZero();
        assertThat(result.reparosAtivos()).isZero();
        assertThat(result.reparosRecentes()).isEmpty();
    }

    private AtendimentoDenuncia reparo(Long id, Denuncia denuncia, Empresa empresa, AtendimentoDenunciaStatus status) {
        return AtendimentoDenuncia.builder()
                .id(id)
                .denuncia(denuncia)
                .empresa(empresa)
                .status(status)
                .dataAceite(LocalDateTime.now())
                .build();
    }
}
