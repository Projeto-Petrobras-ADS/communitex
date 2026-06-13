package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.AdocaoResponseDTO;
import br.senai.sc.communitex.dto.AdocaoStatusUpdateRequestDTO;
import br.senai.sc.communitex.service.AdocaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/propostas")
@RequiredArgsConstructor
@Tag(name = "Propostas", description = "Endpoints administrativos para propostas de adoção")
public class PropostaController {

    private final AdocaoService adocaoService;

    @Operation(
            summary = "Listar propostas para administração",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Lista de propostas retornada com sucesso")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<AdocaoResponseDTO> listarPropostasAdmin() {
        return adocaoService.findAll();
    }

    @Operation(
            summary = "Atualizar status de proposta",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public AdocaoResponseDTO atualizarStatusProposta(
            @PathVariable Long id,
            @Valid @RequestBody AdocaoStatusUpdateRequestDTO requestDTO) {
        return adocaoService.updateStatus(id, requestDTO.status());
    }
}
