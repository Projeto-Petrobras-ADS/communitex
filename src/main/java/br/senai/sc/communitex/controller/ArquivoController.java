package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.service.ArquivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/arquivos")
@RequiredArgsConstructor
public class ArquivoController {

    private final ArquivoService service;

    @GetMapping("/{id}/conteudo")
    public ResponseEntity<byte[]> conteudo(@PathVariable Long id) {
        var arquivo = service.buscar(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(arquivo.getContentType()))
                .contentLength(arquivo.getTamanhoBytes())
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .body(arquivo.getConteudo());
    }
}
