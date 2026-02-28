# Communitex

## Sobre o Projeto

Communitex é uma API REST desenvolvida com Spring Boot 3 para gestão de adoção de praças e denúncias comunitárias. O sistema permite que cidadãos cadastrem praças, registrem problemas urbanos (denúncias), interajam com outras denúncias e que empresas manifestem interesse em adotar praças públicas.

## Tecnologias Utilizadas

- Java 21
- Spring Boot 3
- Spring Data JPA (Hibernate)
- Spring Security + JWT (autenticação stateless)
- PostgreSQL (produção / desenvolvimento local)
- H2 Database (ambiente de desenvolvimento)
- Flyway Migration
- Lombok
- Swagger / OpenAPI (SpringDoc)
- Maven

## Pré-requisitos

- Java JDK 21 ou superior
- Maven 3.8.x ou superior
- Docker (opcional, recomendado para banco local)

## Configuração e Instalação

1. Clone o repositório:
```bash
git clone [URL_DO_REPOSITORIO]
```

2. Entre no diretório do projeto:
```bash
cd communitex
```

## Como Iniciar o Projeto

### Banco de Dados com Docker

Para subir um container PostgreSQL já configurado, execute na raiz do projeto:

```bash
docker-compose up -d
```

Credenciais do container:
- **Usuário:** `devuser`
- **Senha:** `devpass`
- **Banco:** `communitex`
- **Porta:** `5432`

Para parar o banco:
```bash
docker-compose down
```

### Profiles do Spring

O projeto utiliza profiles para separar configurações de ambiente:

| Profile          | Banco de Dados                       | Uso                             |
|------------------|--------------------------------------|---------------------------------|
| `dev`            | H2 em arquivo (`dados_h2`)           | Desenvolvimento local sem Docker|
| `local-postgres` | PostgreSQL via Docker (porta 5432)   | Desenvolvimento local com Docker|
| `prod`           | PostgreSQL (configuração via env)    | Produção                        |

Executar com H2 (padrão):
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Executar com PostgreSQL via Docker:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local-postgres
```

Executar via jar:
```bash
java -jar target/communitex.jar --spring.profiles.active=local-postgres
```

## Documentação da API

Após iniciar a aplicação, acesse:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI Docs:** http://localhost:8080/api-docs

> Endpoints protegidos requerem autenticação via Bearer Token JWT. Use o endpoint `/api/auth/login` para obter o token e clique em **Authorize** no Swagger UI.

## Autenticação

A API utiliza autenticação stateless com JWT (JSON Web Token).

### Fluxo de Autenticação

1. Registre um usuário em `POST /api/auth/register`
2. Autentique-se em `POST /api/auth/login` para obter o `accessToken` e o `refreshToken`
3. Inclua o token no header das requisições protegidas:
   ```
   Authorization: Bearer <accessToken>
   ```
4. Renove o token expirado em `POST /api/auth/refresh`

## Endpoints da API

### Autenticação — `/api/auth`

| Método | Endpoint          | Descrição                        | Auth |
|--------|-------------------|----------------------------------|------|
| `POST` | `/login`          | Autentica usuário e retorna JWT  | ❌   |
| `POST` | `/register`       | Registra novo usuário            | ❌   |
| `POST` | `/refresh`        | Renova o access token            | ❌   |

---

### Denúncias Comunitárias — `/api/issues`

| Método   | Endpoint                              | Descrição                                                              | Auth |
|----------|---------------------------------------|------------------------------------------------------------------------|------|
| `POST`   | `/api/issues`                         | Cria nova denúncia. Verifica duplicidade num raio de 20 m.             | ✅   |
| `GET`    | `/api/issues`                         | Lista todas as denúncias                                               | ❌   |
| `GET`    | `/api/issues/{id}`                    | Busca denúncia por ID                                                  | ❌   |
| `GET`    | `/api/issues/{id}/detalhes`           | Busca denúncia com todas as interações                                 | ❌   |
| `GET`    | `/api/issues/proximidade`             | Lista denúncias por proximidade (lat, long, raioMetros)                | ❌   |
| `PATCH`  | `/api/issues/{id}/status`             | Atualiza o status de uma denúncia                                      | ✅   |
| `POST`   | `/api/issues/{id}/interacoes`         | Adiciona interação (comentário, apoio ou curtida)                      | ✅   |
| `GET`    | `/api/issues/{id}/interacoes`         | Lista todas as interações de uma denúncia                              | ❌   |
| `DELETE` | `/api/issues/{issueId}/interacoes/{interactionId}` | Remove uma interação (apenas o autor)                    | ✅   |

**Tipos de denúncia (`IssueType`):** `ILUMINACAO`, `BURACO`, `LIXO`, `PODA_ARVORE`, `VAZAMENTO`, `PICHACAO`, `CALCADA_DANIFICADA`, `SINALIZACAO`, `OUTRO`

**Status de denúncia (`IssueStatus`):** `ABERTA`, `EM_ANALISE`, `EM_ANDAMENTO`, `RESOLVIDA`, `REJEITADA`

**Tipos de interação (`InteractionType`):** `COMENTARIO`, `APOIO`, `CURTIDA`

---

### Praças — `/api/pracas`

| Método   | Endpoint                  | Descrição                                                    | Auth |
|----------|---------------------------|--------------------------------------------------------------|------|
| `GET`    | `/api/pracas`             | Lista praças com filtros opcionais (id, nome, cidade)        | ❌   |
| `GET`    | `/api/pracas/{id}`        | Busca praça por ID                                           | ❌   |
| `GET`    | `/api/pracas/{id}/detalhes` | Busca praça com dados do cadastrante e histórico de adoções| ❌   |
| `POST`   | `/api/pracas`             | Cria nova praça. Cadastrante obtido pelo JWT.                | ✅   |
| `PUT`    | `/api/pracas/{id}`        | Atualiza praça existente                                     | ✅   |
| `DELETE` | `/api/pracas/{id}`        | Remove praça                                                 | ✅   |

---

### Adoções — `/api/adocao`

| Método | Endpoint                      | Descrição                                                        | Auth         |
|--------|-------------------------------|------------------------------------------------------------------|--------------|
| `POST` | `/api/adocao/interesse`       | Registra interesse de adoção de uma praça (notifica por e-mail)  | ✅ EMPRESA   |
| `GET`  | `/api/adocao/minhas-propostas`| Lista propostas da empresa autenticada                           | ✅ EMPRESA   |

**Status de adoção (`StatusAdocao`):** `PROPOSTA`, `EM_ANALISE`, `APROVADA`, `CONCLUIDA`, `REJEITADA`, `FINALIZADA`

---

### Empresas — `/api/empresas`

| Método   | Endpoint             | Descrição                    | Auth |
|----------|----------------------|------------------------------|------|
| `GET`    | `/api/empresas`      | Lista todas as empresas      | ❌   |
| `GET`    | `/api/empresas/{id}` | Busca empresa por ID         | ❌   |
| `POST`   | `/api/empresas`      | Cria nova empresa            | ❌   |
| `PUT`    | `/api/empresas/{id}` | Atualiza empresa existente   | ❌   |
| `DELETE` | `/api/empresas/{id}` | Remove empresa               | ❌   |

---

### Representantes de Empresa — `/api/representantes`

| Método   | Endpoint                    | Descrição                         | Auth |
|----------|-----------------------------|-----------------------------------|------|
| `GET`    | `/api/representantes`       | Lista todos os representantes     | ❌   |
| `GET`    | `/api/representantes/{id}`  | Busca representante por ID        | ❌   |
| `POST`   | `/api/representantes`       | Cria novo representante           | ❌   |
| `PUT`    | `/api/representantes/{id}`  | Atualiza representante existente  | ❌   |
| `DELETE` | `/api/representantes/{id}`  | Remove representante              | ❌   |

---

### Pessoas Físicas — `/api/pessoas-fisicas`

| Método   | Endpoint                        | Descrição                          | Auth |
|----------|---------------------------------|------------------------------------|------|
| `GET`    | `/api/pessoas-fisicas`          | Lista todas as pessoas físicas     | ❌   |
| `GET`    | `/api/pessoas-fisicas/{id}`     | Busca pessoa física por ID         | ❌   |
| `POST`   | `/api/pessoas-fisicas`          | Cria nova pessoa física            | ❌   |
| `PUT`    | `/api/pessoas-fisicas/{id}`     | Atualiza pessoa física existente   | ❌   |
| `DELETE` | `/api/pessoas-fisicas/{id}`     | Remove pessoa física               | ❌   |

---

## Banco de Dados

### Profile `dev` — H2

Banco em arquivo para desenvolvimento rápido sem dependências externas.

- **Console H2:** http://localhost:8080/h2-console
- **JDBC URL:** `jdbc:h2:file:./dados_h2`
- **Username:** `sa`
- **Password:** *(em branco)*

### Profile `local-postgres` — PostgreSQL via Docker

- **Host:** `localhost:5432`
- **Banco:** `communitex`
- **Username:** `devuser`
- **Password:** `devpass`

### Migrações (Flyway)

O esquema do banco é gerenciado automaticamente pelo Flyway. As migrações ficam em `src/main/resources/db/migration/`.

| Migração | Descrição                                      |
|----------|------------------------------------------------|
| V1       | Criação da tabela `pracas`                     |
| V2       | Criação da tabela `empresas`                   |
| V3       | Criação da tabela `representantes_empresas`    |
| V4       | Relacionamento empresa ↔ representante         |
| V5       | Criação da tabela `adocoes`                    |
| V6       | Adição de e-mail e relacionamento representante|
| V7       | Ajuste de relacionamento empresa/representante |
| V8       | Criação da tabela `usuario`                    |
| V9       | Vincula usuário ao representante de empresa    |
| V10      | Garante colunas para adoção                    |
| V11      | Adiciona campo metragem à praça                |
| V12      | Criação da tabela `pessoas_fisicas`            |
| V13      | Remove pessoa física de adoções                |
| V14      | Vincula pessoa física cadastrante à praça      |
| V15      | Criação da tabela `issues` e `issue_interactions`|

## Arquitetura

O projeto segue arquitetura em camadas:

```
Controller → Service → Repository → Database
```

- **Controller:** Recebe requisições HTTP, valida entrada e delega ao Service. Nunca expõe entidades.
- **Service:** Contém a lógica de negócio. Usa DTOs como contrato de entrada/saída.
- **Repository:** Interfaces JPA para acesso ao banco. Queries nativas/JPQL para consultas complexas.
- **Model (Entity):** Entidades JPA mapeadas para tabelas do banco.
- **DTO:** Records Java imutáveis para transferência de dados.
- **Gateway:** Interface `PhotoStorageGateway` para abstração do armazenamento de fotos.
- **Exception:** Exceções customizadas tratadas via `@RestControllerAdvice`.
- **Enums:** `IssueStatus`, `IssueType`, `InteractionType`, `StatusAdocao`, `StatusPraca`.

## Contribuição

1. Faça um Fork do projeto
2. Crie uma Branch para sua Feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a Branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.
