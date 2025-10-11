package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.PracaRequestDTO;
import br.senai.sc.communitex.dto.PracaResponseDTO;
import br.senai.sc.communitex.service.PracaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pracas")
public class PracaController {
    private final PracaService pracaService;

    public PracaController(PracaService pracaService) {
        this.pracaService = pracaService;
    }

    @GetMapping
    public ResponseEntity<List<PracaResponseDTO>> findAll() {
        return ResponseEntity.ok(pracaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PracaResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(pracaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PracaResponseDTO> create(@Valid @RequestBody PracaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pracaService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PracaResponseDTO> update(@PathVariable Long id, @Valid @RequestBody PracaRequestDTO dto) {
        return ResponseEntity.ok(pracaService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pracaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

