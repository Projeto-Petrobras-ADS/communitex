-- V9: Adicionar coluna 'nome' em usuarios e relacionamento com empresa

-- Adicionar coluna 'nome' na tabela usuarios
ALTER TABLE usuarios ADD COLUMN nome VARCHAR(255) DEFAULT NULL;

-- Adicionar coluna 'usuario_representante_id' na tabela empresas
ALTER TABLE empresas ADD COLUMN usuario_representante_id BIGINT DEFAULT NULL;

-- Adicionar constraint de chave estrangeira
ALTER TABLE empresas ADD CONSTRAINT fk_empresa_usuario_representante
FOREIGN KEY (usuario_representante_id) REFERENCES usuarios(id) ON DELETE SET NULL;

-- Criar índice para melhor performance
CREATE INDEX idx_empresas_usuario_representante_id ON empresas(usuario_representante_id);
