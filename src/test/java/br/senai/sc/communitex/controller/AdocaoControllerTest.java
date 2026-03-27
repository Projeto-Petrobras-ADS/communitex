package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.InteresseAdocaoRequestDTO;
import br.senai.sc.communitex.dto.InteresseAdocaoResponseDTO;
import br.senai.sc.communitex.dto.PropostaEmpresaDTO;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.exception.ForbiddenException;
import br.senai.sc.communitex.service.IAdocaoService;
import br.senai.sc.communitex.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdocaoController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdocaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IAdocaoService adocaoService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void givenValidPayload_whenRegistrarInteresse_thenReturnsCreated() throws Exception {
        var request = new InteresseAdocaoRequestDTO(1L, "Projeto de revitalizacao");
        var response = new InteresseAdocaoResponseDTO(10L, 1L, "Praca Central", 2L, "Empresa X", "Projeto de revitalizacao", StatusAdocao.PROPOSTA, LocalDate.now());

        when(adocaoService.registrarInteresse(any(InteresseAdocaoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/adocao/interesse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void givenInvalidPayload_whenRegistrarInteresse_thenReturnsBadRequest() throws Exception {
        var invalidPayload = """
                {
                  "pracaId": null,
                  "proposta": ""
                }
                """;

        mockMvc.perform(post("/api/adocao/interesse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void givenServiceForbidden_whenRegistrarInteresse_thenReturnsForbidden() throws Exception {
        var request = new InteresseAdocaoRequestDTO(1L, "Projeto de revitalizacao");

        when(adocaoService.registrarInteresse(any(InteresseAdocaoRequestDTO.class)))
                .thenThrow(new ForbiddenException("Acesso negado"));

        mockMvc.perform(post("/api/adocao/interesse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void givenEmpresaAutenticada_whenListarMinhasPropostas_thenReturnsOk() throws Exception {
        when(adocaoService.listarPropostasMinhasEmpresa()).thenReturn(List.of(
                new PropostaEmpresaDTO(1L, 1L, "Praca Central", "Floripa", "Projeto", StatusAdocao.PROPOSTA, LocalDate.now(), LocalDate.now(), null)
        ));

        mockMvc.perform(get("/api/adocao/minhas-propostas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}



