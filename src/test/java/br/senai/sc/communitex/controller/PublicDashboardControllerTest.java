package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.PublicDashboardDTO;
import br.senai.sc.communitex.dto.PublicDashboardMonthlyDTO;
import br.senai.sc.communitex.service.JwtService;
import br.senai.sc.communitex.service.PublicDashboardService;
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

@WebMvcTest(PublicDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicDashboardService dashboardService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void givenPublicRequest_whenObterDashboard_thenReturnsOnlyAggregatedData() throws Exception {
        when(dashboardService.obterDashboard()).thenReturn(new PublicDashboardDTO(
                10, 4, 7200, 8, 18.5, 40, 80,
                List.of(new PublicDashboardMonthlyDTO("2026-06", 4, 8))
        ));

        mockMvc.perform(get("/api/dashboard/publico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPracas").value(10))
                .andExpect(jsonPath("$.areaAdotadaM2").value(7200))
                .andExpect(jsonPath("$.evolucaoMensal[0].mes").value("2026-06"))
                .andExpect(jsonPath("$.empresaNome").doesNotExist())
                .andExpect(jsonPath("$.usuarioNome").doesNotExist());
    }
}
