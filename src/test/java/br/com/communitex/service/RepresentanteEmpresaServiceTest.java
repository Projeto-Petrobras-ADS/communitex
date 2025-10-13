package br.com.communitex.service;

import br.senai.sc.communitex.dto.RepresentanteEmpresaRequestDTO;
import br.senai.sc.communitex.dto.RepresentanteEmpresaResponseDTO;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.RepresentanteEmpresa;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.RepresentanteEmpresaRepository;
import br.senai.sc.communitex.service.RepresentanteEmpresaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepresentanteEmpresaServiceTest {

    @Mock
    private RepresentanteEmpresaRepository representanteRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private RepresentanteEmpresaService representanteService;

    @Test
    void createRepresentanteSuccess() {
        Empresa empresa = new Empresa(1L, "Empresa Teste", "00.000.000/0000-00",
                "Fantasia", "email@teste.com", "(11) 99999-9999");

        RepresentanteEmpresaRequestDTO requestDTO =
                new RepresentanteEmpresaRequestDTO("João Silva", true, empresa.getId());

        when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));

        RepresentanteEmpresa saved = new RepresentanteEmpresa(1L, "João Silva", true, empresa);
        when(representanteRepository.save(any(RepresentanteEmpresa.class))).thenReturn(saved);

        RepresentanteEmpresaResponseDTO response = representanteService.create(requestDTO);

        assertNotNull(response);
        assertEquals("João Silva", response.nome());
        assertTrue(response.ativo());
        assertEquals(empresa.getId(), response.empresaId());
    }

    @Test
    void createRepresentanteEmpresaNotFound() {
        Long empresaId = 99L;
        RepresentanteEmpresaRequestDTO dto =
                new RepresentanteEmpresaRequestDTO("João Silva", true, empresaId);

        when(empresaRepository.findById(empresaId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> representanteService.create(dto));
    }

    @Test
    void findByIdSuccess() {
        Empresa empresa = new Empresa(1L, "Empresa Teste", "00.000.000/0000-00",
                "Fantasia", "email@teste.com", "(11) 99999-9999");
        RepresentanteEmpresa representante = new RepresentanteEmpresa(1L, "Maria Souza", true, empresa);

        when(representanteRepository.findById(1L)).thenReturn(Optional.of(representante));

        RepresentanteEmpresaResponseDTO response = representanteService.findById(1L);

        assertNotNull(response);
        assertEquals("Maria Souza", response.nome());
        assertEquals(empresa.getId(), response.empresaId());
    }

    @Test
    void findByIdNotFound() {
        when(representanteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> representanteService.findById(1L));
    }

    @Test
    void updateRepresentanteSuccess() {
        Empresa empresa = new Empresa(1L, "Empresa Teste", "00.000.000/0000-00",
                "Fantasia", "email@teste.com", "(11) 99999-9999");

        RepresentanteEmpresa existing = new RepresentanteEmpresa(1L, "José", false, empresa);
        RepresentanteEmpresaRequestDTO dto =
                new RepresentanteEmpresaRequestDTO("José Atualizado", true, empresa.getId());

        when(representanteRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
        when(representanteRepository.save(any(RepresentanteEmpresa.class))).thenReturn(existing);

        RepresentanteEmpresaResponseDTO response = representanteService.update(1L, dto);


        assertNotNull(response);
        assertEquals("José Atualizado", response.nome());
        assertTrue(response.ativo());
    }

    @Test
    void updateRepresentanteNotFound() {
        RepresentanteEmpresaRequestDTO dto =
                new RepresentanteEmpresaRequestDTO("Fulano", true, 1L);

        when(representanteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> representanteService.update(1L, dto));
    }

    @Test
    void deleteRepresentanteSuccess() {

        when(representanteRepository.existsById(1L)).thenReturn(true);


        representanteService.delete(1L);


        verify(representanteRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteRepresentanteNotFound() {

        when(representanteRepository.existsById(1L)).thenReturn(false);


        assertThrows(ResourceNotFoundException.class, () -> representanteService.delete(1L));
        verify(representanteRepository, never()).deleteById(any());
    }

    @Test
    void findAllSuccess() {

        Empresa empresa = new Empresa(1L, "Empresa Teste", "00.000.000/0000-00",
                "Fantasia", "email@teste.com", "(11) 99999-9999");
        RepresentanteEmpresa rep1 = new RepresentanteEmpresa(1L, "João", true, empresa);
        RepresentanteEmpresa rep2 = new RepresentanteEmpresa(2L, "Maria", false, empresa);

        when(representanteRepository.findAll()).thenReturn(List.of(rep1, rep2));


        List<RepresentanteEmpresaResponseDTO> responseList = representanteService.findAll();


        assertEquals(2, responseList.size());
        assertEquals("João", responseList.get(0).nome());
        assertEquals("Maria", responseList.get(1).nome());
    }
}
