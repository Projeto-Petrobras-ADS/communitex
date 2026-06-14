package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.AssumirAtendimentoRequestDTO;
import br.senai.sc.communitex.enums.AtendimentoDenunciaStatus;
import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.enums.IssueType;
import br.senai.sc.communitex.exception.BusinessException;
import br.senai.sc.communitex.model.AtendimentoDenuncia;
import br.senai.sc.communitex.model.Denuncia;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.AtendimentoDenunciaRepository;
import br.senai.sc.communitex.repository.DenunciaRepository;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtendimentoDenunciaServiceTest {

    @Mock
    private AtendimentoDenunciaRepository atendimentoRepository;
    @Mock
    private DenunciaRepository denunciaRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AtendimentoDenunciaService service;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void empresaAssumeEIniciaReparoSemResolverDenuncia() {
        var empresaUser = user(2L, "empresa@test.com", "ROLE_EMPRESA");
        var autor = user(1L, "autor@test.com", "ROLE_USER");
        var empresa = Empresa.builder().id(5L).razaoSocial("Empresa Teste").usuarioRepresentante(empresaUser).build();
        var denuncia = issue(10L, autor, IssueStatus.ABERTA);

        authenticate("empresa@test.com");
        when(empresaRepository.buscarPorUsuarioRepresentanteUsername("empresa@test.com")).thenReturn(Optional.of(empresa));
        when(denunciaRepository.findById(10L)).thenReturn(Optional.of(denuncia));
        when(atendimentoRepository.existsByDenunciaId(10L)).thenReturn(false);
        when(atendimentoRepository.save(any(AtendimentoDenuncia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var assumido = service.assumir(10L, new AssumirAtendimentoRequestDTO("Vamos corrigir o problema reportado"));
        when(atendimentoRepository.findByDenunciaId(10L)).thenReturn(Optional.of(AtendimentoDenuncia.builder()
                .id(20L).denuncia(denuncia).empresa(empresa).status(AtendimentoDenunciaStatus.ACEITO)
                .descricaoPlanejada("Vamos corrigir o problema reportado").build()));
        var iniciado = service.iniciar(10L);

        assertEquals(AtendimentoDenunciaStatus.ACEITO, assumido.status());
        assertEquals(AtendimentoDenunciaStatus.EM_ANDAMENTO, iniciado.status());
        assertEquals(IssueStatus.EM_ANDAMENTO, denuncia.getStatus());
    }

    @Test
    void somenteConfirmacaoDoAutorResolveDenuncia() {
        var empresaUser = user(2L, "empresa@test.com", "ROLE_EMPRESA");
        var autor = user(1L, "autor@test.com", "ROLE_USER");
        var empresa = Empresa.builder().id(5L).razaoSocial("Empresa Teste").usuarioRepresentante(empresaUser).build();
        var denuncia = issue(10L, autor, IssueStatus.AGUARDANDO_CONFIRMACAO);
        var atendimento = AtendimentoDenuncia.builder().id(20L).denuncia(denuncia).empresa(empresa)
                .status(AtendimentoDenunciaStatus.CONCLUIDO_PELA_EMPRESA).descricaoPlanejada("Plano").build();

        authenticate("autor@test.com");
        when(usuarioRepository.findByUsername("autor@test.com")).thenReturn(Optional.of(autor));
        when(atendimentoRepository.findByDenunciaId(10L)).thenReturn(Optional.of(atendimento));
        when(atendimentoRepository.save(any(AtendimentoDenuncia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.confirmar(10L);

        assertEquals(AtendimentoDenunciaStatus.CONFIRMADO_PELO_AUTOR, response.status());
        assertEquals(IssueStatus.RESOLVIDA, denuncia.getStatus());
    }

    @Test
    void naoPermiteAssumirDenunciaJaAtendida() {
        var empresaUser = user(2L, "empresa@test.com", "ROLE_EMPRESA");
        var empresa = Empresa.builder().id(5L).razaoSocial("Empresa Teste").usuarioRepresentante(empresaUser).build();
        authenticate("empresa@test.com");
        when(empresaRepository.buscarPorUsuarioRepresentanteUsername("empresa@test.com")).thenReturn(Optional.of(empresa));
        when(denunciaRepository.findById(10L)).thenReturn(Optional.of(issue(10L, user(1L, "autor@test.com", "ROLE_USER"), IssueStatus.ABERTA)));
        when(atendimentoRepository.existsByDenunciaId(10L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.assumir(10L, new AssumirAtendimentoRequestDTO("Vamos corrigir o problema reportado")));
    }

    private void authenticate(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(username, "secret", List.of())
        );
    }

    private Usuario user(Long id, String username, String role) {
        return Usuario.builder().id(id).username(username).nome(username).role(role).build();
    }

    private Denuncia issue(Long id, Usuario autor, IssueStatus status) {
        return Denuncia.builder().id(id).titulo("Buraco").descricao("Descricao").latitude(-27.0).longitude(-48.0)
                .tipo(IssueType.BURACO).status(status).autor(autor).interacoes(List.of()).build();
    }
}
