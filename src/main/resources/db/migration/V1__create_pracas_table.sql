CREATE TABLE pracas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    logradouro VARCHAR(255),
    bairro VARCHAR(255),
    cidade VARCHAR(255) NOT NULL,
    latitude DOUBLE,
    longitude DOUBLE,
    descricao VARCHAR(1000),
    foto_url VARCHAR(255),
    status VARCHAR(50) NOT NULL
);

