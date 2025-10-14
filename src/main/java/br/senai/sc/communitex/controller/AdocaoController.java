package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.AdocaoRequestDTO;
import br.senai.sc.communitex.dto.AdocaoResponseDTO;
import br.senai.sc.communitex.service.AdocaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adocoes")
public class AdocaoController {
    private final AdocaoService adocaoService;

    public AdocaoController(AdocaoService adocaoService){
        this.adocaoService = adocaoService;
    }

    @PostMapping
    public ResponseEntity<AdocaoResponseDTO> create(@RequestBody AdocaoRequestDTO dto){
        return ResponseEntity.ok(adocaoService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<AdocaoResponseDTO>> findAll(){
        return  ResponseEntity.ok(adocaoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdocaoResponseDTO> findById(@PathVariable Long id){
        return ResponseEntity.ok(adocaoService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdocaoResponseDTO> update(@PathVariable Long id,
                                                    @RequestBody AdocaoRequestDTO dto){
        return ResponseEntity.ok(adocaoService.update(id,dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        adocaoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
