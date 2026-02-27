package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.IssueDetailResponseDTO;
import br.senai.sc.communitex.dto.IssueInteractionRequestDTO;
import br.senai.sc.communitex.dto.IssueInteractionResponseDTO;
import br.senai.sc.communitex.dto.IssueRequestDTO;
import br.senai.sc.communitex.dto.IssueResponseDTO;
import br.senai.sc.communitex.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
@Tag(name = "Denúncias", description = "Endpoints para gerenciamento de denúncias comunitárias")
public class IssueController {

    private final IssueService issueService;

    @Operation(
        summary = "Criar nova denúncia",
        description = "Cria uma nova denúncia comunitária. Verifica automaticamente se existe denúncia similar " +
                      "do mesmo tipo em um raio de 20 metros. O autor é obtido automaticamente do token JWT.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Denúncia criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "409", description = "Já existe uma denúncia similar próxima")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IssueResponseDTO create(@Valid @RequestBody IssueRequestDTO dto) {
        return issueService.create(dto);
    }

    @Operation(summary = "Listar todas as denúncias")
    @ApiResponse(responseCode = "200", description = "Lista de denúncias retornada com sucesso")
    @GetMapping
    public List<IssueResponseDTO> findAll() {
        return issueService.findAll();
    }

    @Operation(
        summary = "Listar denúncias por proximidade",
        description = "Retorna denúncias dentro de um raio especificado (em metros) a partir das coordenadas informadas"
    )
    @ApiResponse(responseCode = "200", description = "Lista de denúncias próximas retornada com sucesso")
    @GetMapping("/proximidade")
    public List<IssueResponseDTO> findByProximity(
            @Parameter(description = "Latitude do ponto de referência", required = true)
            @RequestParam Double latitude,
            @Parameter(description = "Longitude do ponto de referência", required = true)
            @RequestParam Double longitude,
            @Parameter(description = "Raio de busca em metros (padrão: 1000)")
            @RequestParam(defaultValue = "1000") Double raioMetros) {
        return issueService.findByProximity(latitude, longitude, raioMetros);
    }

    @Operation(summary = "Buscar denúncia por ID")
    @ApiResponse(responseCode = "200", description = "Denúncia encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Denúncia não encontrada")
    @GetMapping("/{id}")
    public IssueResponseDTO findById(@PathVariable Long id) {
        return issueService.findById(id);
    }

    @Operation(
        summary = "Buscar denúncia por ID com detalhes completos",
        description = "Retorna informações detalhadas da denúncia incluindo todas as interações (comentários, apoios, curtidas)"
    )
    @ApiResponse(responseCode = "200", description = "Denúncia encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Denúncia não encontrada")
    @GetMapping("/{id}/detalhes")
    public IssueDetailResponseDTO findByIdWithDetails(@PathVariable Long id) {
        return issueService.findByIdWithDetails(id);
    }

    @Operation(
        summary = "Atualizar status da denúncia",
        description = "Atualiza o status de uma denúncia. Status válidos: ABERTA, EM_ANALISE, EM_ANDAMENTO, RESOLVIDA, REJEITADA",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Status inválido")
    @ApiResponse(responseCode = "404", description = "Denúncia não encontrada")
    @PatchMapping("/{id}/status")
    public IssueResponseDTO updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        var status = body.get("status");
        return issueService.updateStatus(id, status);
    }

    @Operation(
        summary = "Adicionar interação à denúncia",
        description = "Adiciona um comentário, apoio ou curtida à denúncia. " +
                      "Cada usuário pode dar apenas um apoio e uma curtida por denúncia.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Interação adicionada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos ou interação duplicada")
    @ApiResponse(responseCode = "404", description = "Denúncia não encontrada")
    @PostMapping("/{id}/interacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public IssueInteractionResponseDTO addInteraction(
            @PathVariable Long id,
            @Valid @RequestBody IssueInteractionRequestDTO dto) {
        return issueService.addInteraction(id, dto);
    }

    @Operation(summary = "Listar interações de uma denúncia")
    @ApiResponse(responseCode = "200", description = "Lista de interações retornada com sucesso")
    @ApiResponse(responseCode = "404", description = "Denúncia não encontrada")
    @GetMapping("/{id}/interacoes")
    public List<IssueInteractionResponseDTO> findInteractions(@PathVariable Long id) {
        return issueService.findInteractionsByIssueId(id);
    }

    @Operation(
        summary = "Remover interação de uma denúncia",
        description = "Remove uma interação (comentário, apoio ou curtida). Apenas o autor da interação pode removê-la.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "204", description = "Interação removida com sucesso")
    @ApiResponse(responseCode = "403", description = "Sem permissão para remover esta interação")
    @ApiResponse(responseCode = "404", description = "Denúncia ou interação não encontrada")
    @DeleteMapping("/{issueId}/interacoes/{interactionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeInteraction(
            @PathVariable Long issueId,
            @PathVariable Long interactionId) {
        issueService.removeInteraction(issueId, interactionId);
    }
}
