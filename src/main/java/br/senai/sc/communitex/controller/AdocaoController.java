package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.AdocaoRequestDTO;
import br.senai.sc.communitex.dto.AdocaoResponseDTO;
import br.senai.sc.communitex.dto.AdocaoStatusResponseDTO;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.service.AdocaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/adocoes")
@Tag(name = "Adoções", description = "Endpoints para gerenciamento de adoções")
public class AdocaoController {
    private final AdocaoService adocaoService;

    public AdocaoController(AdocaoService adocaoService) {
        this.adocaoService = adocaoService;
    }

    @Operation(summary = "Criar nova adoção")
    @ApiResponse(responseCode = "200", description = "Adoção criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @PostMapping
    public ResponseEntity<AdocaoResponseDTO> create(@RequestBody AdocaoRequestDTO dto) {
        return ResponseEntity.ok(adocaoService.create(dto));
    }

    @Operation(summary = "Listar todas as adoções")
    @ApiResponse(responseCode = "200", description = "Lista de adoções retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<AdocaoResponseDTO>> findAll() {
        return ResponseEntity.ok(adocaoService.findAll());
    }

    @Operation(summary = "Buscar adoção por ID")
    @ApiResponse(responseCode = "200", description = "Adoção encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Adoção não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<AdocaoResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(adocaoService.findById(id));
    }

    @Operation(summary = "Atualizar adoção existente")
    @ApiResponse(responseCode = "200", description = "Adoção atualizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Adoção não encontrada")
    @PutMapping("/{id}")
    public ResponseEntity<AdocaoResponseDTO> update(@PathVariable Long id,
                                                    @RequestBody AdocaoRequestDTO dto) {
        return ResponseEntity.ok(adocaoService.update(id, dto));
    }

    @Operation(summary = "Excluir adoção")
    @ApiResponse(responseCode = "204", description = "Adoção excluída com sucesso")
    @ApiResponse(responseCode = "404", description = "Adoção não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adocaoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Finalização da adoção")
    @ApiResponse(responseCode = "200", description = "Adoção finalizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Adoção não encontrada")
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<AdocaoResponseDTO> finalizeAdoption(@PathVariable Long id) {
        AdocaoResponseDTO response = adocaoService.finalizeAdoption(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar por periodo")
    @ApiResponse(responseCode = "200", description = "Busca por periodo encontrada com sucesso!")
    @ApiResponse(responseCode = "404", description = "Busca por periodo não encontrada!")
    @GetMapping("/periodo")
    public ResponseEntity<List<AdocaoResponseDTO>> findByPeriodo(
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fim
    ) {
        return ResponseEntity.ok(adocaoService.findByPeriodo(inicio, fim));
    }

    @Operation(summary = "Buscar adoções por praça")
    @ApiResponse(responseCode = "200", description = "Busca de adoção por praça encontrada com sucesso!")
    @ApiResponse(responseCode = "404", description = "Busca de adoção por praça não encontrada!")
    @GetMapping("/praca/{pracaId}")
    public ResponseEntity<List<AdocaoResponseDTO>> findByPracas(@PathVariable Long pracaId) {
        return ResponseEntity.ok(adocaoService.findByPraca(pracaId));
    }

    @Operation(summary = "Busca de adoções que estão preste a vencer")
    @ApiResponse(responseCode = "200", description = "Busca de adoções preste a vencer realizada com sucesso!")
    @ApiResponse(responseCode = "404", description = "Busca de adoções preste a vencer não encontrada")
    @GetMapping("/filtro/prestes-a-vencer")
    public ResponseEntity<List<AdocaoResponseDTO>> findAdocoesByPrazoEStatus(
            @RequestParam(required = false, defaultValue = "7") Integer dias,
            @RequestParam(required = false) StatusAdocao status
    ) {
        List<AdocaoResponseDTO> resultado = adocaoService.findAdocoesByPrazoEStatus(dias, status);

        return ResponseEntity.ok(resultado);

    }

    @Operation(summary = "Buscar adoções por empresa")
    @ApiResponse(responseCode = "200", description = "Busca de adoção por empresa encontrada com sucesso!")
    @ApiResponse(responseCode = "404", description = "Busca de adoção por empresa não encontrada!")
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<AdocaoResponseDTO>> findByEmpresas(@PathVariable Long empresaId) {
        return ResponseEntity.ok(adocaoService.findByEmpresa(empresaId));

    }

    @Operation(summary = "Buscar adoções por status")
    @ApiResponse(responseCode = "200", description = "Busca de adoção por status encontrada com sucesso!")
    @ApiResponse(responseCode = "404", description = "Busca de adoção por status não encontrada!")
    @GetMapping("/status")
    public ResponseEntity<List<AdocaoStatusResponseDTO>> findByStatus(@RequestParam String status) {
        List<AdocaoStatusResponseDTO> result = adocaoService.findByStatus(StatusAdocao.fromString(status));
        return ResponseEntity.ok(result);
    }
}
