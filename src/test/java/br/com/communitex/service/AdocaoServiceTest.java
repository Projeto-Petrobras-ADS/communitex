package br.com.communitex.service;

import br.senai.sc.communitex.dto.AdocaoRequestDTO;
import br.senai.sc.communitex.dto.AdocaoResponseDTO;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.repository.AdocaoRepository;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import br.senai.sc.communitex.service.AdocaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdocaoServiceTest {

    @Mock
    private AdocaoRepository adocaoRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private PracaRepository pracaRepository;

    @InjectMocks
    private AdocaoService adocaoService;

    private Empresa empresa;
    private Praca praca;
    private Adocao adocao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNomeFantasia("Tech Ltda");

        praca = new Praca();
        praca.setId(1L);
        praca.setNome("Praça Central");

        adocao = new Adocao();
        adocao.setId(1L);
        adocao.setDataInicio(LocalDate.now());
        adocao.setDataFim(LocalDate.now().plusMonths(1));
        adocao.setDescricaoProjeto("Projeto de revitalização da praça");
        adocao.setStatus(StatusAdocao.APROVADA);
        adocao.setEmpresa(empresa);
        adocao.setPraca(praca);
    }

    @Test
    void deveRetornarTodasAsAdocoes() {
        when(adocaoRepository.findAll()).thenReturn(List.of(adocao));

        List<AdocaoResponseDTO> result = adocaoService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).descricaoProjeto()).isEqualTo("Projeto de revitalização da praça");
        verify(adocaoRepository, times(1)).findAll();
    }

    @Test
    void deveCriarAdocaoComSucesso() {
        AdocaoRequestDTO dto = new AdocaoRequestDTO(
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                "Nova Adoção",
                StatusAdocao.PROPOSTA,
                empresa,
                praca
        );

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(pracaRepository.findById(1L)).thenReturn(Optional.of(praca));
        when(adocaoRepository.save(any(Adocao.class))).thenReturn(adocao);

        AdocaoResponseDTO result = adocaoService.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.empresa().getNomeFantasia()).isEqualTo("Tech Ltda");
        verify(adocaoRepository, times(1)).save(any(Adocao.class));
    }

    @Test
    void deveLancarExcecaoAoCriarAdocaoSemEmpresa() {
        AdocaoRequestDTO dto = new AdocaoRequestDTO(
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                "Nova Adoção",
                StatusAdocao.PROPOSTA,
                empresa,
                praca
        );

        when(empresaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adocaoService.create(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Empresa não encontrada");
    }

    @Test
    void deveRetornarAdocaoPorId() {
        when(adocaoRepository.findById(1L)).thenReturn(Optional.of(adocao));

        AdocaoResponseDTO result = adocaoService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.descricaoProjeto()).contains("revitalização");
        verify(adocaoRepository, times(1)).findById(1L);
    }

    @Test
    void deveAtualizarAdocaoComSucesso() {
        AdocaoRequestDTO dto = new AdocaoRequestDTO(
                LocalDate.now(),
                LocalDate.now().plusDays(15),
                "Projeto Atualizado",
                StatusAdocao.EM_ANALISE,
                empresa,
                praca
        );

        when(adocaoRepository.findById(1L)).thenReturn(Optional.of(adocao));
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(pracaRepository.findById(1L)).thenReturn(Optional.of(praca));
        when(adocaoRepository.save(any(Adocao.class))).thenReturn(adocao);

        AdocaoResponseDTO result = adocaoService.update(1L, dto);

        assertThat(result).isNotNull();
        assertThat(result.descricaoProjeto()).isEqualTo("Projeto Atualizado");
        verify(adocaoRepository, times(1)).save(any(Adocao.class));
    }

    @Test
    void deveExcluirAdocaoComSucesso() {
        when(adocaoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(adocaoRepository).deleteById(1L);

        adocaoService.delete(1L);

        verify(adocaoRepository, times(1)).deleteById(1L);
    }

    @Test
    void deveLancarExcecaoAoExcluirAdocaoInexistente() {
        when(adocaoRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> adocaoService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Praça não encontrado");
    }
}