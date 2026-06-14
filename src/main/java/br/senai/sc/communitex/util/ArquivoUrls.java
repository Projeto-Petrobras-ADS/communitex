package br.senai.sc.communitex.util;

import br.senai.sc.communitex.model.Arquivo;

public final class ArquivoUrls {

    private ArquivoUrls() {
    }

    public static String url(Arquivo arquivo) {
        return arquivo == null || arquivo.getId() == null ? null : "/api/arquivos/" + arquivo.getId() + "/conteudo";
    }
}
