-- Adicionar coluna pessoa_fisica_id na tabela pracas para indicar quem cadastrou a praça
ALTER TABLE pracas ADD COLUMN cadastrado_por_id BIGINT;

-- Adicionar foreign key
ALTER TABLE pracas
    ADD CONSTRAINT fk_pracas_pessoa_fisica
    FOREIGN KEY (cadastrado_por_id) REFERENCES pessoas_fisicas(id) ON DELETE SET NULL;

-- Criar índice para performance
CREATE INDEX idx_pracas_cadastrado_por_id ON pracas(cadastrado_por_id);

