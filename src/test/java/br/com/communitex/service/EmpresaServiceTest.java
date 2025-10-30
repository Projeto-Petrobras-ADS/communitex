package br.com.communitex.service;

import br.senai.sc.communitex.dto.EmpresaRequestDTO;
import br.senai.sc.communitex.dto.EmpresaResponseDTO;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.RepresentanteEmpresa;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.service.EmpresaService;
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
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private EmpresaService empresaService;

    @Test
    void createEmpresaSuccess() {

        RepresentanteEmpresa representante = new RepresentanteEmpresa();
        representante.setId(1L);
        representante.setNome("João da Silva");
        representante.setEmail("joao@empresa.com");
        representante.setAtivo(true);


        EmpresaRequestDTO requestDTO = new EmpresaRequestDTO(
                "Empresa Teste Ltda",
                "12.345.678/0001-90",
                "Empresa Teste",
                "empresaTeste@teste.com",
                "48999999999",
                representante


        );

        Empresa savedEmpresa = new Empresa();
        savedEmpresa.setId(1L);
        savedEmpresa.setRazaoSocial(requestDTO.razaoSocial());
        savedEmpresa.setCnpj(requestDTO.cnpj());
        savedEmpresa.setEmail(requestDTO.email());
        savedEmpresa.setTelefone(requestDTO.telefone());

        when(empresaRepository.save(any(Empresa.class))).thenReturn(savedEmpresa);

        EmpresaResponseDTO response = empresaService.create(requestDTO);

        assertNotNull(response);
        assertEquals(savedEmpresa.getId(), response.id());
        assertEquals(requestDTO.razaoSocial(), response.nomeSocial());
    }

    @Test
    void findByIdSuccess() {
        Long id = 1L;
        Empresa empresa = new Empresa();
        empresa.setId(id);
        empresa.setRazaoSocial("Empresa Teste");

        when(empresaRepository.findById(id)).thenReturn(Optional.of(empresa));

        EmpresaResponseDTO response = empresaService.findById(id);

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals("Empresa Teste", response.nomeSocial());
    }

    @Test
    void findByIdNotFound() {
        Long id = 1L;
        when(empresaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> empresaService.findById(id));
    }

    @Test
    void updateEmpresaSuccess() {
        Long id = 1L;
        EmpresaRequestDTO requestDTO = new EmpresaRequestDTO(
                "Empresa Atualizada Ltda",
                "12.345.678/0001-90",
                "novo@empresa.com",
                "(11) 98888-7777"
        );

        Empresa existingEmpresa = new Empresa();
        existingEmpresa.setId(id);

        when(empresaRepository.findById(id)).thenReturn(Optional.of(existingEmpresa));
        when(empresaRepository.save(any(Empresa.class))).thenReturn(existingEmpresa);

        EmpresaResponseDTO response = empresaService.update(id, requestDTO);

        assertNotNull(response);
        assertEquals(requestDTO.razaoSocial(), response.nomeSocial());
    }

    @Test
    void deleteEmpresaSuccess() {
        Long id = 1L;
        when(empresaRepository.existsById(id)).thenReturn(true);

        empresaService.delete(id);

        verify(empresaRepository, times(1)).existsById(id);
    }

    @Test
    void deleteEmpresaNotFound() {
        Long id = 1L;
        when(empresaRepository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> empresaService.delete(id));
        verify(empresaRepository, never()).deleteById(any());
    }
}
