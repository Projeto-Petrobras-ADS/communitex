# 📋 DETALHES TÉCNICOS: Mudanças Implementadas

## 1. Atualização da Chave JWT (application.properties)

**Arquivo:** `src/main/resources/application.properties`

### Problema
A chave original era um string simples:
```properties
jwt.secret.key=chavequalqueradadasdadadadadadadadadadadadada
```

**Por que é inválido:**
- Tamanho: ~36 caracteres
- HS256 precisa: 256 bits = 32 bytes = ~43 caracteres em Base64
- Decoificação falha ao tentar usar `Decoders.BASE64.decode()`

### Solução
Substituir por uma chave Base64 válida com 256 bits:
```properties
jwt.secret.key=dGhpcyBpcyBhIHZlcnkgbG9uZyBhbmQgc2VjdXJlIGpzb24gd2ViIHRva2VuIHNlY3JldCBrZXkgZm9yIEhTMjU2IGFsZ29yaXRobQ==
```

**Decodificação:**
```
Base64 Decoded: "this is a very long and secure json web token secret key for HS256 algorithm"
Comprimento: 80 caracteres UTF-8 = 640 bits > 256 bits ✅
```

---

## 2. Melhoria do JwtAuthenticationFilter

**Arquivo:** `src/main/java/br/senai/sc/communitex/config/JwtAuthenticationFilter.java`

### ANTES
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // ...

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String jwt = authHeader.substring(7);
        final String username = jwtService.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

**Problemas:**
- ❌ Sem tratamento de exceção
- ❌ Se `jwtService.extractUsername(jwt)` falha com `SignatureException`, a exceção é silenciosa
- ❌ Impossível debugar - nenhum log

### DEPOIS
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // ...

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            final String jwt = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Autenticação JWT bem-sucedida para usuário: {}", username);
                } else {
                    logger.warn("Token JWT inválido para usuário: {}", username);
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao processar token JWT: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}
```

**Melhorias:**
- ✅ Try-catch para capturar `SignatureException`, `MalformedJwtException`, `ExpiredJwtException`, etc
- ✅ Logger para rastreamento
- ✅ Mensagens informativas de sucesso/erro

---

## 3. Configuração CORS Melhorada

**Arquivo:** `src/main/java/br/senai/sc/communitex/config/SecurityConfig.java`

### ANTES
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())  // ❌ Configuração padrão
            // ...
```

**Problema:**
- `Customizer.withDefaults()` usa configuração CORS mínima
- Pode não funcionar bem com préflight requests (OPTIONS) em métodos POST/PUT
- Não define explicitamente quais métodos HTTP são permitidos

### DEPOIS

#### Passo 1: Adicionar imports
```java
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
```

#### Passo 2: Criar bean de configuração CORS
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // Definir origens permitidas
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",    // React/Vue frontend
        "http://localhost:8080",    // Mesmo servidor
        "http://localhost:5173"     // Vite dev server
    ));
    
    // Definir métodos HTTP permitidos
    configuration.setAllowedMethods(Arrays.asList(
        "GET",
        "POST",
        "PUT",
        "DELETE",
        "OPTIONS",
        "PATCH"
    ));
    
    // Permitir todos os headers (ou ser mais restritivo se necessário)
    configuration.setAllowedHeaders(Arrays.asList("*"));
    
    // Permitir credentials (cookies, authorization headers)
    configuration.setAllowCredentials(true);
    
    // Cache de preflight por 1 hora
    configuration.setMaxAge(3600L);

    // Registrar para todos os paths
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

#### Passo 3: Usar a configuração no SecurityFilterChain
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // ✅ Customizado
            // ...
}
```

**Melhorias:**
- ✅ CORS explicitamente configurado
- ✅ Suporta preflight requests (OPTIONS)
- ✅ Métodos HTTP definidos claramente
- ✅ Permite credentials (Authorization header)

---

## 4. Resumo das Mudanças na Estrutura

```
src/main/java/br/senai/sc/communitex/config/
├── SecurityConfig.java          ✏️ MODIFICADO
│   ├── Adicionado: CorsConfiguration bean
│   ├── Adicionado: CorsConfigurationSource bean
│   └── Modificado: securityFilterChain()
│
└── JwtAuthenticationFilter.java  ✏️ MODIFICADO
    ├── Adicionado: Logger
    ├── Adicionado: try-catch
    └── Adicionado: Log messages

src/main/resources/
└── application.properties         ✏️ MODIFICADO
    └── jwt.secret.key atualizada
```

---

## 5. Fluxo de Autenticação (APÓS correções)

```
1. Cliente faz POST /api/auth/login
   ├─ Envia: {"username": "admin", "password": "password"}
   └─ Recebe: {"accessToken": "eyJ...", "refreshToken": "eyJ..."}

2. Cliente armazena accessToken

3. Cliente faz GET /api/pracas
   ├─ Header: Authorization: Bearer eyJ...
   └─ Fluxo de validação:
      ├─ CorsFilter (preflight se necessário)
      ├─ SecurityFilterChain passa header ao JwtAuthenticationFilter
      ├─ JwtAuthenticationFilter:
      │  ├─ Extrai Bearer token
      │  ├─ Chama jwtService.extractUsername(jwt)
      │  │  └─ Decodifica usando chave Base64 válida ✅
      │  ├─ Carrega UserDetails do banco
      │  ├─ Chama jwtService.isTokenValid(jwt, userDetails)
      │  │  └─ Valida assinatura e expiração ✅
      │  └─ Se válido: Define SecurityContext com authorities
      ├─ Controlador processa a requisição
      └─ Retorna 200 OK com dados

4. Se token for inválido:
   ├─ logger.warn("Token JWT inválido...")
   └─ SecurityContext não é definido → 403 Forbidden
```

---

## 6. Testes Recomendados

### Teste 1: Obter novo token
```bash
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password"}' \
  -w '\nHTTP Status: %{http_code}\n'
```

**Esperado:** HTTP 200 com accessToken

### Teste 2: GET /api/pracas
```bash
TOKEN="<seu_token_aqui>"
curl -i 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN"
```

**Esperado:** HTTP 200 com array JSON (vazio ou com dados)

### Teste 3: POST /api/pracas
```bash
TOKEN="<seu_token_aqui>"
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

**Esperado:** HTTP 201 com dados da praça criada

### Teste 4: Preflight CORS
```bash
curl -i -X OPTIONS 'http://localhost:8080/api/pracas' \
  -H 'Origin: http://localhost:3000' \
  -H 'Access-Control-Request-Method: POST' \
  -H 'Access-Control-Request-Headers: Authorization'
```

**Esperado:** HTTP 200 com headers CORS apropriados

---

## 7. Verificação de Segurança

```bash
# Verificar se token expirado é rejeitado
# (Espere token expirar, padrão: 1 hora)
curl -i 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN_EXPIRADO"
# Esperado: HTTP 403

# Verificar se token inválido é rejeitado
curl -i 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer invalid.token.here"
# Esperado: HTTP 403

# Verificar se ausência de token é rejeitada
curl -i 'http://localhost:8080/api/pracas'
# Esperado: HTTP 403 ou 401
```


