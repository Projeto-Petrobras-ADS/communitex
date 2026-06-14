package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.AssumirAtendimentoRequestDTO;
import br.senai.sc.communitex.dto.AtendimentoDenunciaResponseDTO;
import br.senai.sc.communitex.dto.ConcluirAtendimentoRequestDTO;
import br.senai.sc.communitex.dto.ContestarAtendimentoRequestDTO;
import br.senai.sc.communitex.dto.DenunciaResponseDTO;
import br.senai.sc.communitex.service.AtendimentoDenunciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AtendimentoDenunciaController {

    private final AtendimentoDenunciaService service;

    @PostMapping("/issues/{id}/atendimento")
    @PreAuthorize("hasRole('EMPRESA')")
    public AtendimentoDenunciaResponseDTO assumir(@PathVariable Long id, @Valid @RequestBody AssumirAtendimentoRequestDTO request) {
        return service.assumir(id, request);
    }

    @PatchMapping("/issues/{id}/atendimento/iniciar")
    @PreAuthorize("hasRole('EMPRESA')")
    public AtendimentoDenunciaResponseDTO iniciar(@PathVariable Long id) {
        return service.iniciar(id);
    }

    @PostMapping("/issues/{id}/atendimento/concluir")
    @PreAuthorize("hasRole('EMPRESA')")
    public AtendimentoDenunciaResponseDTO concluir(@PathVariable Long id, @Valid @RequestBody ConcluirAtendimentoRequestDTO request) {
        return service.concluir(id, request);
    }

    @PostMapping("/issues/{id}/atendimento/confirmar")
    @PreAuthorize("hasRole('USER')")
    public AtendimentoDenunciaResponseDTO confirmar(@PathVariable Long id) {
        return service.confirmar(id);
    }

    @PostMapping("/issues/{id}/atendimento/contestar")
    @PreAuthorize("hasRole('USER')")
    public AtendimentoDenunciaResponseDTO contestar(@PathVariable Long id, @Valid @RequestBody ContestarAtendimentoRequestDTO request) {
        return service.contestar(id, request);
    }

    @GetMapping("/issues/{id}/atendimento")
    public AtendimentoDenunciaResponseDTO buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/atendimentos/disponiveis")
    @PreAuthorize("hasRole('EMPRESA')")
    public List<DenunciaResponseDTO> disponiveis() {
        return service.listarDisponiveis();
    }

    @GetMapping("/atendimentos/meus")
    @PreAuthorize("hasRole('EMPRESA')")
    public List<AtendimentoDenunciaResponseDTO> meus() {
        return service.listarMeusAtendimentos();
    }
}
