# 📌 Exemplos de CURL - Prontos para Copiar e Colar

## 🔐 Passo 1: Autenticar (Obter Token)

```bash
# Fazer login
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password"}' | jq .
```

**Resposta esperada:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzYzMjEzMjAwLCJleHAiOjE3NjMyMTY4MDB9.gGrTiJ1EGIJrsJGdouFcHbwB0TWwCfSwatE-eY5pMgk",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzYzMjEzMjAwLCJleHAiOjE3NjM4MTgwMDB9.6EszJDUIG9RTGhgz7uoFyhwUdyOOKHPSV92j41Idjls"
}
```

**⚠️ Importante:** Copie o valor de `accessToken`

---

## 📋 Passo 2: Definir Variável com o Token

```bash
# Depois de copiar o token, execute:
TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzYzMjEzMjAwLCJleHAiOjE3NjMyMTY4MDB9.gGrTiJ1EGIJrsJGdouFcHbwB0TWwCfSwatE-eY5pMgk"

# Ou copie toda a linha abaixo (substitua pelo seu token):
TOKEN="seu_token_aqui"

# Verificar se foi definido:
echo $TOKEN
```

---

## 🟢 GET Requests

### 1. Listar todas as praças

```bash
curl -i 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN"
```

**Esperado:** HTTP 200

```bash
curl -s 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" | jq .
```

### 2. Buscar praça por ID

```bash
curl -i 'http://localhost:8080/api/pracas/1' \
  -H "Authorization: Bearer $TOKEN"
```

**Esperado:** HTTP 200 (se existir) ou 404 (se não existir)

---

## 🔵 POST Request - Criar Nova Praça

### Versão 1: Simples (Uma linha)

```bash
curl -i -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Praça da Matriz","logradouro":"Rua Sete de Setembro, 100","bairro":"Centro Histórico","cidade":"Porto Alegre","latitude":-30.033056,"longitude":-51.230000,"descricao":"Uma praça histórica no coração da cidade","fotoUrl":"https://exemplo.com/imagens/praca_matriz.jpg","status":"ATIVA"}'
```

### Versão 2: Formatada (Mais legível)

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
    "descricao": "Uma praça histórica no coração da cidade, cercada por prédios governamentais e culturais.",
    "fotoUrl": "https://exemplo.com/imagens/praca_matriz.jpg",
    "status": "ATIVA"
  }'
```

### Versão 3: Com arquivo JSON externo

```bash
# Criar arquivo praca.json
cat > /tmp/praca.json << 'EOF'
{
  "nome": "Praça da República",
  "logradouro": "Avenida Paulista, 1000",
  "bairro": "Bela Vista",
  "cidade": "São Paulo",
  "latitude": -23.561686,
  "longitude": -46.656385,
  "descricao": "Importante praça de São Paulo",
  "fotoUrl": "https://exemplo.com/imagens/praca_republica.jpg",
  "status": "ATIVA"
}
EOF

# Usar o arquivo
curl -i -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d @/tmp/praca.json
```

**Esperado:** HTTP 201 Created

```json
{
  "id": 1,
  "nome": "Praça da Matriz",
  "logradouro": "Rua Sete de Setembro, 100",
  "bairro": "Centro Histórico",
  "cidade": "Porto Alegre",
  "latitude": -30.033056,
  "longitude": -51.230000,
  "descricao": "Uma praça histórica no coração da cidade, cercada por prédios governamentais e culturais.",
  "fotoUrl": "https://exemplo.com/imagens/praca_matriz.jpg",
  "status": "ATIVA"
}
```

---

## 🟡 PUT Request - Atualizar Praça

```bash
curl -i -X PUT 'http://localhost:8080/api/pracas/1' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "nome": "Praça da Matriz - Atualizada",
    "logradouro": "Rua Sete de Setembro, 100",
    "bairro": "Centro Histórico",
    "cidade": "Porto Alegre",
    "latitude": -30.033056,
    "longitude": -51.230000,
    "descricao": "Uma praça histórica atualizada",
    "fotoUrl": "https://exemplo.com/imagens/praca_matriz_nova.jpg",
    "status": "ATIVA"
  }'
```

**Esperado:** HTTP 200

---

## 🔴 DELETE Request - Deletar Praça

```bash
curl -i -X DELETE 'http://localhost:8080/api/pracas/1' \
  -H "Authorization: Bearer $TOKEN"
```

**Esperado:** HTTP 204 No Content

---

## 🧪 Testes de Segurança

### 1. Requisição SEM token (deve retornar 403)

```bash
curl -i 'http://localhost:8080/api/pracas'
```

**Esperado:** HTTP 403 Forbidden

### 2. Requisição com token INVÁLIDO (deve retornar 403)

```bash
curl -i 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer invalid.token.xyz"
```

**Esperado:** HTTP 403 Forbidden

### 3. Requisição com token EXPIRADO (deve retornar 403)

```bash
# Espere o token expirar (padrão: 1 hora) ou force a expiração editando o token JWT

curl -i 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2MzIxMjM0NTZ9.xyz"
```

**Esperado:** HTTP 403 Forbidden

---

## 🔍 Testes de CORS

### 1. Preflight request (OPTIONS)

```bash
curl -i -X OPTIONS 'http://localhost:8080/api/pracas' \
  -H 'Origin: http://localhost:3000' \
  -H 'Access-Control-Request-Method: POST' \
  -H 'Access-Control-Request-Headers: Authorization, Content-Type'
```

**Esperado:** HTTP 200 com headers CORS:
```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
Access-Control-Allow-Headers: Authorization, Content-Type
```

### 2. Requisição do frontend (simulada)

```bash
# Simular como o frontend faz a requisição
curl -i 'http://localhost:8080/api/pracas' \
  -H 'Origin: http://localhost:3000' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json'
```

**Esperado:** HTTP 200 com header `Access-Control-Allow-Origin`

---

## 📊 Exemplo Completo de Workflow

```bash
#!/bin/bash

# 1. Login
echo "1️⃣  Fazendo login..."
LOGIN=$(curl -s -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password"}')

TOKEN=$(echo $LOGIN | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo "Token: ${TOKEN:0:50}..."
echo ""

# 2. Listar praças (antes)
echo "2️⃣  Listando praças (antes)..."
curl -s 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""

# 3. Criar nova praça
echo "3️⃣  Criando nova praça..."
CREATED=$(curl -s -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "nome": "Praça Test",
    "logradouro": "Rua Test, 123",
    "bairro": "Bairro Test",
    "cidade": "Cidade Test",
    "latitude": -23.5,
    "longitude": -46.6,
    "descricao": "Praça de teste",
    "fotoUrl": "https://exemplo.com/test.jpg",
    "status": "ATIVA"
  }')

PRACA_ID=$(echo $CREATED | grep -o '"id":[0-9]*' | cut -d':' -f2)
echo "Criada praça ID: $PRACA_ID"
echo ""

# 4. Listar praças (depois)
echo "4️⃣  Listando praças (depois)..."
curl -s 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""

# 5. Atualizar praça
echo "5️⃣  Atualizando praça..."
curl -s -X PUT "http://localhost:8080/api/pracas/$PRACA_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "nome": "Praça Test - Atualizada",
    "logradouro": "Rua Test, 123",
    "bairro": "Bairro Test",
    "cidade": "Cidade Test",
    "latitude": -23.5,
    "longitude": -46.6,
    "descricao": "Praça de teste atualizada",
    "fotoUrl": "https://exemplo.com/test2.jpg",
    "status": "ATIVA"
  }' | jq .
echo ""

# 6. Deletar praça
echo "6️⃣  Deletando praça..."
curl -i -X DELETE "http://localhost:8080/api/pracas/$PRACA_ID" \
  -H "Authorization: Bearer $TOKEN"
echo ""

echo "✅ Workflow completo!"
```

---

## 🛠️ Troubleshooting

### Problema: "HTTP 403 Forbidden"

```bash
# Verificar:
1. Token foi gerado? (Copiar de /api/auth/login)
2. Token está correto? (echo $TOKEN)
3. Header está correto? (-H "Authorization: Bearer $TOKEN")
4. Token expirou? (Deve ser < 1 hora)

# Solução:
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password"}'
```

### Problema: "Connection refused"

```bash
# Verificar:
1. Servidor está rodando? (ps aux | grep spring-boot)
2. Porta 8080 está aberta? (lsof -i :8080)
3. IP está correto? (localhost ou 127.0.0.1)

# Solução:
# Reiniciar servidor
pkill -f "spring-boot:run"
./mvnw spring-boot:run -DskipTests=true
```

### Problema: "jq: command not found"

```bash
# Instalar jq:
# macOS:
brew install jq

# Linux:
sudo apt-get install jq

# Ou usar grep em vez de jq:
curl -s 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" | grep -o '"nome":"[^"]*'
```


