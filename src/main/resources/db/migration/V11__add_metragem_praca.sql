-- Adicionar coluna de metragem em metros quadrados à tabela praça
ALTER TABLE pracas ADD COLUMN metragem_m2 DOUBLE PRECISION DEFAULT NULL;

-- Adicionar comentário explicativo
COMMENT ON COLUMN pracas.metragem_m2 IS 'Área da praça em metros quadrados';

