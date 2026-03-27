package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.IssueInteractionRequestDTO;
import br.senai.sc.communitex.dto.IssueInteractionResponseDTO;
import br.senai.sc.communitex.dto.IssueRequestDTO;
import br.senai.sc.communitex.dto.IssueResponseDTO;
import br.senai.sc.communitex.enums.InteractionType;
import br.senai.sc.communitex.enums.IssueStatus;
import br.senai.sc.communitex.enums.IssueType;
import br.senai.sc.communitex.exception.DuplicateIssueException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.service.JwtService;
import br.senai.sc.communitex.service.IssueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IssueController.class)
@AutoConfigureMockMvc(addFilters = false)
class IssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IssueService issueService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void givenValidIssueRequest_whenCreate_thenReturnsCreated() throws Exception {
        var request = new IssueRequestDTO("Buraco", "Buraco perigoso", -27.6, -48.5, null, IssueType.BURACO);
        var response = new IssueResponseDTO(
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

        when(issueService.create(any(IssueRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Buraco"));
    }

    @Test
    void givenInvalidIssueRequest_whenCreate_thenReturnsBadRequest() throws Exception {
        var invalidPayload = """
                {
                  "titulo": "",
                  "descricao": "",
                  "latitude": null,
                  "longitude": null,
                  "tipo": null
                }
                """;

        mockMvc.perform(post("/api/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void givenDuplicateIssue_whenCreate_thenReturnsConflict() throws Exception {
        var request = new IssueRequestDTO("Buraco", "Buraco perigoso", -27.6, -48.5, null, IssueType.BURACO);

        when(issueService.create(any(IssueRequestDTO.class)))
                .thenThrow(new DuplicateIssueException("Já existe denúncia similar"));

        mockMvc.perform(post("/api/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void givenIssueInexistente_whenFindById_thenReturnsNotFound() throws Exception {
        when(issueService.findById(99L)).thenThrow(new ResourceNotFoundException("Denúncia não encontrada"));

        mockMvc.perform(get("/api/issues/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void givenValidInteraction_whenAddInteraction_thenReturnsCreated() throws Exception {
        var request = new IssueInteractionRequestDTO(InteractionType.COMENTARIO, "Concordo com a denúncia");
        var response = new IssueInteractionResponseDTO(
                20L,
                InteractionType.COMENTARIO,
                "Concordo com a denúncia",
                LocalDateTime.now(),
                10L,
                "Murilo"
        );

        when(issueService.addInteraction(eq(1L), any(IssueInteractionRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/issues/{id}/interacoes", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20L));
    }

    @Test
    void givenExistingInteraction_whenRemoveInteraction_thenReturnsNoContent() throws Exception {
        doNothing().when(issueService).removeInteraction(1L, 2L);

        mockMvc.perform(delete("/api/issues/{issueId}/interacoes/{interactionId}", 1L, 2L))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenValidStatus_whenUpdateStatus_thenReturnsOk() throws Exception {
        var response = new IssueResponseDTO(
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

        when(issueService.updateStatus(1L, "EM_ANALISE")).thenReturn(response);

        mockMvc.perform(patch("/api/issues/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EM_ANALISE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"));
    }
}



