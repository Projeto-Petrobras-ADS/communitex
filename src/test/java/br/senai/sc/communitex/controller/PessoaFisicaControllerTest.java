package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.PessoaFisicaRequestDTO;
import br.senai.sc.communitex.dto.PessoaFisicaResponseDTO;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.service.JwtService;
import br.senai.sc.communitex.service.impl.PessoaFisicaServiceImpl;
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

@WebMvcTest(PessoaFisicaController.class)
@AutoConfigureMockMvc(addFilters = false)
class PessoaFisicaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PessoaFisicaServiceImpl pessoaFisicaService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void givenPessoasFisicasCadastradas_whenFindAll_thenReturnsOk() throws Exception {
        when(pessoaFisicaService.findAll()).thenReturn(List.of(
                new PessoaFisicaResponseDTO(1L, "Murilo", "12345678901", "murilo@email.com", "48999990000")
        ));

        mockMvc.perform(get("/api/pessoas-fisicas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void givenValidPayload_whenCreate_thenReturnsCreated() throws Exception {
        var request = new PessoaFisicaRequestDTO("Murilo", "12345678901", "murilo@email.com", "48999990000", "senha123");
        var response = new PessoaFisicaResponseDTO(1L, "Murilo", "12345678901", "murilo@email.com", "48999990000");

        when(pessoaFisicaService.create(any(PessoaFisicaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/pessoas-fisicas")
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
                  "cpf": "123",
                  "email": "invalido",
                  "telefone": "abc",
                  "senha": ""
                }
                """;

        mockMvc.perform(post("/api/pessoas-fisicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void givenPessoaFisicaInexistente_whenFindById_thenReturnsNotFound() throws Exception {
        when(pessoaFisicaService.findById(99L)).thenThrow(new ResourceNotFoundException("Pessoa Física não encontrada"));

        mockMvc.perform(get("/api/pessoas-fisicas/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}



