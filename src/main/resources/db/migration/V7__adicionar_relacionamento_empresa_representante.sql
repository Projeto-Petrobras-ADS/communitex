ALTER TABLE empresas
ADD COLUMN IF NOT EXISTS representante_id BIGINT;

ALTER TABLE empresas
ADD CONSTRAINT IF NOT EXISTS fk_empresa_representante
FOREIGN KEY (representante_id) REFERENCES representantes_empresas(id)
ON DELETE SET NULL;
