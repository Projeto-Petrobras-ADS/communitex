CREATE TABLE empresas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    razao_social VARCHAR(255) NOT NULL,
    cnpj VARCHAR(20) NOT NULL,
    nome_fantasia VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    telefone VARCHAR(20)
);