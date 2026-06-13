package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.UsuarioDashboardDTO;
import br.senai.sc.communitex.service.JwtService;
import br.senai.sc.communitex.service.UsuarioDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioDashboardService dashboardService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void givenUsuario_whenObterDashboard_thenReturnsAggregatedData() throws Exception {
        when(dashboardService.obterDashboard()).thenReturn(new UsuarioDashboardDTO(
                "Maria", 3, 1, 1, 1, 4, 1, 2, 1, 8, 5, 2, 25,
                List.of(), List.of()
        ));

        mockMvc.perform(get("/api/dashboard/usuario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioNome").value("Maria"))
                .andExpect(jsonPath("$.pracasCadastradas").value(3))
                .andExpect(jsonPath("$.taxaResolucao").value(25));
    }
}
