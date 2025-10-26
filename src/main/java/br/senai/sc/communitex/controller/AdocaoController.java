package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.AdocaoRequestDTO;
import br.senai.sc.communitex.dto.AdocaoResponseDTO;
import br.senai.sc.communitex.dto.AdocaoStatusResponseDTO;
import br.senai.sc.communitex.enums.StatusAdocao;
import br.senai.sc.communitex.service.AdocaoService;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/adocoes")
public class AdocaoController {
    private final AdocaoService adocaoService;

    public AdocaoController(AdocaoService adocaoService){
        this.adocaoService = adocaoService;
    }

    @PostMapping
    public ResponseEntity<AdocaoResponseDTO> create(@RequestBody AdocaoRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(adocaoService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<AdocaoResponseDTO>> findAll(){
        return  ResponseEntity.ok(adocaoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdocaoResponseDTO> findById(@PathVariable Long id){
        return ResponseEntity.ok(adocaoService.findById(id));
    }

    @GetMapping("/status")
    public ResponseEntity<List<AdocaoStatusResponseDTO>> findByStatus(@RequestParam String status){
        List<AdocaoStatusResponseDTO> result = adocaoService.findByStatus(StatusAdocao.fromString(status));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<AdocaoResponseDTO>> findByEmpresas(@PathVariable Long empresaId){
        return ResponseEntity.ok(adocaoService.findByEmpresa(empresaId));

    }

    @GetMapping("/preste-a-vencer")
    public ResponseEntity<List<AdocaoResponseDTO>> findAdocoesByPrazoEStatus(
            @RequestParam(required = false, defaultValue = "7") Integer dias,
            @RequestParam(required = false) StatusAdocao status
    ){
        List<AdocaoResponseDTO> resultado = adocaoService.findAdocoesByPrazoEStatus(dias, status);

        return ResponseEntity.ok(resultado);

    }

    @GetMapping("/praca/{pracaId}")
    public ResponseEntity<List<AdocaoResponseDTO>> findByPracas(@PathVariable Long pracaId){
        return ResponseEntity.ok(adocaoService.findByPraca(pracaId));
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<AdocaoResponseDTO>> findByPeriodo(
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fim
            ){
        return ResponseEntity.ok(adocaoService.findByPeriodo(inicio, fim));
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
