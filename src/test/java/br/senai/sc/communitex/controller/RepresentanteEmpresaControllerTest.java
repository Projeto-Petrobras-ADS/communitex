package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.RepresentanteEmpresaRequestDTO;
import br.senai.sc.communitex.dto.RepresentanteEmpresaResponseDTO;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.service.JwtService;
import br.senai.sc.communitex.service.RepresentanteEmpresaService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RepresentanteEmpresaController.class)
@AutoConfigureMockMvc(addFilters = false)
class RepresentanteEmpresaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RepresentanteEmpresaService representanteService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void givenValidPayload_whenCreate_thenReturnsOk() throws Exception {
        var request = new RepresentanteEmpresaRequestDTO("Joao", true, "joao@empresa.com", 1L);
        var response = new RepresentanteEmpresaResponseDTO(10L, "Joao", true, "joao@empresa.com", 1L, "Empresa X");

        when(representanteService.create(any(RepresentanteEmpresaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/representantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void givenRepresentantesCadastrados_whenFindAll_thenReturnsOk() throws Exception {
        when(representanteService.findAll()).thenReturn(List.of(
                new RepresentanteEmpresaResponseDTO(10L, "Joao", true, "joao@empresa.com", 1L, "Empresa X")
        ));

        mockMvc.perform(get("/api/representantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L));
    }

    @Test
    void givenRepresentanteInexistente_whenFindById_thenReturnsNotFound() throws Exception {
        when(representanteService.findById(99L)).thenThrow(new ResourceNotFoundException("Representante não encontrado"));

        mockMvc.perform(get("/api/representantes/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void givenRepresentanteExistente_whenDelete_thenReturnsNoContent() throws Exception {
        doNothing().when(representanteService).delete(1L);

        mockMvc.perform(delete("/api/representantes/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}



