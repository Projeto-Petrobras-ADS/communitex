ALTER TABLE empresas
    ADD COLUMN representante_id BIGINT;

ALTER TABLE empresas
    ADD CONSTRAINT fk_empresa_representante
        FOREIGN KEY (representante_id) REFERENCES representantes_empresas (id)
            ON
                DELETE
                SET NULL;
