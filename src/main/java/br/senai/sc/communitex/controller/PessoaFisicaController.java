package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.PessoaFisicaRequestDTO;
import br.senai.sc.communitex.dto.PessoaFisicaResponseDTO;
import br.senai.sc.communitex.service.impl.PessoaFisicaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pessoas-fisicas")
@Tag(name = "Pessoas Físicas", description = "Endpoints para gerenciamento de pessoas físicas")
public class PessoaFisicaController {
    private final PessoaFisicaServiceImpl pessoaFisicaServiceImpl;

    public PessoaFisicaController(PessoaFisicaServiceImpl pessoaFisicaServiceImpl) {
        this.pessoaFisicaServiceImpl = pessoaFisicaServiceImpl;
    }

    @Operation(summary = "Listar todas as pessoas físicas")
    @ApiResponse(responseCode = "200", description = "Lista de pessoas físicas retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<PessoaFisicaResponseDTO>> findAll() {
        return ResponseEntity.ok(pessoaFisicaServiceImpl.findAll());
    }

    @Operation(summary = "Buscar pessoa física por ID")
    @ApiResponse(responseCode = "200", description = "Pessoa física encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Pessoa física não encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<PessoaFisicaResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(pessoaFisicaServiceImpl.findById(id));
    }

    @Operation(summary = "Criar nova pessoa física")
    @ApiResponse(responseCode = "201", description = "Pessoa física criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @PostMapping
    public ResponseEntity<PessoaFisicaResponseDTO> create(@Valid @RequestBody PessoaFisicaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pessoaFisicaServiceImpl.create(dto));
    }

    @Operation(summary = "Atualizar pessoa física existente")
    @ApiResponse(responseCode = "200", description = "Pessoa física atualizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Pessoa física não encontrada")
    @PutMapping("/{id}")
    public ResponseEntity<PessoaFisicaResponseDTO> update(@PathVariable Long id, @Valid @RequestBody PessoaFisicaRequestDTO dto) {
        return ResponseEntity.ok(pessoaFisicaServiceImpl.update(id, dto));
    }

    @Operation(summary = "Excluir pessoa física")
    @ApiResponse(responseCode = "204", description = "Pessoa física excluída com sucesso")
    @ApiResponse(responseCode = "404", description = "Pessoa física não encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pessoaFisicaServiceImpl.delete(id);
        return ResponseEntity.noContent().build();
    }
}

