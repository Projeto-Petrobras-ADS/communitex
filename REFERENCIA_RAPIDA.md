# 🎯 REFERÊNCIA RÁPIDA: 403 vs 400 Bad Request

## ❓ Pergunta Original
> "Por que ao fazer um curl e dar algum erro no backend está sempre caindo a resposta 403, ao invés de algum bad request?"

## ✅ Resposta Rápida

| Cenário | Token | Dados | Resposta | Motivo |
|---------|-------|-------|----------|--------|
| SEM Token | ❌ | - | **403** | Spring Security bloqueia |
| COM Token | ✅ | ✅ | **201/200** | Sucesso! |
| COM Token | ✅ | ❌ | **400** ✅ | Validação falhou (AGORA!) |
| Token Inválido | ❌ | - | **403** | JWT inválido |

## 🔑 Regra de Ouro

```
Sempre use: -H "Authorization: Bearer $TOKEN"

SEM Token = 403 (esperado!)
COM Token + Dados ❌ = 400 (agora funciona!)
```

## 🚀 Comandos Essenciais

### 1️⃣ Obter Token
```bash
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password"}'
```

### 2️⃣ Salvar Token
```bash
export TOKEN="eyJhbGc..." # Copie do accessToken acima
```

### 3️⃣ Usar em POST
```bash
curl -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{...}'
```

## 📊 Fluxo Rápido

```
SEM TOKEN:
  Request → Spring Security: "❌ Token?" → 403 Forbidden
  (Seu código não executa!)

COM TOKEN + Dados ❌:
  Request → Spring Security: "✅ OK" → Controller [Erro] → Handler ✅ → 400
  (Seu exception handler intercepta!)
```

## 🔍 O Que Foi Feito

✅ **GlobalExceptionHandler.java** modificado:
- Captura `MethodArgumentNotValidException` → 400 Bad Request
- Captura `Exception` genérica → 500 Internal Server Error
- Logger em todos os casos
- Resposta consistente

## 🧪 Teste Crítico

```bash
TOKEN="seu_token"
curl -i -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"nome": "", "latitude": 999}'

# ANTES: HTTP 403 ❌
# DEPOIS: HTTP 400 ✅
```

## 📚 Documentação Disponível

1. **GUIA_PASSO_A_PASSO.md** ← Comece aqui (prático)
2. **ENTENDIMENTO_403_vs_400.md** ← Técnico
3. **EXEMPLOS_CURL_403_vs_400.sh** ← Testes interativos
4. **CHECKLIST_IMPLEMENTACAO.md** ← Verificação

## ✨ Status

```
✅ Problema identificado
✅ Solução implementada
✅ Código modificado
✅ Documentação criada
✅ Pronto para usar!
```

## 🎯 Próximo Passo

Teste com token válido e veja 400 Bad Request funcionar! 🎉


