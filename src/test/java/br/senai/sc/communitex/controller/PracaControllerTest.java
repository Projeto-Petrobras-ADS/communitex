package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.PracaDetailResponseDTO;
import br.senai.sc.communitex.dto.PracaRequestDTO;
import br.senai.sc.communitex.dto.PracaResponseDTO;
import br.senai.sc.communitex.enums.StatusPraca;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.service.JwtService;
import br.senai.sc.communitex.service.PracaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PracaController.class)
@AutoConfigureMockMvc(addFilters = false)
class PracaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PracaService pracaService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void givenPracasCadastradas_whenFindAll_thenReturnsOk() throws Exception {
        when(pracaService.findAll(any())).thenReturn(List.of(
                new PracaResponseDTO(1L, "Praca Central", "Rua A", "Centro", "Floripa", -27.6, -48.5, "Descricao", null, 1000.0, StatusPraca.DISPONIVEL)
        ));

        mockMvc.perform(get("/api/pracas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nome").value("Praca Central"));
    }

    @Test
    void givenValidPayload_whenCreate_thenReturnsCreated() throws Exception {
        var request = new PracaRequestDTO("Praca Central", "Rua A", "Centro", "Floripa", -27.6, -48.5, "Descricao", null, 1000.0, StatusPraca.DISPONIVEL);
        var response = new PracaResponseDTO(1L, "Praca Central", "Rua A", "Centro", "Floripa", -27.6, -48.5, "Descricao", null, 1000.0, StatusPraca.DISPONIVEL);

        when(pracaService.create(any(PracaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/pracas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void givenInvalidPayload_whenCreate_thenReturnsBadRequest() throws Exception {
        var invalidPayload = """
                {
                  "nome": "",
                  "cidade": ""
                }
                """;

        mockMvc.perform(post("/api/pracas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void givenPracaInexistente_whenFindById_thenReturnsNotFound() throws Exception {
        when(pracaService.findById(99L)).thenThrow(new ResourceNotFoundException("Praça não encontrada"));

        mockMvc.perform(get("/api/pracas/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void givenPracaExistente_whenFindByIdWithDetails_thenReturnsOk() throws Exception {
        var detail = new PracaDetailResponseDTO(1L, "Praca Central", "Rua A", "Centro", "Floripa", -27.6, -48.5, "Descricao", null, 1000.0, StatusPraca.DISPONIVEL, null, List.of());
        when(pracaService.findByIdWithDetails(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/pracas/{id}/detalhes", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void givenPracaExistente_whenDelete_thenReturnsNoContent() throws Exception {
        doNothing().when(pracaService).delete(eq(1L));

        mockMvc.perform(delete("/api/pracas/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}



