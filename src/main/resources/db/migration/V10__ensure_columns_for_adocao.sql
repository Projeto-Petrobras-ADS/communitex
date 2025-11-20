-- V10: Garantir colunas necessárias (idempotente para H2 e Postgres)

-- Adiciona coluna 'nome' em usuarios se não existir
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS nome VARCHAR(255);

-- Adiciona coluna 'usuario_representante_id' em empresas se não existir
ALTER TABLE empresas ADD COLUMN IF NOT EXISTS usuario_representante_id BIGINT;

-- Observação: as constraints/foreign keys podem já existir (V9), portanto não re-criaremos aqui para evitar erros.
-- Caso seja necessário, você pode adicionar manualmente uma constraint com um nome específico.

