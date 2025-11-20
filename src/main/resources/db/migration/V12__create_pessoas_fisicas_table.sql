-- Criar tabela pessoas_fisicas
CREATE TABLE pessoas_fisicas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    telefone VARCHAR(11),
    usuario_id BIGINT,
    CONSTRAINT fk_pessoas_fisicas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Adicionar coluna pessoa_fisica_id na tabela adocoes
ALTER TABLE adocoes ADD COLUMN pessoa_fisica_id BIGINT;

-- Adicionar constraint de foreign key
ALTER TABLE adocoes ADD CONSTRAINT fk_adocoes_pessoa_fisica
    FOREIGN KEY (pessoa_fisica_id) REFERENCES pessoas_fisicas(id) ON DELETE CASCADE;

-- Tornar empresa_id nullable (agora pode ser empresa OU pessoa física)
ALTER TABLE adocoes ALTER COLUMN empresa_id DROP NOT NULL;

-- Adicionar constraint para garantir que pelo menos um dos dois (empresa_id ou pessoa_fisica_id) seja preenchido
ALTER TABLE adocoes ADD CONSTRAINT check_adocao_adotante
    CHECK ((empresa_id IS NOT NULL AND pessoa_fisica_id IS NULL) OR
           (empresa_id IS NULL AND pessoa_fisica_id IS NOT NULL));

-- Criar índices para melhor performance
CREATE INDEX idx_pessoas_fisicas_cpf ON pessoas_fisicas(cpf);
CREATE INDEX idx_pessoas_fisicas_email ON pessoas_fisicas(email);
CREATE INDEX idx_adocoes_pessoa_fisica_id ON adocoes(pessoa_fisica_id);

