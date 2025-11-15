# ✅ CHECKLIST: Verificação das Mudanças

## 📝 Arquivos Modificados

- [x] `src/main/resources/application.properties`
  - [x] jwt.secret.key atualizada para Base64 válido (256 bits)
  - [x] Outras configurações intactas

- [x] `src/main/java/br/senai/sc/communitex/config/SecurityConfig.java`
  - [x] Imports adicionados (CorsConfiguration, CorsConfigurationSource, etc)
  - [x] Bean corsConfigurationSource() criado
  - [x] SecurityFilterChain atualizado para usar CORS customizado
  - [x] Métodos HTTP: GET, POST, PUT, DELETE, PATCH, OPTIONS
  - [x] Origens permitidas: localhost:3000, 8080, 5173
  - [x] Credentials habilitadas

- [x] `src/main/java/br/senai/sc/communitex/config/JwtAuthenticationFilter.java`
  - [x] Import Logger adicionado
  - [x] Logger estático criado
  - [x] Try-catch envolvendo todo o processamento de token
  - [x] Logs adicionados para sucesso e erro
  - [x] Sem mudanças na lógica core

---

## 🧪 Testes de Compilação

```bash
# ✅ VERIFICAR: Projeto compila sem erros
./mvnw clean compile -DskipTests=true -q
# Resultado esperado: Exit code 0

# ✅ VERIFICAR: Projeto constrói JAR sem erros
./mvnw clean package -DskipTests=true -q
# Resultado esperado: communitex-0.0.1-SNAPSHOT.jar criado

# ✅ VERIFICAR: Nenhum erro de segurança
./mvnw clean compile -Pcheck-dependencies
# Resultado esperado: Sem vulnerabilidades críticas
```

---

## 🚀 Testes de Runtime

### Iniciando o Servidor

```bash
# ✅ Parar instâncias anteriores
pkill -f "spring-boot:run"
sleep 2

# ✅ Compilar (se necessário)
./mvnw clean compile -DskipTests=true -q

# ✅ Iniciar servidor
./mvnw spring-boot:run -DskipTests=true

# ✅ VERIFICAR: Logs de inicialização
# Procurar por:
# - "Usuário admin criado!" (primeira execução)
# - "Servidor iniciado em xxxx ms"
# - Nenhum erro de JWT ou CORS
```

### Verificação de Banco de Dados

```bash
# ✅ Verificar que a tabela usuarios existe
# (Se usando H2)
# Acessar: http://localhost:8080/h2-console

# ✅ Verificar que admin existe
SELECT * FROM usuarios WHERE username='admin';
# Resultado esperado: 1 linha com username='admin'
```

---

## 🔐 Testes de Autenticação

### Teste 1: Login com Credenciais Corretas

```bash
# ✅ EXECUTAR
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password"}'

# ✅ VERIFICAR RESPOSTA
# - Status: 200
# - Body contém: "accessToken"
# - Body contém: "refreshToken"
# - Ambos são JWTs válidos (eyJhbGciOi...)

# ✅ SALVAR TOKEN
TOKEN="eyJ..." # Copiar do accessToken acima
```

### Teste 2: Login com Credenciais Incorretas

```bash
# ✅ EXECUTAR
curl -i -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"senha_errada"}'

# ✅ VERIFICAR RESPOSTA
# - Status: 401 Unauthorized OU 403 Forbidden
# - Sem accessToken na resposta
```

---

## 📊 Testes de API Endpoints

### Teste 3: GET /api/pracas SEM Token

```bash
# ✅ EXECUTAR
curl -i 'http://localhost:8080/api/pracas'

# ✅ VERIFICAR RESPOSTA
# - Status: 403 Forbidden (sem token)
# - OU 401 Unauthorized
```

### Teste 4: GET /api/pracas COM Token Válido

```bash
# ✅ EXECUTAR
curl -i 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN"

# ✅ VERIFICAR RESPOSTA
# - Status: 200 OK ✅✅✅ (ESTE ERA O ERRO 403)
# - Body: [] (array vazio, que é válido)
# - OU Body com dados se houver praças cadastradas
```

### Teste 5: GET /api/pracas COM Token Inválido

```bash
# ✅ EXECUTAR
curl -i 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer invalid_token_xyz"

# ✅ VERIFICAR RESPOSTA
# - Status: 403 Forbidden
```

### Teste 6: POST /api/pracas COM Token Válido

```bash
# ✅ EXECUTAR
curl -i -X POST 'http://localhost:8080/api/pracas' \
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

# ✅ VERIFICAR RESPOSTA
# - Status: 201 Created ✅✅✅ (ANTES ERA 403)
# - Body: JSON com id, nome, etc
# - Campo "id" presente (ID gerado)
```

### Teste 7: GET /api/pracas COM Token Válido (Após POST)

```bash
# ✅ EXECUTAR
curl -i 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN"

# ✅ VERIFICAR RESPOSTA
# - Status: 200 OK
# - Body: Array com ao menos 1 praça criada no Teste 6
```

### Teste 8: PUT /api/pracas/1

```bash
# ✅ EXECUTAR
curl -i -X PUT 'http://localhost:8080/api/pracas/1' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "nome": "Praça Teste - Atualizada",
    "logradouro": "Rua Teste, 123",
    "bairro": "Bairro",
    "cidade": "Cidade",
    "latitude": -23.5,
    "longitude": -46.6,
    "descricao": "Teste atualizado",
    "fotoUrl": "https://exemplo.com/img2.jpg",
    "status": "ATIVA"
  }'

# ✅ VERIFICAR RESPOSTA
# - Status: 200 OK
# - Body: JSON com dados atualizados
```

### Teste 9: DELETE /api/pracas/1

```bash
# ✅ EXECUTAR
curl -i -X DELETE 'http://localhost:8080/api/pracas/1' \
  -H "Authorization: Bearer $TOKEN"

# ✅ VERIFICAR RESPOSTA
# - Status: 204 No Content
```

---

## 🌐 Testes de CORS

### Teste 10: Preflight Request (OPTIONS)

```bash
# ✅ EXECUTAR
curl -i -X OPTIONS 'http://localhost:8080/api/pracas' \
  -H 'Origin: http://localhost:3000' \
  -H 'Access-Control-Request-Method: POST' \
  -H 'Access-Control-Request-Headers: Authorization, Content-Type'

# ✅ VERIFICAR RESPOSTA HEADERS
# - Access-Control-Allow-Origin: http://localhost:3000
# - Access-Control-Allow-Methods: GET, POST, PUT, DELETE, ...
# - Access-Control-Allow-Headers: *
# - Access-Control-Allow-Credentials: true
# - Status: 200 OK
```

### Teste 11: CORS com Origem Não Permitida

```bash
# ✅ EXECUTAR
curl -i 'http://localhost:8080/api/pracas' \
  -H 'Origin: http://origem-nao-permitida.com' \
  -H "Authorization: Bearer $TOKEN"

# ✅ VERIFICAR RESPOSTA
# - Access-Control-Allow-Origin: NÃO presente
# - OU Access-Control-Allow-Origin: null
```

---

## 📋 Verificação de Logs

### Depois de executar os testes acima

```bash
# ✅ VERIFICAR em tempo real
tail -f /tmp/spring-boot.log

# ✅ PROCURAR POR:
# [OK] "Autenticação JWT bem-sucedida para usuário: admin"
# [OK] Nenhum "Erro ao processar token JWT"
# [OK] Nenhum erro de CORS
# [OK] Status 200, 201, 204 nos endpoints

# ✅ NÃO DEVE HAVER:
# [ERRO] "SignatureException"
# [ERRO] "MalformedJwtException"
# [ERRO] "CORS"
# [AVISO] "Token JWT inválido" (para token válido)
```

---

## 📊 Checklist de Validação Final

| Item | Status | Nota |
|------|--------|------|
| Projeto compila sem erros | ✅ | `./mvnw clean compile` |
| JWT com chave válida (256 bits) | ✅ | Base64 decodificado |
| JwtAuthenticationFilter com try-catch | ✅ | Logger adicionado |
| CORS customizado criado | ✅ | Bean configurado |
| Login retorna 200 com token | ✅ | Token válido |
| GET sem token retorna 403 | ✅ | Segurança OK |
| GET com token retorna 200 | ✅ | **ANTES ERA 403** ✅ |
| POST com token retorna 201 | ✅ | **ANTES ERA 403** ✅ |
| PUT com token retorna 200 | ✅ | **ANTES ERA 403** ✅ |
| DELETE com token retorna 204 | ✅ | **ANTES ERA 403** ✅ |
| OPTIONS preflight retorna 200 | ✅ | CORS funcionando |
| Logs informativos aparecem | ✅ | Debug facilitado |

---

## 🎉 Resultado Final

Se todos os testes passarem:

```
✅ PROBLEMA RESOLVIDO!

ANTES:
- curl com token → HTTP 403 Forbidden ❌

DEPOIS:
- curl com token → HTTP 200 OK ✅
- POST → HTTP 201 Created ✅
- PUT → HTTP 200 OK ✅
- DELETE → HTTP 204 No Content ✅
```

---

## 📞 Se Ainda Tiver Problemas

1. **Verificar logs:**
   ```bash
   tail -50 /tmp/spring-boot.log
   ```

2. **Decodificar token em:** https://jwt.io
   - Verificar se "alg" é HS256
   - Verificar se "sub" é "admin"
   - Verificar expiração ("exp")

3. **Validar chave JWT:**
   ```bash
   echo "dGhpcyBpcyBhIHZlcnkgbG9uZyBhbmQgc2VjdXJlIGpzb24gd2ViIHRva2VuIHNlY3JldCBrZXkgZm9yIEhTMjU2IGFsZ29yaXRobQ==" | base64 -d
   ```
   - Deve retornar: "this is a very long and secure json web token secret key for HS256 algorithm"

4. **Testar com curl verbose:**
   ```bash
   curl -v 'http://localhost:8080/api/pracas' \
     -H "Authorization: Bearer $TOKEN"
   ```

5. **Verificar se porta 8080 está em uso:**
   ```bash
   lsof -i :8080
   ```


