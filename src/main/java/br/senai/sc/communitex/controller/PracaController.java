package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.PracaRequestDTO;
import br.senai.sc.communitex.dto.PracaResponseDTO;
import br.senai.sc.communitex.service.PracaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pracas")
@Tag(name = "Praças", description = "Endpoints para gerenciamento de praças")
public class PracaController {
    private final PracaService pracaService;

    public PracaController(PracaService pracaService) {
        this.pracaService = pracaService;
    }

    @Operation(summary = "Listar todas as praças")
    @ApiResponse(responseCode = "200", description = "Lista de praças retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<PracaResponseDTO>> findAll() {
        return ResponseEntity.ok(pracaService.findAll());
    }

    @Operation(summary = "Buscar praça por ID")
    @ApiResponse(responseCode = "200", description = "Praça encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Praça não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<PracaResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(pracaService.findById(id));
    }

    @Operation(summary = "Criar nova praça")
    @ApiResponse(responseCode = "201", description = "Praça criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @PostMapping
    public ResponseEntity<PracaResponseDTO> create(@Valid @RequestBody PracaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pracaService.create(dto));
    }

    @Operation(summary = "Atualizar praça existente")
    @ApiResponse(responseCode = "200", description = "Praça atualizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Praça não encontrada")
    @PutMapping("/{id}")
    public ResponseEntity<PracaResponseDTO> update(@PathVariable Long id, @Valid @RequestBody PracaRequestDTO dto) {
        return ResponseEntity.ok(pracaService.update(id, dto));
    }

    @Operation(summary = "Excluir praça")
    @ApiResponse(responseCode = "204", description = "Praça excluída com sucesso")
    @ApiResponse(responseCode = "404", description = "Praça não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pracaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
