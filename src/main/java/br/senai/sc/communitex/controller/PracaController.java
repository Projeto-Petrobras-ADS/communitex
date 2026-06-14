package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.PracaDetailResponseDTO;
import br.senai.sc.communitex.dto.PracaPesquisaDTO;
import br.senai.sc.communitex.dto.PracaRequestDTO;
import br.senai.sc.communitex.dto.PracaResponseDTO;
import br.senai.sc.communitex.service.PracaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;
import java.util.List;

@RestController
@RequestMapping("/api/pracas")
@RequiredArgsConstructor
@Tag(name = "Praças", description = "Endpoints para gerenciamento de praças")
public class PracaController {

    private final PracaService pracaService;

    @Operation(
        summary = "Listar todas as praças",
        description = "Lista praças com filtros opcionais: id, nome e cidade. " +
                      "Todos os parâmetros são opcionais e podem ser combinados."
    )
    @ApiResponse(responseCode = "200", description = "Lista de praças retornada com sucesso")
    @GetMapping
    public Page<PracaResponseDTO> findAll(@ModelAttribute PracaPesquisaDTO pesquisaDTO, Pageable pageable) {
        return pracaService.findAll(pesquisaDTO, pageable);
    }

    @Operation(summary = "Buscar praça por ID")
    @ApiResponse(responseCode = "200", description = "Praça encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Praça não encontrada")
    @GetMapping("/{id}")
    public PracaResponseDTO findById(@PathVariable Long id) {
        return pracaService.findById(id);
    }

    @Operation(
        summary = "Buscar praça por ID com detalhes completos",
        description = "Retorna informações detalhadas da praça incluindo dados do cadastrante e histórico de interesses"
    )
    @ApiResponse(responseCode = "200", description = "Praça encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Praça não encontrada")
    @GetMapping("/{id}/detalhes")
    public PracaDetailResponseDTO findByIdWithDetails(@PathVariable Long id) {
        return pracaService.findByIdWithDetails(id);
    }

    @Operation(
        summary = "Criar nova praça",
        description = "Cria uma nova praça. A pessoa física cadastrante é obtida automaticamente do token JWT.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Praça criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "403", description = "Acesso negado - apenas pessoas físicas podem cadastrar praças")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PracaResponseDTO create(
            @Valid @RequestPart("dados") PracaRequestDTO dto,
            @RequestPart(value = "arquivo", required = false) MultipartFile arquivo) {
        return pracaService.create(dto, arquivo);
    }

    @Operation(summary = "Atualizar praça existente")
    @ApiResponse(responseCode = "200", description = "Praça atualizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Praça não encontrada")
    @PutMapping("/{id}")
    @PreAuthorize("@authz.isPracaOwnerOrAdmin(#id)")
    public PracaResponseDTO update(@PathVariable Long id, @Valid @RequestBody PracaRequestDTO dto) {
        return pracaService.update(id, dto);
    }

    @Operation(summary = "Excluir praça")
    @ApiResponse(responseCode = "204", description = "Praça excluída com sucesso")
    @ApiResponse(responseCode = "404", description = "Praça não encontrada")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authz.isPracaOwnerOrAdmin(#id)")
    public void delete(@PathVariable Long id) {
        pracaService.delete(id);
    }
}
