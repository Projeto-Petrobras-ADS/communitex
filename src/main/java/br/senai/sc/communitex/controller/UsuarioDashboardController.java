package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.UsuarioDashboardDTO;
import br.senai.sc.communitex.service.UsuarioDashboardService;
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
@Tag(name = "Dashboard", description = "Indicadores consolidados do usuario autenticado")
public class UsuarioDashboardController {

    private final UsuarioDashboardService dashboardService;

    @GetMapping("/usuario")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Obter dashboard do cidadao", security = @SecurityRequirement(name = "bearerAuth"))
    public UsuarioDashboardDTO obterDashboardUsuario() {
        return dashboardService.obterDashboard();
    }
}
