package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.DenunciaInteracaoRequestDTO;
import br.senai.sc.communitex.dto.DenunciaInteracaoResponseDTO;
import br.senai.sc.communitex.dto.DenunciaRequestDTO;
import br.senai.sc.communitex.dto.IssueStatusUpdateRequest;
import br.senai.sc.communitex.dto.DenunciaResponseDTO;
import br.senai.sc.communitex.enums.InteractionType;
import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.enums.IssueType;
import br.senai.sc.communitex.exception.DuplicateIssueException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.service.JwtService;
import br.senai.sc.communitex.service.DenunciaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DenunciaController.class)
@AutoConfigureMockMvc(addFilters = false)
class IssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DenunciaService issueService;

    @MockitoBean
    private JwtService jwtService;

    @Test
        void dadoPedidoValidoAoCriar_deveRetornarCriado() throws Exception {
        var request = new DenunciaRequestDTO("Buraco", "Buraco perigoso", -27.6, -48.5, IssueType.BURACO);
        var response = new DenunciaResponseDTO(
                1L,
                "Buraco",
                "Buraco perigoso",
                -27.6,
                -48.5,
                null,
                IssueStatus.ABERTA,
                IssueType.BURACO,
                LocalDateTime.now(),
                10L,
                "Murilo",
                0,
                0
        );

        when(issueService.criar(any(DenunciaRequestDTO.class), nullable(org.springframework.web.multipart.MultipartFile.class))).thenReturn(response);

        mockMvc.perform(multipart("/api/issues")
                        .file(jsonPart("dados", objectMapper.writeValueAsString(request))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Buraco"));
    }

    @Test
        void dadoPedidoInvalidoAoCriar_deveRetornarBadRequest() throws Exception {
        var invalidPayload = """
                {
                  "titulo": "",
                  "descricao": "",
                  "latitude": null,
                  "longitude": null,
                  "tipo": null
                }
                """;

        mockMvc.perform(multipart("/api/issues").file(jsonPart("dados", invalidPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
        void dadaDenunciaDuplicadaAoCriar_deveRetornarConflict() throws Exception {
        var request = new DenunciaRequestDTO("Buraco", "Buraco perigoso", -27.6, -48.5, IssueType.BURACO);

        when(issueService.criar(any(DenunciaRequestDTO.class), nullable(org.springframework.web.multipart.MultipartFile.class)))
                .thenThrow(new DuplicateIssueException("Já existe denúncia similar"));

        mockMvc.perform(multipart("/api/issues")
                        .file(jsonPart("dados", objectMapper.writeValueAsString(request))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
        void dadaDenunciaInexistenteAoBuscarPorId_deveRetornarNotFound() throws Exception {
        when(issueService.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("Denúncia não encontrada"));

        mockMvc.perform(get("/api/issues/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
        void dadaInteracaoValidaAoAdicionar_deveRetornarCriado() throws Exception {
        var request = new DenunciaInteracaoRequestDTO(InteractionType.COMENTARIO, "Concordo com a denúncia");
        var response = new DenunciaInteracaoResponseDTO(
                20L,
                InteractionType.COMENTARIO,
                "Concordo com a denúncia",
                LocalDateTime.now(),
                10L,
                "Murilo"
        );

        when(issueService.adicionarInteracao(eq(1L), any(DenunciaInteracaoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/issues/{id}/interacoes", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20L));
    }

    @Test
        void dadaInteracaoExistenteAoRemover_deveRetornarNoContent() throws Exception {
        doNothing().when(issueService).removerInteracao(1L, 2L);

        mockMvc.perform(delete("/api/issues/{issueId}/interacoes/{interactionId}", 1L, 2L))
                .andExpect(status().isNoContent());
    }

    @Test
        void dadoStatusValidoAoAtualizar_deveRetornarOk() throws Exception {
        var response = new DenunciaResponseDTO(
                1L,
                "Buraco",
                "Buraco perigoso",
                -27.6,
                -48.5,
                null,
                IssueStatus.EM_ANALISE,
                IssueType.BURACO,
                LocalDateTime.now(),
                10L,
                "Murilo",
                1,
                1
        );

        when(issueService.atualizarStatus(1L, IssueStatus.EM_ANALISE)).thenReturn(response);

        mockMvc.perform(patch("/api/issues/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EM_ANALISE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"));
    }
    private MockMultipartFile jsonPart(String name, String json) {
        return new MockMultipartFile(name, "", MediaType.APPLICATION_JSON_VALUE, json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}



