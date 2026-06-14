package br.senai.sc.communitex.service;

import br.senai.sc.communitex.exception.BusinessException;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Arquivo;
import br.senai.sc.communitex.repository.ArquivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ArquivoService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final ArquivoRepository repository;

    @Transactional
    public Arquivo salvarImagem(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) return null;
        validarImagem(arquivo);
        try {
            return repository.save(Arquivo.builder()
                    .conteudo(arquivo.getBytes())
                    .nomeOriginal(nomeOriginal(arquivo))
                    .contentType(arquivo.getContentType())
                    .tamanhoBytes(arquivo.getSize())
                    .build());
        } catch (IOException ex) {
            throw new BusinessException("Nao foi possivel processar a imagem enviada", ex);
        }
    }

    @Transactional(readOnly = true)
    public Arquivo buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Arquivo nao encontrado com ID: " + id));
    }

    public String url(Arquivo arquivo) {
        return arquivo == null || arquivo.getId() == null ? null : "/api/arquivos/" + arquivo.getId() + "/conteudo";
    }

    private void validarImagem(MultipartFile arquivo) {
        if (arquivo.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException("A imagem deve ter no maximo 5 MB");
        }
        if (!IMAGE_CONTENT_TYPES.contains(arquivo.getContentType())) {
            throw new BusinessException("Formato invalido. Envie uma imagem JPEG, PNG ou WebP");
        }
    }

    private String nomeOriginal(MultipartFile arquivo) {
        return arquivo.getOriginalFilename() == null || arquivo.getOriginalFilename().isBlank()
                ? "imagem"
                : arquivo.getOriginalFilename();
    }
}
