package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.PublicDashboardDTO;
import br.senai.sc.communitex.dto.PublicDashboardMonthlyDTO;
import br.senai.sc.communitex.enums.AtendimentoDenunciaStatus;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.model.AtendimentoDenuncia;
import br.senai.sc.communitex.repository.AdocaoRepository;
import br.senai.sc.communitex.repository.AtendimentoDenunciaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PublicDashboardService {

    private static final Set<StatusAdocao> STATUS_HISTORICO_ADOCAO = Set.of(
            StatusAdocao.APROVADA,
            StatusAdocao.CONCLUIDA,
            StatusAdocao.FINALIZADA
    );

    private final PracaRepository pracaRepository;
    private final AdocaoRepository adocaoRepository;
    private final AtendimentoDenunciaRepository atendimentoRepository;

    @Transactional(readOnly = true)
    public PublicDashboardDTO obterDashboard() {
        var totalPracas = pracaRepository.count();
        var pracasAdotadas = pracaRepository.findByStatus(StatusPraca.ADOTADA);
        var adocoes = adocaoRepository.findByStatusIn(STATUS_HISTORICO_ADOCAO);
        var reparos = atendimentoRepository.findByStatusNot(AtendimentoDenunciaStatus.CANCELADO);
        var confirmados = reparos.stream()
                .filter(reparo -> reparo.getStatus() == AtendimentoDenunciaStatus.CONFIRMADO_PELO_AUTOR)
                .toList();
        var confirmadosComData = confirmados.stream()
                .filter(reparo -> reparo.getDataConfirmacaoAutor() != null)
                .toList();

        var areaAdotada = pracasAdotadas.stream()
                .filter(praca -> praca.getMetragemM2() != null)
                .mapToDouble(praca -> praca.getMetragemM2())
                .sum();
        var tempoMedioHoras = confirmadosComData.stream()
                .filter(reparo -> reparo.getDataAceite() != null)
                .mapToLong(reparo -> Duration.between(reparo.getDataAceite(), reparo.getDataConfirmacaoAutor()).toMinutes())
                .average()
                .orElse(0) / 60.0;

        return new PublicDashboardDTO(
                totalPracas,
                pracasAdotadas.size(),
                roundOneDecimal(areaAdotada),
                confirmados.size(),
                roundOneDecimal(tempoMedioHoras),
                percentage(pracasAdotadas.size(), totalPracas),
                percentage(confirmados.size(), reparos.size()),
                buildMonthlyEvolution(adocoes, confirmadosComData)
        );
    }

    private List<PublicDashboardMonthlyDTO> buildMonthlyEvolution(
            List<Adocao> adocoes,
            List<AtendimentoDenuncia> confirmados
    ) {
        var firstAdoption = adocoes.stream()
                .map(Adocao::getDataInicio)
                .filter(date -> date != null)
                .map(YearMonth::from)
                .min(Comparator.naturalOrder());
        var firstRepair = confirmados.stream()
                .map(AtendimentoDenuncia::getDataConfirmacaoAutor)
                .filter(date -> date != null)
                .map(YearMonth::from)
                .min(Comparator.naturalOrder());

        var firstMonth = Stream.concat(firstAdoption.stream(), firstRepair.stream())
                .min(Comparator.naturalOrder());

        if (firstMonth.isEmpty()) return List.of();

        var result = new ArrayList<PublicDashboardMonthlyDTO>();
        var current = firstMonth.get();
        var last = YearMonth.now();
        long adoptionTotal = 0;
        long repairTotal = 0;

        while (!current.isAfter(last)) {
            var month = current;
            adoptionTotal += adocoes.stream()
                    .map(Adocao::getDataInicio)
                    .filter(date -> date != null && YearMonth.from(date).equals(month))
                    .count();
            repairTotal += confirmados.stream()
                    .map(AtendimentoDenuncia::getDataConfirmacaoAutor)
                    .filter(date -> date != null && YearMonth.from(date).equals(month))
                    .count();
            result.add(new PublicDashboardMonthlyDTO(month.toString(), adoptionTotal, repairTotal));
            current = current.plusMonths(1);
        }
        return result;
    }

    private double percentage(long value, long total) {
        return total == 0 ? 0 : roundOneDecimal(value * 100.0 / total);
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
