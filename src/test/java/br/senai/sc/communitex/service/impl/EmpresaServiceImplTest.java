package br.senai.sc.communitex.service.impl;

import br.senai.sc.communitex.dto.EmpresaRequestDTO;
import br.senai.sc.communitex.dto.EmpresaResponseDTO;
import br.senai.sc.communitex.exception.BusinessException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.RepresentanteEmpresa;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceImplTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmpresaServiceImpl empresaService;

    private Empresa empresa;
    private EmpresaRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);
        empresa.setRazaoSocial("Tech Soluções LTDA");
        empresa.setCnpj("11222333000181");
        empresa.setNomeFantasia("Tech Soluções");
        empresa.setEmail("contato@tech.com");
        empresa.setTelefone("48999999999");
        empresa.setUsuarioRepresentante(Usuario.builder()
                .id(10L).username("joao@tech.com").password("hash").role("ROLE_EMPRESA").build());

        RepresentanteEmpresa representante = new RepresentanteEmpresa();
        representante.setId(1L);
        representante.setNome("João Representante");
        representante.setAtivo(true);

        requestDTO = new EmpresaRequestDTO(
                "Tech Soluções LTDA",
                "11.222.333/0001-81",
                "Tech Soluções",
                "contato@tech.com",
                "(48) 99999-9999",
                representante,
                "João Representante",
                "joao@tech.com",
                "senha123"
        );
    }

    @Test
    void givenEmpresasCadastradas_whenFindAll_thenRetornaLista() {
        when(empresaRepository.findAll()).thenReturn(List.of(empresa));

        List<EmpresaResponseDTO> result = empresaService.listarTodas();

        assertEquals(1, result.size());
        assertEquals("Tech Soluções LTDA", result.get(0).razaoSocial());
        verify(empresaRepository, times(1)).findAll();
    }

    @Test
    void givenEmpresaExistente_whenFindById_thenRetornaEmpresa() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        EmpresaResponseDTO response = empresaService.buscarPorId(1L);

        assertEquals("Tech Soluções", response.nomeFantasia());
        verify(empresaRepository, times(1)).findById(1L);
    }

    @Test
    void givenEmpresaInexistente_whenFindById_thenLancaResourceNotFoundException() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> empresaService.buscarPorId(99L));
        verify(empresaRepository, times(1)).findById(99L);
    }

    @Test
    void givenDadosValidos_whenCreate_thenCriaEmpresa() {
        when(empresaRepository.buscarPorCnpj(anyString())).thenReturn(Optional.empty());
        when(usuarioService.findByUsername(anyString())).thenReturn(Optional.empty());

        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setUsername("joao@tech.com");
        usuarioMock.setRole("ROLE_EMPRESA");
        usuarioMock.setNome("João Representante");

        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(usuarioService.save(any(Usuario.class))).thenReturn(usuarioMock);
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

        EmpresaResponseDTO response = empresaService.criar(requestDTO);

        assertEquals("Tech Soluções", response.nomeFantasia());
        assertEquals("11222333000181", response.cnpj());
        verify(empresaRepository, times(1)).save(any(Empresa.class));
        verify(usuarioService, times(1)).save(any(Usuario.class));
    }

    @Test
    void givenCnpjExistente_whenCreate_thenLancaBusinessException() {
        when(empresaRepository.buscarPorCnpj(anyString())).thenReturn(Optional.of(empresa));

        assertThrows(BusinessException.class, () -> empresaService.criar(requestDTO));
        verify(empresaRepository, never()).save(any(Empresa.class));
    }

    @Test
    void givenEmailRepresentanteExistente_whenCreate_thenLancaBusinessException() {
        when(empresaRepository.buscarPorCnpj(anyString())).thenReturn(Optional.empty());
        when(usuarioService.findByUsername(anyString())).thenReturn(Optional.of(new Usuario()));

        assertThrows(BusinessException.class, () -> empresaService.criar(requestDTO));
        verify(empresaRepository, never()).save(any(Empresa.class));
    }

    @Test
    void givenEmpresaExistente_whenUpdate_thenAtualizaEmpresa() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(passwordEncoder.encode("senha123")).thenReturn("nova-senha");
        when(usuarioService.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

        EmpresaResponseDTO response = empresaService.atualizar(1L, requestDTO);

        assertEquals("Tech Soluções", response.nomeFantasia());
        assertEquals("joao@tech.com", empresa.getUsuarioRepresentante().getUsername());
        assertEquals("nova-senha", empresa.getUsuarioRepresentante().getPassword());
        verify(empresaRepository, times(1)).save(any(Empresa.class));
    }

    @Test
    void givenEmpresaInexistente_whenUpdate_thenLancaResourceNotFoundException() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> empresaService.atualizar(99L, requestDTO));
    }

    @Test
    void givenEmpresaExistente_whenDelete_thenRemoveEmpresa() {
        when(empresaRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> empresaService.excluir(1L));
        verify(empresaRepository, times(1)).deleteById(1L);
    }

    @Test
    void givenEmpresaInexistente_whenDelete_thenLancaResourceNotFoundException() {
        when(empresaRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> empresaService.excluir(99L));
        verify(empresaRepository, times(1)).existsById(99L);
    }

    @Test
    void givenEmpresaExistente_whenFindEntityById_thenRetornaEntity() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        Empresa result = empresaService.buscarEntidadePorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Tech Soluções LTDA", result.getRazaoSocial());
    }

    @Test
    void givenEmpresaInexistente_whenFindEntityById_thenLancaResourceNotFoundException() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> empresaService.buscarEntidadePorId(99L));
    }
}
