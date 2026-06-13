package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.DenunciaDetailResponseDTO;
import br.senai.sc.communitex.dto.DenunciaInteracaoRequestDTO;
import br.senai.sc.communitex.dto.DenunciaInteracaoResponseDTO;
import br.senai.sc.communitex.dto.DenunciaRequestDTO;
import br.senai.sc.communitex.dto.IssueStatusUpdateRequest;
import br.senai.sc.communitex.dto.DenunciaResponseDTO;
import br.senai.sc.communitex.service.DenunciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
@Tag(name = "Denúncias", description = "Endpoints para gerenciamento de denúncias comunitárias")
public class DenunciaController {

    private final DenunciaService issueService;

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
    public DenunciaResponseDTO create(@Valid @RequestBody DenunciaRequestDTO dto) {
        return issueService.criar(dto);
    }

    @Operation(summary = "Listar todas as denúncias")
    @ApiResponse(responseCode = "200", description = "Lista de denúncias retornada com sucesso")
    @GetMapping
    public Page<DenunciaResponseDTO> findAll(Pageable pageable) {
        return issueService.listarTodas(pageable);
    }

    @Operation(
        summary = "Listar denúncias por proximidade",
        description = "Retorna denúncias dentro de um raio especificado (em metros) a partir das coordenadas informadas"
    )
    @ApiResponse(responseCode = "200", description = "Lista de denúncias próximas retornada com sucesso")
    @GetMapping("/proximidade")
    public List<DenunciaResponseDTO> findByProximity(
            @Parameter(description = "Latitude do ponto de referência", required = true)
            @RequestParam Double latitude,
            @Parameter(description = "Longitude do ponto de referência", required = true)
            @RequestParam Double longitude,
            @Parameter(description = "Raio de busca em metros (padrão: 1000)")
            @RequestParam(defaultValue = "1000") Double raioMetros) {
        return issueService.buscarPorProximidade(latitude, longitude, raioMetros);
    }

    @Operation(summary = "Buscar denúncia por ID")
    @ApiResponse(responseCode = "200", description = "Denúncia encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Denúncia não encontrada")
    @GetMapping("/{id}")
    public DenunciaResponseDTO findById(@PathVariable Long id) {
        return issueService.buscarPorId(id);
    }

    @Operation(
        summary = "Buscar denúncia por ID com detalhes completos",
        description = "Retorna informações detalhadas da denúncia incluindo todas as interações (comentários, apoios, curtidas)"
    )
    @ApiResponse(responseCode = "200", description = "Denúncia encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Denúncia não encontrada")
    @GetMapping("/{id}/detalhes")
    public DenunciaDetailResponseDTO findByIdWithDetails(@PathVariable Long id) {
        return issueService.buscarPorIdComDetalhes(id);
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
    @PreAuthorize("hasRole('ADMIN')")
    public DenunciaResponseDTO updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody IssueStatusUpdateRequest body) {
        return issueService.atualizarStatus(id, body.status());
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
    public DenunciaInteracaoResponseDTO addInteraction(
            @PathVariable Long id,
            @Valid @RequestBody DenunciaInteracaoRequestDTO dto) {
        return issueService.adicionarInteracao(id, dto);
    }

    @Operation(summary = "Listar interações de uma denúncia")
    @ApiResponse(responseCode = "200", description = "Lista de interações retornada com sucesso")
    @ApiResponse(responseCode = "404", description = "Denúncia não encontrada")
    @GetMapping("/{id}/interacoes")
    public List<DenunciaInteracaoResponseDTO> findInteractions(@PathVariable Long id) {
        return issueService.listarInteracoesDaDenuncia(id);
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
        issueService.removerInteracao(issueId, interactionId);
    }
}
