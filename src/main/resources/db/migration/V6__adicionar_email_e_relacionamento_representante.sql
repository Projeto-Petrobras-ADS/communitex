ALTER TABLE representantes_empresas
ADD COLUMN IF NOT EXISTS email VARCHAR(255);

ALTER TABLE representantes_empresas
ADD COLUMN IF NOT EXISTS empresa_id BIGINT;

ALTER TABLE representantes_empresas
ADD CONSTRAINT IF NOT EXISTS fk_representante_empresa
FOREIGN KEY (empresa_id) REFERENCES empresas(id)
ON DELETE CASCADE;
