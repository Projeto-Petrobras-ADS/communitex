package br.senai.sc.communitex.service;

import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.repository.AdocaoRepository;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdocaoServiceTest {

    @Mock
    private AdocaoRepository adocaoRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private PracaRepository pracaRepository;
    @InjectMocks
    private AdocaoService service;

    @Test
    void listsAdoptionsAndHandlesMissingRelations() {
        var adocao = Adocao.builder().id(1L).dataInicio(LocalDate.now())
                .status(StatusAdocao.PROPOSTA).build();
        when(adocaoRepository.findAll()).thenReturn(List.of(adocao));

        var result = service.findAll();

        assertEquals(1, result.size());
        assertNull(result.get(0).empresaId());
        assertNull(result.get(0).pracaId());
    }

    @Test
    void updatesStatusAndSynchronizesSquareStatus() {
        var praca = Praca.builder().id(2L).status(StatusPraca.DISPONIVEL).metragemM2(10.0).build();
        var adocao = Adocao.builder().id(1L).dataInicio(LocalDate.now())
                .status(StatusAdocao.PROPOSTA).praca(praca).build();
        when(adocaoRepository.findById(1L)).thenReturn(Optional.of(adocao));
        when(adocaoRepository.save(any(Adocao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pracaRepository.save(any(Praca.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateStatus(1L, StatusAdocao.EM_ANALISE);
        assertEquals(StatusPraca.EM_PROCESSO, praca.getStatus());

        service.updateStatus(1L, StatusAdocao.APROVADA);
        assertEquals(StatusPraca.ADOTADA, praca.getStatus());
    }

    @Test
    void rejectsMissingAdoptionWhenUpdatingStatus() {
        when(adocaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateStatus(99L, StatusAdocao.APROVADA));
    }
}
