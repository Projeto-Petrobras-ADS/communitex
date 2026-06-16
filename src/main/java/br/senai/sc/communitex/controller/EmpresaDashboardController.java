package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.EmpresaDashboardDTO;
import br.senai.sc.communitex.service.EmpresaDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Indicadores consolidados da empresa autenticada")
public class EmpresaDashboardController {

    private final EmpresaDashboardService dashboardService;

    @GetMapping("/empresa")
    @PreAuthorize("hasRole('EMPRESA')")
    @Operation(summary = "Obter dashboard da empresa", security = @SecurityRequirement(name = "bearerAuth"))
    public EmpresaDashboardDTO obterDashboardEmpresa() {
        return dashboardService.obterDashboard();
    }
}
