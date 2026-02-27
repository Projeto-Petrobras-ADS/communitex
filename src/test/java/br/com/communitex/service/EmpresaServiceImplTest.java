package br.com.communitex.service;

import br.senai.sc.communitex.dto.EmpresaRequestDTO;
import br.senai.sc.communitex.dto.EmpresaResponseDTO;
import br.senai.sc.communitex.exception.BusinessException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.RepresentanteEmpresa;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.service.impl.EmpresaServiceImpl;
import br.senai.sc.communitex.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        MockitoAnnotations.openMocks(this);

        empresa = new Empresa();
        empresa.setId(1L);
        empresa.setRazaoSocial("Tech Soluções LTDA");
        empresa.setCnpj("12345678000199");
        empresa.setNomeFantasia("Tech Soluções");
        empresa.setEmail("contato@tech.com");
        empresa.setTelefone("48999999999");

        RepresentanteEmpresa representante = new RepresentanteEmpresa();
        representante.setId(1L);
        representante.setNome("João Representante");
        representante.setAtivo(true);

        requestDTO = new EmpresaRequestDTO(
                "Tech Soluções LTDA",
                "12.345.678/0001-99",
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
    void deveRetornarListaDeEmpresas() {
        when(empresaRepository.findAll()).thenReturn(List.of(empresa));

        List<EmpresaResponseDTO> result = empresaService.findAll();

        assertEquals(1, result.size());
        assertEquals("Tech Soluções LTDA", result.get(0).nomeSocial());
        verify(empresaRepository, times(1)).findAll();
    }

    @Test
    void deveRetornarEmpresaPorId() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        EmpresaResponseDTO response = empresaService.findById(1L);

        assertEquals("Tech Soluções", response.nomeFantasia());
        verify(empresaRepository, times(1)).findById(1L);
    }

    @Test
    void deveLancarExcecaoQuandoEmpresaNaoEncontradaPorId() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> empresaService.findById(99L));
        verify(empresaRepository, times(1)).findById(99L);
    }

    @Test
    void deveCriarNovaEmpresa() {
        when(empresaRepository.findByCnpj(anyString())).thenReturn(Optional.empty());
        when(usuarioService.findByUsername(anyString())).thenReturn(Optional.empty());

        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setUsername("joao@tech.com");
        usuarioMock.setRole("ROLE_EMPRESA");
        usuarioMock.setNome("João Representante");

        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(usuarioService.save(any(Usuario.class))).thenReturn(usuarioMock);
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

        EmpresaResponseDTO response = empresaService.create(requestDTO);

        assertEquals("Tech Soluções", response.nomeFantasia());
        assertEquals("12345678000199", response.cnpj());
        verify(empresaRepository, times(1)).save(any(Empresa.class));
        verify(usuarioService, times(1)).save(any(Usuario.class));
    }

    @Test
    void deveLancarExcecaoAoCriarEmpresaComCnpjExistente() {
        when(empresaRepository.findByCnpj(anyString())).thenReturn(Optional.of(empresa));

        assertThrows(BusinessException.class, () -> empresaService.create(requestDTO));
        verify(empresaRepository, never()).save(any(Empresa.class));
    }

    @Test
    void deveAtualizarEmpresaExistente() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

        EmpresaResponseDTO response = empresaService.update(1L, requestDTO);

        assertEquals("Tech Soluções", response.nomeFantasia());
        verify(empresaRepository, times(1)).save(any(Empresa.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarEmpresaInexistente() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> empresaService.update(99L, requestDTO));
    }

    @Test
    void deveDeletarEmpresaExistente() {
        when(empresaRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> empresaService.delete(1L));
        verify(empresaRepository, times(1)).existsById(1L);
    }

    @Test
    void deveLancarExcecaoAoDeletarEmpresaInexistente() {
        when(empresaRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> empresaService.delete(99L));
        verify(empresaRepository, times(1)).existsById(99L);
    }
}
