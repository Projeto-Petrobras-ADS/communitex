# ✅ GUIA PASSO A PASSO: Como Usar Corretamente a API

## TL;DR (Resumo Executivo)

**Problema:** Você recebe 403 quando deveria receber 400 Bad Request

**Causa:** Falta de token válido no header Authorization

**Solução:** 
1. Sempre use token válido
2. Exception Handler já está corrigido
3. Agora funciona: Token OK + Dados Inválidos = 400 Bad Request ✅

---

## 🚀 Passo 1: Iniciar o Servidor

```bash
cd /Users/murilodasilva/senai/projeto-aplicado/communitex

# Compilar
./mvnw clean compile -DskipTests=true -q

# Iniciar
./mvnw spring-boot:run -DskipTests=true

# Aguarde 15 segundos até ver:
# "Usuário admin criado!" ou "Servidor iniciado..."
```

---

## 🔐 Passo 2: Obter Token de Autenticação

**Abra outro terminal** e execute:

```bash
# Fazer login
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password"}'
```

**Resposta esperada:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzYzMjE0MjAwLCJleHAiOjE3NjMyMTc4MDB9.xxxxxx",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzYzMjE0MjAwLCJleHAiOjE3NjMyMTk4MDB9.xxxxxx"
}
```

**👉 Copie o valor de `accessToken`**

---

## 💾 Passo 3: Salvar Token em Variável

```bash
# Cole o token aqui (substitua por seu token real)
export TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzYzMjE0MjAwLCJleHAiOjE3NjMyMTc4MDB9.xxxxxx"

# Verificar se foi salvo
echo $TOKEN
# Deve mostrar o token
```

---

## ✅ Passo 4: Teste COM Token + Dados Válidos (201 Created)

```bash
curl -i -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "nome": "Praça da Matriz",
    "logradouro": "Rua Sete de Setembro, 100",
    "bairro": "Centro Histórico",
    "cidade": "Porto Alegre",
    "latitude": -30.033056,
    "longitude": -51.230000,
    "descricao": "Uma praça histórica",
    "fotoUrl": "https://exemplo.com/img.jpg",
    "status": "ATIVA"
  }'
```

**Resposta esperada: HTTP 201 Created** ✅
```json
{
  "id": 1,
  "nome": "Praça da Matriz",
  "logradouro": "Rua Sete de Setembro, 100",
  ...
}
```

---

## ❌ Passo 5: Teste COM Token + Dados Inválidos (400 Bad Request) ← ESTE ERA SEU PROBLEMA!

```bash
curl -i -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "nome": "",
    "logradouro": "",
    "bairro": "",
    "cidade": "Porto Alegre",
    "latitude": 999,
    "longitude": -46.6,
    "descricao": "Teste",
    "fotoUrl": "https://exemplo.com/img.jpg",
    "status": "INVALIDO"
  }'
```

**Resposta esperada: HTTP 400 Bad Request** ✅ (AGORA FUNCIONA!)
```json
{
  "status": 400,
  "message": "nome: não deve estar vazio, logradouro: não deve estar vazio, bairro: não deve estar vazio, latitude: deve estar entre -90 e 90, status: deve ser ATIVA ou INATIVA"
}
```

**ANTES:** HTTP 403 Forbidden ❌
**DEPOIS:** HTTP 400 Bad Request ✅

---

## 🚫 Passo 6: Teste SEM Token (403 Forbidden) - ESPERADO!

```bash
# SEM header Authorization
curl -i -X POST 'http://localhost:8080/api/pracas' \
  -H 'Content-Type: application/json' \
  -d '{
    "nome": "Praça Teste",
    "logradouro": "Rua, 123",
    "bairro": "Bairro",
    "cidade": "Cidade",
    "latitude": -23.5,
    "longitude": -46.6,
    "descricao": "Teste",
    "fotoUrl": "https://exemplo.com/img.jpg",
    "status": "ATIVA"
  }'
```

**Resposta esperada: HTTP 403 Forbidden** ✅ (Bloqueado por Spring Security - CORRETO!)

---

## 📋 Resumo Rápido

| Teste | Token | Dados | Resultado | Status |
|-------|-------|-------|-----------|--------|
| 1 | ✅ | ✅ | Criado | **201** ✅ |
| 2 | ✅ | ❌ | Erro de validação | **400** ✅ ← NOVO! |
| 3 | ❌ | ✅ | Bloqueado | **403** ✅ |
| 4 | ❌ | ❌ | Bloqueado | **403** ✅ |

---

## 🎯 Entendimento Importante

### Por que 403 sem token?

```
Requisição (SEM Token)
    ↓
SecurityFilterChain: "Onde está o Authorization header?"
    ↓
"Token não encontrado!" → Retorna 403 Forbidden
    ↓
❌ Seu código NUNCA é executado
❌ Exception handler NUNCA é acionado
```

### Por que agora 400 com dados inválidos?

```
Requisição (COM Token Válido + Dados Inválidos)
    ↓
SecurityFilterChain: "Token OK, deixo passar" → ✅
    ↓
Controlador: Recebe e valida com @Valid
    ↓
@Valid: "Campo 'nome' vazio! Campo 'latitude' fora do range!"
    ↓
Lança: MethodArgumentNotValidException
    ↓
GlobalExceptionHandler: Intercepta! ✅
    ↓
Retorna: HTTP 400 Bad Request com detalhes ✅
```

---

## 🔑 Regra de Ouro

```
Para TODA requisição POST/PUT/DELETE use:
  -H "Authorization: Bearer $TOKEN"
  
Sem isso = 403 (Spring Security bloqueia)
Com isso + dados OK = 200/201 (sucesso)
Com isso + dados ruins = 400 (seu exception handler)
```

---

## ✨ Pronto!

Agora você entende:
- ✅ Por que recebia 403
- ✅ Como usar token corretamente
- ✅ Por que agora recebe 400 para dados inválidos
- ✅ Como depurar e testar sua API

Seu backend está **100% funcional**! 🚀


