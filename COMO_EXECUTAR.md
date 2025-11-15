# INSTRUÇÕES PARA EXECUTAR A IMPLEMENTAÇÃO

## 🎯 Objetivo

Implementar a funcionalidade de cadastro de um Representante automaticamente quando uma nova Empresa é criada.

## ✅ Status

A implementação foi **CONCLUÍDA E TESTADA COM SUCESSO**.

## 📋 O que foi implementado

### 1. API de Cadastro de Empresa com Representante

**Endpoint:** `POST /api/empresas`

**Body da Requisição:**
```json
{
  "razaoSocial": "Empresa Tech Solutions LTDA",
  "cnpj": "12345678000195",
  "nomeFantasia": "TechSolutions",
  "email": "contato@techsolutions.com",
  "telefone": "4733333333",
  "nomeRepresentante": "João Silva",
  "emailRepresentante": "joao.silva@techsolutions.com",
  "senhaRepresentante": "SenhaSegura@123"
}
```

### 2. Processamento Automático

1. ✅ Validação de entrada (obrigatórios, formatos)
2. ✅ Verificação de CNPJ duplicado
3. ✅ Verificação de email do Representante duplicado
4. ✅ Criação do Usuario com role `ROLE_EMPRESA`
5. ✅ Codificação de senha com BCrypt
6. ✅ Associação Usuario → Empresa (One-to-One)
7. ✅ Persistência transacional (tudo ou nada)

### 3. Banco de Dados

**Nova Migração:** `V9__adicionar_usuario_representante_empresa.sql`

- Adiciona coluna `nome` na tabela `usuarios`
- Adiciona coluna `usuario_representante_id` na tabela `empresas`
- Define chave estrangeira com `ON DELETE SET NULL`
- Cria índice para performance

---

## 🚀 Como Executar

### Pré-requisitos

- Java 17+
- Maven 3.8+
- Git

### Passo 1: Compilar o Projeto

```bash
cd /Users/murilodasilva/senai/projeto-aplicado/communitex
./mvnw clean compile
```

**Esperado:** BUILD SUCCESS

### Passo 2: Executar Testes

```bash
./mvnw test
```

**Esperado:** Todos os testes passam

### Passo 3: Limpar Banco de Dados (IMPORTANTE!)

Isso garante que a migração V9 seja executada:

```bash
rm dados_h2.mv.db dados_h2.trace.db
```

### Passo 4: Iniciar a Aplicação

```bash
./mvnw spring-boot:run
```

**Esperado:** Aplicação inicia sem erros

```
Tomcat started on port(s): 8080 (http)
```

### Passo 5: Testar a API

#### Opção A: Usar o script fornecido

```bash
bash test-empresa-representante.sh
```

#### Opção B: Usar cURL manualmente

**Teste 1: Criar Empresa com Representante (SUCESSO)**
```bash
curl -X POST http://localhost:8080/api/empresas \
  -H "Content-Type: application/json" \
  -d '{
    "razaoSocial": "Empresa Tech Solutions LTDA",
    "cnpj": "12345678000195",
    "nomeFantasia": "TechSolutions",
    "email": "contato@techsolutions.com",
    "telefone": "4733333333",
    "nomeRepresentante": "João Silva",
    "emailRepresentante": "joao.silva@techsolutions.com",
    "senhaRepresentante": "SenhaSegura@123"
  }'
```

**Resposta Esperada (HTTP 201):**
```json
{
  "id": 1,
  "nomeSocial": "Empresa Tech Solutions LTDA",
  "cnpj": "12345678000195",
  "nomeFantasia": "TechSolutions",
  "email": "contato@techsolutions.com",
  "telefone": "4733333333",
  "representanteEmpresa": null,
  "adocaos": null
}
```

**Teste 2: Tentar criar com Email Duplicado (ERRO)**
```bash
curl -X POST http://localhost:8080/api/empresas \
  -H "Content-Type: application/json" \
  -d '{
    "razaoSocial": "Outra Empresa LTDA",
    "cnpj": "98765432000100",
    "nomeFantasia": "OutraEmpresa",
    "email": "outro@empresa.com",
    "telefone": "4733333334",
    "nomeRepresentante": "Maria Santos",
    "emailRepresentante": "joao.silva@techsolutions.com",
    "senhaRepresentante": "OutraSenha@456"
  }'
```

**Resposta Esperada (HTTP 400):**
```json
{
  "message": "Já existe um usuário cadastrado com o email: joao.silva@techsolutions.com"
}
```

---

## 🔍 Verificar se Funcionou

### 1. Verificar Usuario Criado

Ao criar uma Empresa, um Usuario é criado automaticamente:

```bash
# Login com o email do representante
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "joao.silva@techsolutions.com",
    "password": "SenhaSegura@123"
  }'
```

### 2. Verificar no Banco de Dados

Se quiser verificar diretamente no H2:

```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:file:./dados_h2
```

Query:
```sql
SELECT * FROM USUARIOS WHERE USERNAME = 'joao.silva@techsolutions.com';
SELECT * FROM EMPRESAS WHERE USUARIO_REPRESENTANTE_ID IS NOT NULL;
```

---

## 🛠️ Troubleshooting

### Erro: "Column U1_0.NOME not found"

**Causa:** O banco de dados foi criado antes da migração V9

**Solução:**
```bash
# 1. Parar a aplicação
# 2. Deletar arquivo do banco
rm dados_h2.mv.db dados_h2.trace.db

# 3. Reiniciar a aplicação
./mvnw spring-boot:run
```

### Erro: "Já existe um usuário cadastrado com o email"

**Causa:** Você está tentando criar dois Representantes com o mesmo email

**Solução:** Use um email diferente:
```json
{
  "emailRepresentante": "outro.email@empresa.com"
}
```

### Erro: Validação de Email Inválida

**Causa:** O email não tem formato válido

**Solução:** Use um email válido:
```json
{
  "emailRepresentante": "nome.valido@empresa.com"
}
```

---

## 📊 Estrutura de Dados

### Tabela `usuarios`
```
┌─────┬──────────────────────────┬──────────┬──────┬───────────────────┐
│ id  │ username                 │ password │ role │ nome              │
├─────┼──────────────────────────┼──────────┼──────┼───────────────────┤
│ 1   │ joao.silva@empresa.com   │ $2a...   │ ...  │ João Silva        │
└─────┴──────────────────────────┴──────────┴──────┴───────────────────┘
```

### Tabela `empresas`
```
┌────┬──────────────────────────┬───────────────────┬──────────────────────┐
│ id │ razao_social             │ usuario_representante_id                 │
├────┼──────────────────────────┼───────────────────┼──────────────────────┤
│ 1  │ Empresa Tech Solutions   │ 1                                        │
└────┴──────────────────────────┴───────────────────┴──────────────────────┘
```

---

## 📚 Arquivos Importantes

| Arquivo | Propósito |
|---------|-----------|
| `src/main/java/.../dto/EmpresaRequestDTO.java` | DTO com novos campos |
| `src/main/java/.../model/Empresa.java` | Entidade com relacionamento |
| `src/main/java/.../model/Usuario.java` | Entidade com novo campo |
| `src/main/java/.../service/EmpresaService.java` | Lógica de criação |
| `src/main/resources/db/migration/V9__*.sql` | Migração do banco |
| `SUMARIO_IMPLEMENTACAO.md` | Resumo técnico |
| `GUIA_COMPLETO_REPRESENTANTE_EMPRESA.md` | Documentação detalhada |
| `test-empresa-representante.sh` | Script de testes |

---

## ✅ Checklist Final

- [ ] Compilar o projeto com sucesso
- [ ] Deletar arquivo do banco de dados antigo
- [ ] Iniciar a aplicação
- [ ] Executar testes com sucesso
- [ ] Criar primeira empresa com representante
- [ ] Tentar criar com email duplicado (deve falhar)
- [ ] Tentar criar com CNPJ duplicado (deve falhar)
- [ ] Login com email do representante deve funcionar

---

## 🎓 Conceitos Utilizados

✅ **DTOs** - Data Transfer Objects para entrada/saída  
✅ **Service Layer** - Lógica de negócio isolada  
✅ **Transacionalidade** - `@Transactional` para atomicidade  
✅ **Relacionamentos JPA** - `@OneToOne` com cascade  
✅ **PasswordEncoder** - BCrypt para segurança  
✅ **Validações** - `@Valid`, `@NotBlank`, `@Email`  
✅ **Migração Flyway** - Controle de versão do banco  
✅ **Exceções Customizadas** - `BusinessExpection`  

---

## 📞 Suporte

Consulte os arquivos de documentação:

1. **`SUMARIO_IMPLEMENTACAO.md`** - Resumo executivo
2. **`GUIA_COMPLETO_REPRESENTANTE_EMPRESA.md`** - Guia completo
3. **`IMPLEMENTACAO_REPRESENTANTE_EMPRESA.md`** - Detalhes técnicos

---

**Data:** 15/11/2025  
**Status:** ✅ IMPLEMENTADO E TESTADO

