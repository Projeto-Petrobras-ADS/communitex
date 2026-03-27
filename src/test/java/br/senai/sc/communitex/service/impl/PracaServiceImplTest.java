package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.PracaRequestDTO;
import br.senai.sc.communitex.dto.PracaResponseDTO;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.repository.PracaRepository;
import br.senai.sc.communitex.service.PessoaFisicaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PracaServiceImplTest {

    @Mock
    private PracaRepository pracaRepository;

    @Mock
    private PessoaFisicaService pessoaFisicaService;

    @InjectMocks
    private PracaServiceImpl pracaService;

    @BeforeEach
    void setUp() {
        // Setup SecurityContext mock for authenticated user
        var authentication = new UsernamePasswordAuthenticationToken("testuser", "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenUsuarioAutenticado_whenCreate_thenCriaPraca() {
        PracaRequestDTO requestDTO = new PracaRequestDTO(
            "Praça Teste", "Rua Teste", "Bairro Teste",
            "Cidade Teste", -23.123, -46.123,
            "Descrição teste", "http://foto.jpg", 2500.0, StatusPraca.DISPONIVEL
        );

        PessoaFisica pessoaFisica = new PessoaFisica();
        pessoaFisica.setId(1L);
        pessoaFisica.setNome("Test User");

        Praca savedPraca = new Praca();
        savedPraca.setId(1L);
        savedPraca.setNome(requestDTO.nome());
        savedPraca.setMetragemM2(requestDTO.metragemM2());
        savedPraca.setCadastradoPor(pessoaFisica);

        when(pessoaFisicaService.findByUsuarioUsername("testuser")).thenReturn(pessoaFisica);
        when(pracaRepository.save(any(Praca.class))).thenReturn(savedPraca);

        PracaResponseDTO response = pracaService.create(requestDTO);

        assertNotNull(response);
        assertEquals(savedPraca.getId(), response.id());
        assertEquals(requestDTO.nome(), response.nome());
        verify(pracaRepository, times(1)).save(any(Praca.class));
    }

    @Test
    void givenUsuarioNaoAutenticado_whenCreate_thenLancaForbiddenException() {
        SecurityContextHolder.clearContext();
        PracaRequestDTO requestDTO = new PracaRequestDTO(
            "Praça Teste", "Rua Teste", "Bairro Teste",
            "Cidade Teste", -23.123, -46.123,
            "Descrição teste", "http://foto.jpg", 2500.0, StatusPraca.DISPONIVEL
        );

        assertThrows(Exception.class, () -> pracaService.create(requestDTO));
    }

    @Test
    void givenPracaExistente_whenFindById_thenRetornaPraca() {
        Long id = 1L;
        Praca praca = new Praca();
        praca.setId(id);
        praca.setNome("Praça Teste");

        when(pracaRepository.findById(id)).thenReturn(Optional.of(praca));

        PracaResponseDTO response = pracaService.findById(id);

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals("Praça Teste", response.nome());
    }

    @Test
    void givenPracaInexistente_whenFindById_thenLancaResourceNotFoundException() {
        Long id = 1L;
        when(pracaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pracaService.findById(id));
        verify(pracaRepository, times(1)).findById(id);
    }

    @Test
    void givenPracaComDetalhes_whenFindByIdWithDetails_thenRetornaPracaComDetalhes() {
        Long id = 1L;
        Praca praca = new Praca();
        praca.setId(id);
        praca.setNome("Praça Detalhada");
        praca.setDescricao("Uma praça com detalhes");

        when(pracaRepository.findById(id)).thenReturn(Optional.of(praca));

        var response = pracaService.findByIdWithDetails(id);

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals("Praça Detalhada", response.nome());
    }

    @Test
    void givenPracasDisponiveis_whenFindAll_thenRetornaListaDePracas() {
        Praca praca1 = new Praca();
        praca1.setId(1L);
        praca1.setNome("Praça 1");

        Praca praca2 = new Praca();
        praca2.setId(2L);
        praca2.setNome("Praça 2");

        when(pracaRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class))).thenReturn(List.of(praca1, praca2));

        var pesquisaDTO = new br.senai.sc.communitex.dto.PracaPesquisaDTO(
                null, null, null
        );
        var response = pracaService.findAll(pesquisaDTO);

        assertNotNull(response);
        assertEquals(2, response.size());
    }

    @Test
    void givenPracaExistente_whenUpdate_thenAtualizaPraca() {
        Long id = 1L;
        PracaRequestDTO requestDTO = new PracaRequestDTO(
            "Praça Atualizada", "Rua Nova", "Bairro Novo",
            "Cidade Nova", -23.123, -46.123,
            "Nova descrição", "http://nova-foto.jpg", 3000.0, StatusPraca.ADOTADA
        );

        Praca existingPraca = new Praca();
        existingPraca.setId(id);
        existingPraca.setNome("Praça Antiga");

        when(pracaRepository.findById(id)).thenReturn(Optional.of(existingPraca));
        when(pracaRepository.save(any(Praca.class))).thenReturn(existingPraca);

        PracaResponseDTO response = pracaService.update(id, requestDTO);

        assertNotNull(response);
        verify(pracaRepository, times(1)).save(any(Praca.class));
    }

    @Test
    void givenPracaInexistente_whenUpdate_thenLancaResourceNotFoundException() {
        Long id = 1L;
        PracaRequestDTO requestDTO = new PracaRequestDTO(
            "Praça Teste", "Rua Teste", "Bairro Teste",
            "Cidade Teste", -23.123, -46.123,
            "Descrição teste", "http://foto.jpg", 2500.0, StatusPraca.DISPONIVEL
        );

        when(pracaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pracaService.update(id, requestDTO));
    }

    @Test
    void givenPracaExistente_whenDelete_thenRemovePraca() {
        Long id = 1L;
        when(pracaRepository.existsById(id)).thenReturn(true);

        assertDoesNotThrow(() -> pracaService.delete(id));
        verify(pracaRepository, times(1)).deleteById(id);
    }

    @Test
    void givenPracaInexistente_whenDelete_thenLancaResourceNotFoundException() {
        Long id = 1L;
        when(pracaRepository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> pracaService.delete(id));
        verify(pracaRepository, never()).deleteById(any());
    }
}


