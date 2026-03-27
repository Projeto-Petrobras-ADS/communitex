package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.EmpresaRequestDTO;
import br.senai.sc.communitex.dto.EmpresaResponseDTO;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.service.EmpresaService;
import br.senai.sc.communitex.service.JwtService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmpresaController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmpresaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmpresaService empresaService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void givenEmpresasCadastradas_whenFindAll_thenReturnsOk() throws Exception {
        when(empresaService.findAll()).thenReturn(List.of(
                new EmpresaResponseDTO(1L, "Tech LTDA", "12345678000199", "Tech", "contato@tech.com", "48999990000", null, List.of())
        ));

        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nomeSocial").value("Tech LTDA"));
    }

    @Test
    void givenInvalidPayload_whenCreate_thenReturnsBadRequest() throws Exception {
        var invalidPayload = """
                {
                  "razaoSocial": "",
                  "cnpj": "",
                  "email": "",
                  "nomeRepresentante": "",
                  "emailRepresentante": "",
                  "senhaRepresentante": ""
                }
                """;

        mockMvc.perform(post("/api/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void givenEmpresaInexistente_whenFindById_thenReturnsNotFound() throws Exception {
        when(empresaService.findById(99L)).thenThrow(new ResourceNotFoundException("Empresa não encontrada"));

        mockMvc.perform(get("/api/empresas/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void givenValidPayload_whenCreate_thenReturnsCreated() throws Exception {
        var request = new EmpresaRequestDTO(
                "Tech LTDA",
                "12.345.678/0001-99",
                "Tech",
                "contato@tech.com",
                "(48)99999-0000",
                null,
                "Joao",
                "joao@tech.com",
                "senha123"
        );

        var response = new EmpresaResponseDTO(1L, "Tech LTDA", "12345678000199", "Tech", "contato@tech.com", "48999990000", null, List.of());

        when(empresaService.create(any(EmpresaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }
}



