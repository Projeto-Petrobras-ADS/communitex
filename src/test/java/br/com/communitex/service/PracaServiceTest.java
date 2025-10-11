package br.com.communitex.service;

import br.senai.sc.communitex.dto.PracaRequestDTO;
import br.senai.sc.communitex.dto.PracaResponseDTO;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Praca;
import br.senai.sc.communitex.repository.PracaRepository;
import br.senai.sc.communitex.service.PracaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PracaServiceTest {

    @Mock
    private PracaRepository pracaRepository;

    @InjectMocks
    private PracaService pracaService;

    @Test
    void createPracaSuccess() {
        // Arrange
        PracaRequestDTO requestDTO = new PracaRequestDTO(
            "Praça Teste", "Rua Teste", "Bairro Teste",
            "Cidade Teste", -23.123, -46.123,
            "Descrição teste", "http://foto.jpg", StatusPraca.DISPONIVEL
        );

        Praca savedPraca = new Praca();
        savedPraca.setId(1L);
        savedPraca.setNome(requestDTO.nome());
        // ... set other properties

        when(pracaRepository.save(any(Praca.class))).thenReturn(savedPraca);

        // Act
        PracaResponseDTO response = pracaService.create(requestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(savedPraca.getId(), response.id());
        assertEquals(requestDTO.nome(), response.nome());
    }

    @Test
    void findByIdSuccess() {
        // Arrange
        Long id = 1L;
        Praca praca = new Praca();
        praca.setId(id);
        praca.setNome("Praça Teste");

        when(pracaRepository.findById(id)).thenReturn(Optional.of(praca));

        // Act
        PracaResponseDTO response = pracaService.findById(id);

        // Assert
        assertNotNull(response);
        assertEquals(id, response.id());
    }

    @Test
    void findByIdNotFound() {
        // Arrange
        Long id = 1L;
        when(pracaRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> pracaService.findById(id));
    }

    @Test
    void updatePracaSuccess() {
        // Arrange
        Long id = 1L;
        PracaRequestDTO requestDTO = new PracaRequestDTO(
            "Praça Atualizada", "Rua Nova", "Bairro Novo",
            "Cidade Nova", -23.123, -46.123,
            "Nova descrição", "http://nova-foto.jpg", StatusPraca.ADOTADA
        );

        Praca existingPraca = new Praca();
        existingPraca.setId(id);

        when(pracaRepository.findById(id)).thenReturn(Optional.of(existingPraca));
        when(pracaRepository.save(any(Praca.class))).thenReturn(existingPraca);

        // Act
        PracaResponseDTO response = pracaService.update(id, requestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(requestDTO.nome(), response.nome());
    }

    @Test
    void deletePracaSuccess() {
        // Arrange
        Long id = 1L;
        when(pracaRepository.existsById(id)).thenReturn(true);

        // Act
        pracaService.delete(id);

        // Assert
        verify(pracaRepository, times(1)).deleteById(id);
    }

    @Test
    void deletePracaNotFound() {
        // Arrange
        Long id = 1L;
        when(pracaRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> pracaService.delete(id));
        verify(pracaRepository, never()).deleteById(any());
    }
}

