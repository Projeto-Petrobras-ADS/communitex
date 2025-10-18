# CommuniTex - Sistema de Gestão para Indústria Têxtil

## Sobre o Projeto

CommuniTex é uma API REST desenvolvida com Spring Boot para gestão de empresas do setor têxtil, focando na administração de praças, empresas, representantes e processos de adoção.

## Tecnologias Utilizadas

- Java 17
- Spring Boot
- Spring Data JPA
- H2 Database
- Swagger/OpenAPI
- Flyway Migration
- Maven

## Pré-requisitos

- Java JDK 17 ou superior
- Maven 3.8.x ou superior

## Configuração e Instalação

1. Clone o repositório:
```bash
git clone [URL_DO_REPOSITORIO]
```

2. Entre no diretório do projeto:
```bash
cd communitex
```

3. Execute o projeto com Maven:
```bash
mvn spring-boot:run
```

## Documentação da API

A documentação da API está disponível através do Swagger UI após iniciar a aplicação:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Docs: http://localhost:8080/api-docs

## Endpoints Principais

### Empresas
- `GET` `/api/empresas` - Lista todas as empresas
- `POST` `/api/empresas` - Cria uma nova empresa
- `GET` `/api/empresas/{id}` - Busca empresa por ID
- `PUT` `/api/empresas/{id}` - Atualiza uma empresa
- `DELETE` `/api/empresas/{id}` - Remove uma empresa

### Praças
- `GET` `/api/pracas` - Lista todas as praças
- `POST` `/api/pracas` - Cria uma nova praça
- `GET` `/api/pracas/{id}` - Busca praça por ID
- `PUT` `/api/pracas/{id}` - Atualiza uma praça
- `DELETE` `/api/pracas/{id}` - Remove uma praça

### Representantes
- `GET` `/representantes` - Lista todos os representantes
- `POST` `/representantes` - Cria um novo representante
- `GET` `/representantes/{id}` - Busca representante por ID
- `PUT` `/representantes/{id}` - Atualiza um representante
- `DELETE` `/representantes/{id}` - Remove um representante

### Adoções
- `GET` `/adocoes` - Lista todas as adoções
- `POST` `/adocoes` - Cria uma nova adoção
- `GET` `/adocoes/{id}` - Busca adoção por ID
- `PUT` `/adocoes/{id}` - Atualiza uma adoção
- `DELETE` `/adocoes/{id}` - Remove uma adoção

## Banco de Dados

O projeto utiliza H2 Database em arquivo para persistência dos dados. O console do H2 pode ser acessado em:
http://localhost:8080/h2-console

Credenciais padrão:
- JDBC URL: jdbc:h2:file:./dados_h2
- Username: sa
- Password: [em branco]

## Contribuição

1. Faça um Fork do projeto
2. Crie uma Branch para sua Feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a Branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.
