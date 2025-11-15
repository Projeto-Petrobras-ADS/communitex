# 🔍 Entendimento: Por que recebe 403 em vez de 400 Bad Request?

## ❌ O Problema

Quando você faz um POST com dados inválidos (erro de validação), recebe:
```
HTTP 403 Forbidden
```

Mas deveria receber:
```
HTTP 400 Bad Request
```

---

## 🎯 Root Cause (Causa Raiz)

O erro **403 ocorre ANTES** do seu exception handler ser acionado. Existem 3 cenários:

### Cenário 1: Token JWT Inválido/Ausente ❌

```bash
curl -X POST 'http://localhost:8080/api/pracas' \
  -H 'Content-Type: application/json' \
  -d '{"nome": "Praça", ...}'
# SEM Header Authorization
```

**O que acontece:**
1. Requisição chega no `SecurityFilterChain`
2. `JwtAuthenticationFilter` não encontra token válido
3. Spring Security retorna **403 Forbidden** 
4. ❌ Nunca chega no controlador
5. ❌ Exception handler nunca é executado

**Solução:** Sempre enviar um token válido!

```bash
TOKEN="seu_token_válido"
curl -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"nome": "Praça", ...}'
```

### Cenário 2: Dados JSON Inválidos (SEM Token) ❌

```bash
curl -X POST 'http://localhost:8080/api/pracas' \
  -H 'Content-Type: application/json' \
  -d '{"nome": invalid json}' # JSON malformado
```

**O que acontece:**
1. Requisição chega no `SecurityFilterChain`
2. Sem token → **403 Forbidden** primeiro
3. ❌ Erro de JSON nunca é verificado

**Solução:** Enviar token válido + JSON correto

### Cenário 3: Dados JSON Válidos, MAS Validação de Negócio Falha (COM Token) ✅

```bash
TOKEN="seu_token_válido"
curl -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"nome": "", "latitude": 999}' # Campos vazios/inválidos
```

**O que acontece:**
1. Requisição passa no `SecurityFilterChain` (token válido)
2. Chega no controlador
3. `@Valid` detecta erro de validação
4. Spring ativa `MethodArgumentNotValidException`
5. ✅ **Agora seu exception handler intercepta!**
6. ✅ Retorna **400 Bad Request** com detalhes

---

## ✅ A Solução

### Mudança 1: Melhorar GlobalExceptionHandler

**Já feito!** Adicionei captura de:
- ✅ `MethodArgumentNotValidException` → 400 Bad Request
- ✅ `Exception` genérica → 500 Internal Server Error
- ✅ Logger para debug

### Mudança 2: Sempre Usar Token Válido

Seu curl deve ser:

```bash
# 1. Obter token
TOKEN=$(curl -s -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password"}' \
  | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# 2. Usar token em TODAS as requisições
curl -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "nome": "Praça Teste",
    "logradouro": "Rua Teste, 123",
    "bairro": "Bairro",
    "cidade": "Cidade",
    "latitude": -23.5,
    "longitude": -46.6,
    "descricao": "Teste",
    "fotoUrl": "https://exemplo.com/img.jpg",
    "status": "ATIVA"
  }'
```

---

## 🔄 Fluxo de Requisição (COM Token Válido)

```
Requisição HTTP
     ↓
┌────────────────────────────────────┐
│  SecurityFilterChain               │
│  1. CORS headers verificados ✅    │
│  2. Token JWT verificado ✅        │
│  3. Autenticação definida ✅       │
└────────────────────────────────────┘
     ↓
┌────────────────────────────────────┐
│  Controlador                       │
│  1. @Valid valida o request body   │
│     • Se VÁLIDO → Processa request │
│     • Se INVÁLIDO → Lança exceção  │
└────────────────────────────────────┘
     ↓ (Se erro de validação)
┌────────────────────────────────────┐
│  GlobalExceptionHandler ✅         │
│  handleValidationException()       │
│  → HTTP 400 Bad Request            │
│  → Retorna detalhes do erro        │
└────────────────────────────────────┘
```

---

## 📊 Comparação: Antes vs Depois

### ANTES (Seu código original)

```bash
# Com token válido + dados inválidos
curl -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"nome": "", "latitude": 999}'

# Resposta:
HTTP 400 Bad Request ✅
Mas o formato da resposta era inconsistente
```

### DEPOIS (Com melhorias)

```bash
# Com token válido + dados inválidos
curl -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"nome": "", "latitude": 999}'

# Resposta:
HTTP 400 Bad Request ✅
{
  "status": 400,
  "message": "nome: não deve estar vazio, latitude: deve estar entre -90 e 90"
}
```

---

## 🧪 Testes para Validar

### Teste 1: SEM Token (Esperado: 403) ✅

```bash
curl -i -X POST 'http://localhost:8080/api/pracas' \
  -H 'Content-Type: application/json' \
  -d '{"nome": "Teste"}'

# Resultado esperado: HTTP 403 Forbidden
# Motivo: Sem autenticação, Spring Security bloqueia
```

### Teste 2: COM Token + Dados Válidos (Esperado: 201) ✅

```bash
TOKEN="seu_token"
curl -i -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "nome": "Praça Válida",
    "logradouro": "Rua, 123",
    "bairro": "Bairro",
    "cidade": "Cidade",
    "latitude": -23.5,
    "longitude": -46.6,
    "descricao": "OK",
    "fotoUrl": "https://exemplo.com/img.jpg",
    "status": "ATIVA"
  }'

# Resultado esperado: HTTP 201 Created
# Resposta: {"id": 1, "nome": "Praça Válida", ...}
```

### Teste 3: COM Token + Dados Inválidos (Esperado: 400) ✅

```bash
TOKEN="seu_token"
curl -i -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "nome": "",
    "logradouro": "Rua, 123",
    "bairro": "",
    "cidade": "Cidade",
    "latitude": 999,
    "longitude": -46.6,
    "descricao": "OK",
    "fotoUrl": "https://exemplo.com/img.jpg",
    "status": "STATUS_INVALIDO"
  }'

# Resultado esperado: HTTP 400 Bad Request ✅
# Resposta: {"status": 400, "message": "nome: não deve estar vazio, ..."}
# NÃO DEVE ser 403! ✅
```

---

## 📝 Resumo

| Cenário | Sem Token | Com Token Válido |
|---------|-----------|------------------|
| Dados válidos | 403 ❌ | 201 ✅ |
| Dados inválidos | 403 ❌ | 400 ✅ |
| JSON malformado | 403 ❌ | 400 ✅ |
| Erro de negócio | 403 ❌ | 400 ✅ |
| Erro interno | 403 ❌ | 500 ✅ |

**Conclusão:** Você está recebendo 403 porque falta o Token em suas requisições!

---

## 🎯 Ação Recomendada

1. ✅ Gere sempre um token válido antes de fazer POST/PUT/DELETE
2. ✅ Use o `Authorization: Bearer $TOKEN` em TODAS as requisições autenticadas
3. ✅ O `GlobalExceptionHandler` agora captura e retorna erros com status correto (400, 500, etc)

Se seguir isso, você verá:
- **403** apenas quando NÃO tiver token (esperado!)
- **400** quando os dados estiverem inválidos (agora! ✅)
- **201/200** quando os dados estiverem válidos (agora! ✅)


