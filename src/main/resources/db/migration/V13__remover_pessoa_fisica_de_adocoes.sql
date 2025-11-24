-- Remover constraint check que permite pessoa física ou empresa
ALTER TABLE adocoes DROP CONSTRAINT IF EXISTS check_adocao_adotante;

-- Remover foreign key de pessoa_fisica_id
ALTER TABLE adocoes DROP CONSTRAINT IF EXISTS fk_adocoes_pessoa_fisica;

-- Remover índice de pessoa_fisica_id
DROP INDEX IF EXISTS idx_adocoes_pessoa_fisica_id;

-- Remover coluna pessoa_fisica_id da tabela adocoes
ALTER TABLE adocoes DROP COLUMN IF EXISTS pessoa_fisica_id;

-- Tornar empresa_id obrigatório novamente (NOT NULL)
-- Importante: Certifique-se que não há registros com empresa_id NULL antes de executar
ALTER TABLE adocoes ALTER COLUMN empresa_id SET NOT NULL;

-- NOTA: A tabela pessoas_fisicas e seus dados permanecem intactos
-- Apenas o relacionamento com adocoes foi removido

