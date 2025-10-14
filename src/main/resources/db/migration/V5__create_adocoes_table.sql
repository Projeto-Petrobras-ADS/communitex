CREATE TABLE adocoes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    data_inicio DATE NOT NULL,
    data_fim DATE,
    descricao_projeto TEXT,
    status VARCHAR(50) NOT NULL,
    empresa_id BIGINT NOT NULL,
    praca_id BIGINT NOT NULL,
    CONSTRAINT fk_adocao_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    CONSTRAINT fk_adocao_praca FOREIGN KEY (praca_id) REFERENCES pracas(id)
);