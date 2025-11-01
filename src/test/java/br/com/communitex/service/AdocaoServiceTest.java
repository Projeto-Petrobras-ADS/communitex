package br.com.communitex.service;

import br.senai.sc.communitex.dto.AdocaoRequestDTO;
import br.senai.sc.communitex.dto.AdocaoResponseDTO;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.exception.InvalidAdocaoException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.repository.AdocaoRepository;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import br.senai.sc.communitex.service.AdocaoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdocaoServiceTest {

    @Mock
    private AdocaoRepository adocaoRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private PracaRepository pracaRepository;

    @InjectMocks
    private AdocaoService adocaoService;

    // ✅ Criação de adoção bem-sucedida
    @Test
    void createAdocaoSuccess() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);

        Praca praca = new Praca();
        praca.setId(2L);
        praca.setStatus(StatusPraca.DISPONIVEL);

        AdocaoRequestDTO request = new AdocaoRequestDTO(
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                "Projeto Verde Sustentável",
                StatusAdocao.APROVADA,
                empresa,
                praca
        );

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(pracaRepository.findById(2L)).thenReturn(Optional.of(praca));
        when(adocaoRepository.save(any(Adocao.class))).thenAnswer(invocation -> {
            Adocao a = invocation.getArgument(0);
            a.setId(10L);
            return a;
        });

        AdocaoResponseDTO response = adocaoService.create(request);

        assertNotNull(response);
        assertEquals("Projeto Verde Sustentável", response.descricaoProjeto());
        assertEquals(StatusAdocao.APROVADA, response.status());
        verify(adocaoRepository, times(1)).save(any(Adocao.class));
        verify(pracaRepository, times(1)).save(any(Praca.class));
    }

    // ❌ Tentativa de criar adoção com praça já adotada
    @Test
    void createAdocaoWithPracaAlreadyAdoptedThrowsException() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);

        Praca praca = new Praca();
        praca.setId(2L);
        praca.setStatus(StatusPraca.ADOTADA);

        AdocaoRequestDTO request = new AdocaoRequestDTO(
                LocalDate.now(),
                LocalDate.now().plusDays(15),
                "Projeto Verde",
                StatusAdocao.PROPOSTA,
                empresa,
                praca
        );

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(pracaRepository.findById(2L)).thenReturn(Optional.of(praca));

        assertThrows(InvalidAdocaoException.class, () -> adocaoService.create(request));
        verify(adocaoRepository, never()).save(any());
    }

    // 🔍 Busca por ID existente
    @Test
    void findByIdSuccess() {
        Adocao adocao = new Adocao();
        adocao.setId(1L);
        adocao.setDescricaoProjeto("Praça Solar");

        when(adocaoRepository.findById(1L)).thenReturn(Optional.of(adocao));

        AdocaoResponseDTO response = adocaoService.findById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Praça Solar", response.descricaoProjeto());
    }

    // ❌ Busca por ID inexistente
    @Test
    void findByIdNotFoundThrowsException() {
        when(adocaoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adocaoService.findById(1L));
    }

    // 🧱 Atualização bem-sucedida
    @Test
    void updateAdocaoSuccess() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);

        Praca praca = new Praca();
        praca.setId(2L);

        Adocao existingAdocao = new Adocao();
        existingAdocao.setId(1L);

        AdocaoRequestDTO dto = new AdocaoRequestDTO(
                LocalDate.now(),
                LocalDate.now().plusDays(20),
                "Projeto Atualizado",
                StatusAdocao.EM_ANALISE,
                empresa,
                praca
        );

        when(adocaoRepository.findById(1L)).thenReturn(Optional.of(existingAdocao));
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(pracaRepository.findById(anyLong())).thenReturn(Optional.of(praca));
        when(adocaoRepository.save(any(Adocao.class))).thenReturn(existingAdocao);

        AdocaoResponseDTO response = adocaoService.update(1L, dto);

        assertNotNull(response);
        assertEquals("Projeto Atualizado", response.descricaoProjeto());
        verify(adocaoRepository, times(1)).save(any(Adocao.class));
    }

    // 🗑️ Exclusão bem-sucedida
    @Test
    void deleteAdocaoSuccess() {
        when(adocaoRepository.existsById(1L)).thenReturn(true);
        adocaoService.delete(1L);
        verify(adocaoRepository, times(1)).deleteById(1L);
    }

    // ❌ Exclusão de ID inexistente
    @Test
    void deleteAdocaoNotFoundThrowsException() {
        when(adocaoRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> adocaoService.delete(1L));
        verify(adocaoRepository, never()).deleteById(anyLong());
    }

    // ✅ Finalização de adoção
    @Test
    void finalizeAdoptionSuccess() {
        Praca praca = new Praca();
        praca.setStatus(StatusPraca.ADOTADA);

        Adocao adocao = new Adocao();
        adocao.setId(1L);
        adocao.setPraca(praca);

        when(adocaoRepository.findById(1L)).thenReturn(Optional.of(adocao));
        when(adocaoRepository.save(any(Adocao.class))).thenReturn(adocao);

        AdocaoResponseDTO response = adocaoService.finalizeAdoption(1L);

        assertEquals(StatusAdocao.FINALIZADA, response.status());
        assertEquals(StatusPraca.DISPONIVEL, praca.getStatus());
        verify(adocaoRepository, times(1)).save(adocao);
    }
}
