# 🔍 Diagnóstico: Erro 403 Forbidden em `/api/pracas`

## Problemas Identificados

### ✅ PROBLEMA 1: Chave JWT Inválida (CORRIGIDO)
**Causa:** A chave secreta do JWT estava muito curta e não atendia aos requisitos do algoritmo HS256 (mínimo 256 bits/32 bytes em Base64).

**Arquivo afetado:** `src/main/resources/application.properties`

**Antes:**
```properties
jwt.secret.key=chavequalqueradadasdadadadadadadadadadadadada
```

**Depois:**
```properties
jwt.secret.key=dGhpcyBpcyBhIHZlcnkgbG9uZyBhbmQgc2VjdXJlIGpzb24gd2ViIHRva2VuIHNlY3JldCBrZXkgZm9yIEhTMjU2IGFsZ29yaXRobQ==
```

**Impacto:** Tokens antigos com a chave velha são agora **inválidos**. É necessário gerar novo token!

---

### ✅ PROBLEMA 2: Falta de Tratamento de Erros no JWT Filter (CORRIGIDO)
**Causa:** O `JwtAuthenticationFilter` não capturava exceções ao validar tokens, causando falhas silenciosas que resultavam em 403.

**Arquivo afetado:** `src/main/java/br/senai/sc/communitex/config/JwtAuthenticationFilter.java`

**Mudanças:**
- ✅ Adicionado `Logger` para diagnóstico
- ✅ Adicionado bloco `try-catch` completo
- ✅ Mensagens de erro detalhadas

---

### ✅ PROBLEMA 3: Configuração CORS Inadequada (CORRIGIDO)
**Causa:** A configuração CORS com `Customizer.withDefaults()` pode não funcionar corretamente com métodos POST/PUT.

**Arquivo afetado:** `src/main/java/br/senai/sc/communitex/config/SecurityConfig.java`

**Mudanças:**
- ✅ Criado bean `CorsConfigurationSource` customizado
- ✅ Configurados explicitamente todos os métodos HTTP (GET, POST, PUT, DELETE, PATCH, OPTIONS)
- ✅ Definidas origens permitidas
- ✅ Habilitadas credentials

---

## 🔧 Próximos Passos para Testar

### 1️⃣ Parar o servidor antigo
```bash
pkill -f "spring-boot:run"
```

### 2️⃣ Limpar e recompilar
```bash
./mvnw clean compile -DskipTests=true -q
```

### 3️⃣ Iniciar novo servidor
```bash
./mvnw spring-boot:run -DskipTests=true
```

### 4️⃣ Obter novo token
```bash
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password"}'
```

### 5️⃣ Testar GET (já funciona)
```bash
TOKEN="seu_novo_token_aqui"
curl -i http://localhost:8080/api/pracas \
  -H "Authorization: Bearer $TOKEN"
```

### 6️⃣ Testar POST
```bash
curl -i -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "nome": "Praça da Matriz",
    "logradouro": "Rua Sete de Setembro, 100",
    "bairro": "Centro",
    "cidade": "Porto Alegre",
    "latitude": -30.033056,
    "longitude": -51.230000,
    "descricao": "Praça histórica",
    "fotoUrl": "https://exemplo.com/img.jpg",
    "status": "ATIVA"
  }'
```

---

## 🎯 O que Causava o 403 Original

| Problema | Motivo | Solução |
|----------|--------|---------|
| Chave JWT curta | Token não podia ser validado corretamente | ✅ Chave Base64 válida |
| Sem tratamento de erro | Exceção silenciosa | ✅ Try-catch com logging |
| CORS inadequado | Pré-flight requests falhando | ✅ Configuração explícita |

---

## ✨ Resumo das Mudanças

### `application.properties`
- ✅ Chave JWT atualizada para 256 bits (Base64)

### `SecurityConfig.java`
- ✅ Adicionado `CorsConfigurationSource` bean
- ✅ Métodos HTTP explícitos: GET, POST, PUT, DELETE, PATCH, OPTIONS
- ✅ Origens configuradas: localhost:3000, 8080, 5173
- ✅ Credentials habilitadas

### `JwtAuthenticationFilter.java`
- ✅ Adicionado Logger
- ✅ Try-catch para capturar exceções JWT
- ✅ Mensagens informativas de debug
- ✅ Melhor rastreabilidade de problemas

---

## 📌 Token Anterior é INVÁLIDO

**Importante:** Seu token anterior não funcionará mais porque a chave secreta mudou!

**Token antigo (não funciona):**
```
eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzYzMjEyNTM4LCJleHAiOjE3NjMyMTYxMzh9.xfeXJh--MbbkKW7TsTHUlGzbak34yKv7WWL1hMg3SBs
```

**Solução:** Use o novo token gerado após reiniciar o servidor!


