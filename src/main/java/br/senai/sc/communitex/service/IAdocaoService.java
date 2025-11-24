package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.InteresseAdocaoRequestDTO;
import br.senai.sc.communitex.dto.InteresseAdocaoResponseDTO;
import br.senai.sc.communitex.dto.PropostaEmpresaDTO;

import java.util.List;

public interface IAdocaoService {
    InteresseAdocaoResponseDTO registrarInteresse(InteresseAdocaoRequestDTO requestDTO);
    List<PropostaEmpresaDTO> listarPropostasMinhasEmpresa();
}

