package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.AdocaoRequestDTO;
import br.senai.sc.communitex.dto.AdocaoResponseDTO;
import br.senai.sc.communitex.model.Adocao;
import br.senai.sc.communitex.service.AdocaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adocoes")
@Tag(name = "Adoções", description = "Endpoints para gerenciamento de adoções")
public class AdocaoController {
    private final AdocaoService adocaoService;

    public AdocaoController(AdocaoService adocaoService){
        this.adocaoService = adocaoService;
    }

    @Operation(summary = "Criar nova adoção")
    @ApiResponse(responseCode = "200", description = "Adoção criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @PostMapping
    public ResponseEntity<AdocaoResponseDTO> create(@RequestBody AdocaoRequestDTO dto){
        return ResponseEntity.ok(adocaoService.create(dto));
    }

    @Operation(summary = "Listar todas as adoções")
    @ApiResponse(responseCode = "200", description = "Lista de adoções retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<AdocaoResponseDTO>> findAll(){
        return ResponseEntity.ok(adocaoService.findAll());
    }

    @Operation(summary = "Buscar adoção por ID")
    @ApiResponse(responseCode = "200", description = "Adoção encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Adoção não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<AdocaoResponseDTO> findById(@PathVariable Long id){
        return ResponseEntity.ok(adocaoService.findById(id));
    }

    @Operation(summary = "Atualizar adoção existente")
    @ApiResponse(responseCode = "200", description = "Adoção atualizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Adoção não encontrada")
    @PutMapping("/{id}")
    public ResponseEntity<AdocaoResponseDTO> update(@PathVariable Long id,
                                                    @RequestBody AdocaoRequestDTO dto){
        return ResponseEntity.ok(adocaoService.update(id,dto));
    }

    @Operation(summary = "Excluir adoção")
    @ApiResponse(responseCode = "204", description = "Adoção excluída com sucesso")
    @ApiResponse(responseCode = "404", description = "Adoção não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        adocaoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Finalização da adoção")
    @ApiResponse(responseCode = "200", description = "Adoção finalizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Adoção não encontrada")
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<AdocaoResponseDTO> finalizeAdoption(@PathVariable Long id){
        AdocaoResponseDTO response = adocaoService.finalizeAdoption(id);
        return ResponseEntity.ok(response);

    }
}
