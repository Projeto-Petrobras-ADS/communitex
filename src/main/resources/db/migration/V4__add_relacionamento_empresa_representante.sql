ALTER TABLE representantes_empresas
    ADD COLUMN empresa_id BIGINT;

ALTER TABLE representantes_empresas
    ADD CONSTRAINT fk_representante_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresas (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE;
