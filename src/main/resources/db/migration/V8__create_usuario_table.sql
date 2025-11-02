CREATE TABLE usuarios
(
    id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    username varchar(255),
    password varchar(255),
    role     varchar(255),
    refresh_token varchar(512)
);