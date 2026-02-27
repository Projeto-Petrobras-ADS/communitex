package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.InteresseAdocaoRequestDTO;
import br.senai.sc.communitex.dto.InteresseAdocaoResponseDTO;
import br.senai.sc.communitex.dto.PropostaEmpresaDTO;
import br.senai.sc.communitex.service.IAdocaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/adocao")
@RequiredArgsConstructor
@Tag(name = "Adoções", description = "Endpoints para gerenciamento de adoções de praças")
public class AdocaoController {

    private final IAdocaoService adocaoService;

    @Operation(
        summary = "Registrar interesse de adoção",
        description = "Permite que uma empresa registre interesse em adotar uma praça. " +
                      "O responsável pela praça será notificado por email.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Interesse registrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Praça ou empresa não encontrada")
    @ApiResponse(responseCode = "403", description = "Acesso negado - apenas empresas podem registrar interesse")
    @PreAuthorize("hasRole('EMPRESA')")
    @PostMapping("/interesse")
    @ResponseStatus(HttpStatus.CREATED)
    public InteresseAdocaoResponseDTO registrarInteresse(
            @Valid @RequestBody InteresseAdocaoRequestDTO requestDTO) {
        return adocaoService.registrarInteresse(requestDTO);
    }

    @Operation(
        summary = "Listar minhas propostas",
        description = "Lista todas as propostas de adoção da empresa autenticada. " +
                      "A empresa é identificada automaticamente pelo token JWT.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Lista de propostas retornada com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado - apenas empresas podem visualizar suas propostas")
    @PreAuthorize("hasRole('EMPRESA')")
    @GetMapping("/minhas-propostas")
    public List<PropostaEmpresaDTO> listarMinhasPropostas() {
        return adocaoService.listarPropostasMinhasEmpresa();
    }
}
