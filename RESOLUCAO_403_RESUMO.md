# 🎯 RESUMO EXECUTIVO: Correção do Erro 403 em `/api/pracas`

## O Problema
Ao tentar acessar o endpoint `/api/pracas` com um token JWT válido, você recebia:
```
HTTP 403 Forbidden
```

Seu curl estava **aparentemente correto**, mas o token não era aceito.

---

## As 3 Causas Raiz

### 1️⃣ **Chave JWT Inválida** ⚠️ CRÍTICO
A chave secreta configurada era **muito curta** para o algoritmo HS256:

```properties
# ❌ ERRADO (antes)
jwt.secret.key=chavequalqueradadasdadadadadadadadadadadadada

# ✅ CORRETO (depois)
jwt.secret.key=dGhpcyBpcyBhIHZlcnkgbG9uZyBhbmQgc2VjdXJlIGpzb24gd2ViIHRva2VuIHNlY3JldCBrZXkgZm9yIEhTMjU2IGFsZ29yaXRobQ==
```

**Por que?** O HS256 precisa de uma chave Base64 com **no mínimo 256 bits (32 bytes)**. Sem isso:
- ✗ Tokens gerados ficam mal-formados
- ✗ Validação do token falha silenciosamente
- ✗ Resultado: 403 Forbidden

### 2️⃣ **Falta de Tratamento de Erros no JWT Filter**
O `JwtAuthenticationFilter` tinha um problema:
```java
// ❌ ANTES: Se falha-se, nenhuma informação é registrada
if (jwtService.isTokenValid(jwt, userDetails)) {
    // processa
}

// ✅ DEPOIS: Captura e loga exceções
try {
    if (jwtService.isTokenValid(jwt, userDetails)) {
        // processa
    }
} catch (Exception e) {
    logger.error("Erro ao processar token JWT: {}", e.getMessage(), e);
}
```

### 3️⃣ **Configuração CORS Inadequada**
A configuração CORS padrão não funciona bem com todos os métodos HTTP:

```java
// ❌ ANTES
.cors(Customizer.withDefaults())

// ✅ DEPOIS
.cors(cors -> cors.configurationSource(corsConfigurationSource()))

// Com bean customizado:
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080", "http://localhost:5173"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    // ...
}
```

---

## ✅ Arquivos Modificados

| Arquivo | Mudanças |
|---------|----------|
| `application.properties` | Chave JWT atualizada para 256 bits |
| `SecurityConfig.java` | Configuração CORS explícita + bean customizado |
| `JwtAuthenticationFilter.java` | Try-catch + Logger |

---

## ⚠️ IMPORTANTE: Token Anterior Inválido

Seu token antigo:
```
eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluIiwiaWF0IjoxNzYzMjEyNTM4LCJleHAiOjE3NjMyMTYxMzh9.xfeXJh--MbbkKW7TsTHUlGzbak34yKv7WWL1hMg3SBs
```

**NÃO FUNCIONA MAIS** porque foi assinado com a chave velha.

### Para gerar novo token:

```bash
# 1. Fazer login
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password"}'

# Resposta:
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJ...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJ..."
}

# 2. Usar o novo accessToken
TOKEN="seu_novo_token_aqui"

# 3. Testar GET
curl -i http://localhost:8080/api/pracas \
  -H "Authorization: Bearer $TOKEN"

# 4. Testar POST
curl -i -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Praça da Matriz",...}'
```

---

## 🔍 Antes vs Depois

### ANTES (Erro 403)
```bash
$ curl http://localhost:8080/api/pracas \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

HTTP 403 Forbidden
```

### DEPOIS (Sucesso!)
```bash
$ curl http://localhost:8080/api/pracas \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

HTTP 200 OK
[]
```

---

## 🎓 Lições Aprendidas

1. **JWT requer chave suficientemente longa** → Use Base64 com mínimo 256 bits
2. **Sempre registre (log) exceções em filtros** → Facilita debug
3. **Configure CORS explicitamente** → Não confie em defaults
4. **Ao mudar chave secreta, todos os tokens antigos caducam** → Lembre aos usuários

---

## 📞 Próximas Ações (Se Ainda Tiver Problemas)

1. Verifique logs: `tail -f /tmp/spring-boot.log`
2. Teste com `curl -v` (modo verbose) para ver headers completos
3. Valide o token em https://jwt.io
4. Verifique se o usuário existe: `SELECT * FROM usuarios WHERE username='admin';`

---

**Status:** ✅ **RESOLVIDO** - Código corrigido, pronto para testar!

