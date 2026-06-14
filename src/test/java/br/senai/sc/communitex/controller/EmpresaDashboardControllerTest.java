package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.EmpresaDashboardDTO;
import br.senai.sc.communitex.service.EmpresaDashboardService;
import br.senai.sc.communitex.service.JwtService;
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

@WebMvcTest(EmpresaDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmpresaDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmpresaDashboardService dashboardService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void givenEmpresa_whenObterDashboard_thenReturnsAggregatedData() throws Exception {
        when(dashboardService.obterDashboard()).thenReturn(new EmpresaDashboardDTO(
                "Empresa Verde", 12, 4, 1, 2, 1, 2, 2500, 50, 1, 6, 3, 1, 1, 1, 1, 1,
                List.of(), List.of(), List.of()
        ));

        mockMvc.perform(get("/api/dashboard/empresa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresaNome").value("Empresa Verde"))
                .andExpect(jsonPath("$.pracasDisponiveis").value(12))
                .andExpect(jsonPath("$.reparosAtivos").value(3))
                .andExpect(jsonPath("$.denunciasRealizadas").doesNotExist())
                .andExpect(jsonPath("$.taxaAprovacao").value(50));
    }
}
