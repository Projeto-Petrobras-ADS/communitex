package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.PublicDashboardDTO;
import br.senai.sc.communitex.service.PublicDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Indicadores agregados da plataforma")
public class PublicDashboardController {

    private final PublicDashboardService dashboardService;

    @GetMapping("/publico")
    @Operation(summary = "Obter indicadores publicos agregados")
    public PublicDashboardDTO obterDashboardPublico() {
        return dashboardService.obterDashboard();
    }
}
