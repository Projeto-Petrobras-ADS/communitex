package br.senai.sc.communitex.service;

import br.senai.sc.communitex.exception.BusinessException;
import br.senai.sc.communitex.model.Arquivo;
import br.senai.sc.communitex.repository.ArquivoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArquivoServiceTest {

    @Mock
    private ArquivoRepository repository;

    @InjectMocks
    private ArquivoService service;

    @Test
    void salvaImagemValidaComoBlob() {
        var upload = new MockMultipartFile("arquivo", "imagem.png", "image/png", new byte[]{1, 2, 3});
        when(repository.save(any(Arquivo.class))).thenAnswer(invocation -> {
            var arquivo = invocation.getArgument(0, Arquivo.class);
            arquivo.setId(7L);
            return arquivo;
        });

        var arquivo = service.salvarImagem(upload);

        assertEquals(3L, arquivo.getTamanhoBytes());
        assertEquals("image/png", arquivo.getContentType());
        assertEquals("/api/arquivos/7/conteudo", service.url(arquivo));
    }

    @Test
    void arquivoOpcionalPodeSerNulo() {
        assertNull(service.salvarImagem(null));
        verify(repository, never()).save(any());
    }

    @Test
    void rejeitaFormatoInvalido() {
        var upload = new MockMultipartFile("arquivo", "arquivo.txt", "text/plain", new byte[]{1});
        assertThrows(BusinessException.class, () -> service.salvarImagem(upload));
        verify(repository, never()).save(any());
    }

    @Test
    void rejeitaImagemMaiorQueCincoMb() {
        var upload = new MockMultipartFile("arquivo", "imagem.png", "image/png", new byte[5 * 1024 * 1024 + 1]);
        assertThrows(BusinessException.class, () -> service.salvarImagem(upload));
        verify(repository, never()).save(any());
    }
}
