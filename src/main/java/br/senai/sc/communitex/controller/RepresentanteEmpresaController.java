package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.RepresentanteEmpresaRequestDTO;
import br.senai.sc.communitex.dto.RepresentanteEmpresaResponseDTO;
import br.senai.sc.communitex.service.RepresentanteEmpresaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/representantes")
public class RepresentanteEmpresaController {

    private final RepresentanteEmpresaService representanteService;

    public RepresentanteEmpresaController(RepresentanteEmpresaService representanteService) {
        this.representanteService = representanteService;
    }

    @PostMapping
    public ResponseEntity<RepresentanteEmpresaResponseDTO> create(@RequestBody RepresentanteEmpresaRequestDTO dto) {
        return ResponseEntity.ok(representanteService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<RepresentanteEmpresaResponseDTO>> findAll() {
        return ResponseEntity.ok(representanteService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepresentanteEmpresaResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(representanteService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RepresentanteEmpresaResponseDTO> update(@PathVariable Long id,
                                                                  @RequestBody RepresentanteEmpresaRequestDTO dto) {
        return ResponseEntity.ok(representanteService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        representanteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
