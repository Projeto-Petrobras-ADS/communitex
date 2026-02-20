package br.com.communitex.service;

import br.senai.sc.communitex.dto.PracaRequestDTO;
import br.senai.sc.communitex.dto.PracaResponseDTO;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.repository.PracaRepository;
import br.senai.sc.communitex.service.PessoaFisicaService;
import br.senai.sc.communitex.service.impl.PracaServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PracaServiceImplTest {

    @Mock
    private PracaRepository pracaRepository;

    @Mock
    private PessoaFisicaService pessoaFisicaService;

    @InjectMocks
    private PracaServiceImpl pracaServiceImpl;

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
    void createPracaSuccess() {
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

        PracaResponseDTO response = pracaServiceImpl.create(requestDTO);

        assertNotNull(response);
        assertEquals(savedPraca.getId(), response.id());
        assertEquals(requestDTO.nome(), response.nome());
    }

    @Test
    void findByIdSuccess() {
        Long id = 1L;
        Praca praca = new Praca();
        praca.setId(id);
        praca.setNome("Praça Teste");

        when(pracaRepository.findById(id)).thenReturn(Optional.of(praca));

        PracaResponseDTO response = pracaServiceImpl.findById(id);

        assertNotNull(response);
        assertEquals(id, response.id());
    }

    @Test
    void findByIdNotFound() {
        Long id = 1L;
        when(pracaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pracaServiceImpl.findById(id));
    }

    @Test
    void updatePracaSuccess() {
        Long id = 1L;
        PracaRequestDTO requestDTO = new PracaRequestDTO(
            "Praça Atualizada", "Rua Nova", "Bairro Novo",
            "Cidade Nova", -23.123, -46.123,
            "Nova descrição", "http://nova-foto.jpg", 3000.0, StatusPraca.ADOTADA
        );

        Praca existingPraca = new Praca();
        existingPraca.setId(id);
        existingPraca.setNome(requestDTO.nome());

        when(pracaRepository.findById(id)).thenReturn(Optional.of(existingPraca));
        when(pracaRepository.save(any(Praca.class))).thenReturn(existingPraca);

        PracaResponseDTO response = pracaServiceImpl.update(id, requestDTO);

        assertNotNull(response);
        assertEquals(requestDTO.nome(), response.nome());
    }

    @Test
    void deletePracaSuccess() {
        Long id = 1L;
        when(pracaRepository.existsById(id)).thenReturn(true);

        pracaServiceImpl.delete(id);

        verify(pracaRepository, times(1)).deleteById(id);
    }

    @Test
    void deletePracaNotFound() {
        Long id = 1L;
        when(pracaRepository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> pracaServiceImpl.delete(id));
        verify(pracaRepository, never()).deleteById(any());
    }
}
