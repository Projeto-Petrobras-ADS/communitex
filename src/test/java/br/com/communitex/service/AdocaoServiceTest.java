package br.com.communitex.service;

import br.senai.sc.communitex.dto.AdocaoRequestDTO;
import br.senai.sc.communitex.dto.AdocaoResponseDTO;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.exception.ForbiddenException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        // Novo DTO: último parâmetro é pracaId (Long)
        AdocaoRequestDTO request = new AdocaoRequestDTO(
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                "Projeto Verde Sustentável",
                StatusAdocao.APROVADA,
                2L
        );

        // mockar SecurityContext com um usuário associado à empresa
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("user_empresa");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(empresaRepository.findByUsuarioRepresentanteUsername("user_empresa")).thenReturn(Optional.of(empresa));
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

        SecurityContextHolder.clearContext();
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
                2L
        );

        // mockar SecurityContext com um usuário associado à empresa
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("user_empresa");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(empresaRepository.findByUsuarioRepresentanteUsername("user_empresa")).thenReturn(Optional.of(empresa));
        when(pracaRepository.findById(2L)).thenReturn(Optional.of(praca));

        assertThrows(InvalidAdocaoException.class, () -> adocaoService.create(request));
        verify(adocaoRepository, never()).save(any());

        SecurityContextHolder.clearContext();
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
        Praca praca = new Praca();
        praca.setId(2L);

        Adocao existingAdocao = new Adocao();
        existingAdocao.setId(1L);

        AdocaoRequestDTO dto = new AdocaoRequestDTO(
                LocalDate.now(),
                LocalDate.now().plusDays(20),
                "Projeto Atualizado",
                StatusAdocao.EM_ANALISE,
                2L
        );

        when(adocaoRepository.findById(1L)).thenReturn(Optional.of(existingAdocao));
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

    // 🔒 Usuário autenticado sem empresa deve receber 403 Forbidden
    @Test
    void createAdocao_UserWithoutEmpresa_ThrowsForbidden() {
        // preparar praca existente
        Praca praca = new Praca();
        praca.setId(5L);
        praca.setStatus(StatusPraca.DISPONIVEL);

        AdocaoRequestDTO request = new AdocaoRequestDTO(
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                "Projeto Sem Empresa",
                StatusAdocao.PROPOSTA,
                5L
        );

        // mockar repositorios
        when(pracaRepository.findById(5L)).thenReturn(Optional.of(praca));
        when(empresaRepository.findByUsuarioRepresentanteUsername("user_no_empresa")).thenReturn(Optional.empty());

        // mockar SecurityContext com um usuário sem empresa
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("user_no_empresa");
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            assertThrows(ForbiddenException.class, () -> adocaoService.create(request));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ✅ Buscar adoções pela empresa do usuário autenticado
    @Test
    void findByAuthenticatedUserEmpresa_Success() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNomeFantasia("Empresa Test");

        Praca praca = new Praca();
        praca.setId(1L);
        praca.setNome("Praça Test");

        Adocao adocao = new Adocao();
        adocao.setId(1L);
        adocao.setDescricaoProjeto("Projeto Test");
        adocao.setEmpresa(empresa);
        adocao.setPraca(praca);

        // mockar SecurityContext com um usuário associado à empresa
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("user_empresa");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(empresaRepository.findByUsuarioRepresentanteUsername("user_empresa")).thenReturn(Optional.of(empresa));
        when(adocaoRepository.findByEmpresaId(1L)).thenReturn(List.of(adocao));

        try {
            List<AdocaoResponseDTO> responses = adocaoService.findByAuthenticatedUserEmpresa();

            assertNotNull(responses);
            assertEquals(1, responses.size());
            assertEquals("Projeto Test", responses.get(0).descricaoProjeto());
            verify(adocaoRepository, times(1)).findByEmpresaId(1L);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ❌ Buscar adoções pela empresa quando usuário não está autenticado
    @Test
    void findByAuthenticatedUserEmpresa_UserNotAuthenticatedThrowsForbidden() {
        when(empresaRepository.findByUsuarioRepresentanteUsername("user_no_empresa")).thenReturn(Optional.empty());

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("user_no_empresa");
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            assertThrows(ForbiddenException.class, () -> adocaoService.findByAuthenticatedUserEmpresa());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
