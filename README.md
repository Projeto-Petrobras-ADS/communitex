# Communitex

## Sobre o Projeto

Communitex é uma API REST desenvolvida com Spring Boot para gestão de adoção de praça, focando na administração de praças, empresas, representantes e processos de adoção.

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

## Como iniciar o projeto

1. **Pré-requisitos**:
   - Java 17+
   - Maven 3.8+
   - Docker (opcional, recomendado para ambiente local)

2. **Banco de Dados com Docker**:
   Para facilitar o setup do banco de dados PostgreSQL, utilize o Docker. Execute o comando abaixo na raiz do projeto:

   ```bash
   docker-compose up -d
   ```

   Isso irá subir um container com PostgreSQL já configurado (usuário: `devuser`, senha: `devpass`, banco: `communitex`). O banco ficará disponível na porta `5432`.

3. **Configuração dos Profiles do Spring**:
   O projeto utiliza profiles para separar configurações de ambiente. Os principais profiles são:

   - `dev`: Usado para desenvolvimento local. Conecta ao banco do Docker.
   - `prod`: Usado para produção. Requer configuração própria de banco e variáveis.

   Para rodar o projeto com o profile de desenvolvimento:

   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   Ou, se preferir, pode rodar o jar diretamente:

   ```bash
   java -jar target/communitex.jar --spring.profiles.active=dev
   ```

4. **Acessando a aplicação**:
   Após iniciar, acesse a aplicação conforme instruções do projeto (ex: http://localhost:8080).

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
- `GET` `/api/adocoes` - Lista todas as adoções
- `GET` `/api/adocoes/status?status={status}` - Lista adoções com o status desejado
- `GET` `/api/adocoes/empresa/{empresaId}` - Lista todas as adoções de uma empresa
- `GET` `/api/adocoes/praca/{pracaId}` - Lista todas as adoções de uma praça
- `GET` `/api/adocoes/periodo?inicio={dataInicio}&fim={dataFim}` - Lista todas as adoções ative em um determiado periodo
- `GET` `/api/adocoesfiltro//prestes-a-vencer?dias={dias}&status={status}` - Lista todas as adoções que estão preste a vencer
- `POST` `/api/adocoes` - Cria uma nova adoção
- `GET` `/api/adocoes/{id}` - Busca adoção por ID
- `PUT` `/api/adocoes/{id}` - Atualiza uma adoção
- `DELETE` `/api/adocoes/{id}` - Remove uma adoção

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

## Observações

- Certifique-se que o container do banco está rodando antes de iniciar o backend.
- Para parar o banco de dados, execute:

  ```bash
  docker-compose down
  ```
