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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepresentanteEmpresaServiceTest {

    @Mock
    private RepresentanteEmpresaRepository representanteRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private RepresentanteEmpresaService representanteService;

    // ✅ CREATE - sucesso
    @Test
    void createRepresentanteSuccess() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNomeFantasia("Tech Soluções");

        RepresentanteEmpresaRequestDTO dto = new RepresentanteEmpresaRequestDTO(
                "João da Silva",
                true,
                "joao@tech.com",
                1L
        );

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(representanteRepository.save(any(RepresentanteEmpresa.class))).thenAnswer(invocation -> {
            RepresentanteEmpresa rep = invocation.getArgument(0);
            rep.setId(10L);
            return rep;
        });

        RepresentanteEmpresaResponseDTO response = representanteService.create(dto);

        assertNotNull(response);
        assertEquals("João da Silva", response.nome());
        assertEquals("Tech Soluções", response.empresaNomeFantasia());
        verify(representanteRepository, times(1)).save(any(RepresentanteEmpresa.class));
    }

    // ❌ CREATE - empresa não encontrada
    @Test
    void createRepresentanteEmpresaNotFoundThrowsException() {
        RepresentanteEmpresaRequestDTO dto = new RepresentanteEmpresaRequestDTO(
                "João da Silva",
                true,
                "joao@tech.com",
                99L
        );

        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> representanteService.create(dto));
        verify(representanteRepository, never()).save(any());
    }

    // ✅ FIND ALL
    @Test
    void findAllSuccess() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNomeFantasia("Tech Soluções");

        RepresentanteEmpresa rep = new RepresentanteEmpresa();
        rep.setId(1L);
        rep.setNome("João");
        rep.setAtivo(true);
        rep.setEmail("joao@tech.com");
        rep.setEmpresa(empresa);

        when(representanteRepository.findAll()).thenReturn(List.of(rep));

        List<RepresentanteEmpresaResponseDTO> result = representanteService.findAll();

        assertEquals(1, result.size());
        assertEquals("João", result.get(0).nome());
        verify(representanteRepository, times(1)).findAll();
    }

    // ✅ FIND BY ID - sucesso
    @Test
    void findByIdSuccess() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNomeFantasia("Tech Soluções");

        RepresentanteEmpresa rep = new RepresentanteEmpresa();
        rep.setId(1L);
        rep.setNome("João");
        rep.setAtivo(true);
        rep.setEmail("joao@tech.com");
        rep.setEmpresa(empresa);

        when(representanteRepository.findById(1L)).thenReturn(Optional.of(rep));

        RepresentanteEmpresaResponseDTO response = representanteService.findById(1L);

        assertEquals("João", response.nome());
        assertEquals("Tech Soluções", response.empresaNomeFantasia());
        verify(representanteRepository, times(1)).findById(1L);
    }

    // ❌ FIND BY ID - não encontrado
    @Test
    void findByIdNotFoundThrowsException() {
        when(representanteRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> representanteService.findById(1L));
    }

    // ✅ UPDATE - sucesso
    @Test
    void updateRepresentanteSuccess() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNomeFantasia("Tech Soluções");

        RepresentanteEmpresa existing = new RepresentanteEmpresa();
        existing.setId(1L);
        existing.setNome("Antigo");
        existing.setAtivo(false);
        existing.setEmail("antigo@old.com");
        existing.setEmpresa(empresa);

        RepresentanteEmpresaRequestDTO dto = new RepresentanteEmpresaRequestDTO(
                "Novo Nome",
                true,
                "novo@tech.com",
                1L
        );

        when(representanteRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(representanteRepository.save(any(RepresentanteEmpresa.class))).thenReturn(existing);

        RepresentanteEmpresaResponseDTO response = representanteService.update(1L, dto);

        assertEquals("Novo Nome", response.nome());
        assertTrue(response.ativo());
        assertEquals("novo@tech.com", response.email());
        verify(representanteRepository, times(1)).save(any(RepresentanteEmpresa.class));
    }

    // ❌ UPDATE - representante não encontrado
    @Test
    void updateRepresentanteNotFoundThrowsException() {
        RepresentanteEmpresaRequestDTO dto = new RepresentanteEmpresaRequestDTO(
                "Novo Nome",
                true,
                "novo@tech.com",
                1L
        );

        when(representanteRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> representanteService.update(1L, dto));
    }

    // ❌ UPDATE - empresa não encontrada
    @Test
    void updateEmpresaNotFoundThrowsException() {
        RepresentanteEmpresa existing = new RepresentanteEmpresa();
        existing.setId(1L);

        RepresentanteEmpresaRequestDTO dto = new RepresentanteEmpresaRequestDTO(
                "Novo Nome",
                true,
                "novo@tech.com",
                2L
        );

        when(representanteRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(empresaRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> representanteService.update(1L, dto));
    }

    // ✅ DELETE - sucesso
    @Test
    void deleteRepresentanteSuccess() {
        when(representanteRepository.existsById(1L)).thenReturn(true);
        representanteService.delete(1L);
        verify(representanteRepository, times(1)).deleteById(1L);
    }

    // ❌ DELETE - não encontrado
    @Test
    void deleteRepresentanteNotFoundThrowsException() {
        when(representanteRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> representanteService.delete(1L));
        verify(representanteRepository, never()).deleteById(anyLong());
    }
}
