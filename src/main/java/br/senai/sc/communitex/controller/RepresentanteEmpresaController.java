package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.RepresentanteEmpresaRequestDTO;
import br.senai.sc.communitex.dto.RepresentanteEmpresaResponseDTO;
import br.senai.sc.communitex.service.RepresentanteEmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/representantes")
@Tag(name = "Representantes", description = "Endpoints para gerenciamento de representantes de empresas")
public class RepresentanteEmpresaController {

    private final RepresentanteEmpresaService representanteService;

    public RepresentanteEmpresaController(RepresentanteEmpresaService representanteService) {
        this.representanteService = representanteService;
    }

    @Operation(summary = "Criar novo representante")
    @ApiResponse(responseCode = "200", description = "Representante criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @PostMapping
    public ResponseEntity<RepresentanteEmpresaResponseDTO> create(@RequestBody RepresentanteEmpresaRequestDTO dto) {
        return ResponseEntity.ok(representanteService.create(dto));
    }

    @Operation(summary = "Listar todos os representantes")
    @ApiResponse(responseCode = "200", description = "Lista de representantes retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<RepresentanteEmpresaResponseDTO>> findAll() {
        return ResponseEntity.ok(representanteService.findAll());
    }

    @Operation(summary = "Buscar representante por ID")
    @ApiResponse(responseCode = "200", description = "Representante encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Representante não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<RepresentanteEmpresaResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(representanteService.findById(id));
    }

    @Operation(summary = "Atualizar representante existente")
    @ApiResponse(responseCode = "200", description = "Representante atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Representante não encontrado")
    @PutMapping("/{id}")
    public ResponseEntity<RepresentanteEmpresaResponseDTO> update(@PathVariable Long id,
                                                                  @RequestBody RepresentanteEmpresaRequestDTO dto) {
        return ResponseEntity.ok(representanteService.update(id, dto));
    }

    @Operation(summary = "Excluir representante")
    @ApiResponse(responseCode = "204", description = "Representante excluído com sucesso")
    @ApiResponse(responseCode = "404", description = "Representante não encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        representanteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
