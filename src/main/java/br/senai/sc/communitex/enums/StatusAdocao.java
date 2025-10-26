package br.senai.sc.communitex.enums;

public enum StatusAdocao {
    PROPOSTA,
    EM_ANALISE,
    APROVADA,
    CONCLUIDA,
    REJEITADA,
    FINALIZADA;

    public static StatusAdocao fromString(String value){
        for (StatusAdocao status : StatusAdocao.values()){
            if(status.name().equalsIgnoreCase(value)){
                return status;
            }
        }
        throw new IllegalStateException("Status invalido: " + value);
    }
}
