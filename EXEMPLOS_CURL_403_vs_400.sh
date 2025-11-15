#!/bin/bash

# 📋 EXEMPLOS PRÁTICOS: Reproduzindo 403 vs 400 Bad Request
# Execute este arquivo ou copie os comandos

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║          📋 EXEMPLOS DE CURL: 403 vs 400 Bad Request             ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
echo ""

# PASSO 1: Fazer Login e Obter Token
echo "PASSO 1️⃣: Obtendo token de autenticação..."
echo ""
echo "Comando:"
echo "--------"
echo "curl -X POST 'http://localhost:8080/api/auth/login' \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"username\":\"admin\",\"password\":\"password\"}'"
echo ""
echo "Resposta esperada:"
echo "{\"accessToken\":\"eyJ...\",\"refreshToken\":\"eyJ...\"}"
echo ""
echo "👉 Copie o valor de accessToken na variável abaixo:"
echo ""
read -p "Cole o token aqui (ou pressione Enter para um exemplo): " TOKEN

if [ -z "$TOKEN" ]; then
    TOKEN="eyJhbGciOiJIUzI1NiJ9.exemplo"
    echo "Usando token de exemplo para demonstração"
fi

echo ""
echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║                   TESTE 1: SEM TOKEN (403)                        ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
echo ""
echo "Comando:"
echo "--------"
echo "curl -i -X POST 'http://localhost:8080/api/pracas' \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"nome\": \"\", \"latitude\": 999}'"
echo ""
echo "O que acontece:"
echo "• SEM header Authorization"
echo "• Spring Security intercepta"
echo "• Retorna: ❌ HTTP 403 Forbidden"
echo "• Nunca chega no controlador"
echo ""
read -p "Pressione Enter para executar..."
curl -i -X POST 'http://localhost:8080/api/pracas' \
  -H 'Content-Type: application/json' \
  -d '{"nome": "", "latitude": 999}' 2>/dev/null | head -20
echo ""
echo ""

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║         TESTE 2: COM TOKEN + DADOS INVÁLIDOS (400)               ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
echo ""
echo "Comando:"
echo "--------"
echo "TOKEN=\"seu_token_aqui\""
echo "curl -i -X POST 'http://localhost:8080/api/pracas' \\"
echo "  -H \"Authorization: Bearer \$TOKEN\" \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"nome\": \"\", \"latitude\": 999}'"
echo ""
echo "O que acontece:"
echo "• COM header Authorization válido"
echo "• Spring Security deixa passar"
echo "• Controlador recebe a requisição"
echo "• @Valid detecta erros:"
echo "  - nome: vazio ❌"
echo "  - latitude: 999 (fora do range -90 a 90) ❌"
echo "• Exception handler intercepta"
echo "• Retorna: ✅ HTTP 400 Bad Request"
echo ""
read -p "Pressione Enter para executar..."
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
    "status": "INVALIDO"
  }' 2>/dev/null | head -30
echo ""
echo ""

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║         TESTE 3: COM TOKEN + DADOS VÁLIDOS (201)                 ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
echo ""
echo "Comando:"
echo "--------"
echo "TOKEN=\"seu_token_aqui\""
echo "curl -i -X POST 'http://localhost:8080/api/pracas' \\"
echo "  -H \"Authorization: Bearer \$TOKEN\" \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"nome\": \"Praça Válida\", \"latitude\": -23.5, ...}'"
echo ""
echo "O que acontece:"
echo "• COM header Authorization válido"
echo "• Spring Security deixa passar ✅"
echo "• Controlador recebe a requisição ✅"
echo "• @Valid valida com sucesso ✅"
echo "• Serviço processa a criação ✅"
echo "• Retorna: ✅ HTTP 201 Created"
echo ""
read -p "Pressione Enter para executar..."
curl -i -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "nome": "Praça Válida",
    "logradouro": "Rua Teste, 123",
    "bairro": "Bairro Teste",
    "cidade": "Cidade Teste",
    "latitude": -23.5,
    "longitude": -46.6,
    "descricao": "Praça de teste criada com sucesso",
    "fotoUrl": "https://exemplo.com/img.jpg",
    "status": "ATIVA"
  }' 2>/dev/null | head -30
echo ""
echo ""

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║         TESTE 4: COM TOKEN INVÁLIDO (403)                        ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
echo ""
echo "Comando:"
echo "--------"
echo "curl -i -X POST 'http://localhost:8080/api/pracas' \\"
echo "  -H 'Authorization: Bearer token_invalido_xyz' \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"nome\": \"Praça\", ...}'"
echo ""
echo "O que acontece:"
echo "• Token presente mas INVÁLIDO"
echo "• Spring Security intercepta"
echo "• Valida assinatura do JWT → ❌ Falha"
echo "• Retorna: HTTP 403 Forbidden (correto!)"
echo "• Nunca chega no controlador"
echo ""
read -p "Pressione Enter para executar..."
curl -i -X POST 'http://localhost:8080/api/pracas' \
  -H 'Authorization: Bearer token_invalido_xyz' \
  -H 'Content-Type: application/json' \
  -d '{"nome": "Praça", "logradouro": "Rua", "bairro": "Bairro", "cidade": "Cidade", "latitude": -23.5, "longitude": -46.6, "descricao": "OK", "fotoUrl": "https://exemplo.com/img.jpg", "status": "ATIVA"}' 2>/dev/null | head -20
echo ""
echo ""

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║                   RESUMO DOS TESTES                               ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
echo ""
echo "┌─ CENÁRIO 1: SEM TOKEN ───────────────────────────────────────────┐"
echo "│ Resultado: ❌ HTTP 403 Forbidden                                 │"
echo "│ Motivo: Spring Security bloqueia sem autenticação                │"
echo "│ Seu código: ❌ Nunca executado                                   │"
echo "└──────────────────────────────────────────────────────────────────┘"
echo ""
echo "┌─ CENÁRIO 2: COM TOKEN + DADOS INVÁLIDOS ─────────────────────────┐"
echo "│ Resultado: ✅ HTTP 400 Bad Request (AGORA FUNCIONA!)            │"
echo "│ Motivo: @Valid detecta erro de validação                        │"
echo "│ Seu código: ✅ Exception handler intercepta                     │"
echo "└──────────────────────────────────────────────────────────────────┘"
echo ""
echo "┌─ CENÁRIO 3: COM TOKEN + DADOS VÁLIDOS ──────────────────────────┐"
echo "│ Resultado: ✅ HTTP 201 Created                                  │"
echo "│ Motivo: Tudo OK, recurso criado com sucesso                    │"
echo "│ Seu código: ✅ Controlador processa normalmente                │"
echo "└──────────────────────────────────────────────────────────────────┘"
echo ""
echo "┌─ CENÁRIO 4: COM TOKEN INVÁLIDO ──────────────────────────────────┐"
echo "│ Resultado: ❌ HTTP 403 Forbidden (esperado!)                    │"
echo "│ Motivo: Token inválido, JWT não passa na validação             │"
echo "│ Seu código: ❌ Nunca executado                                  │"
echo "└──────────────────────────────────────────────────────────────────┘"
echo ""

echo "═══════════════════════════════════════════════════════════════════════"
echo "✨ Conclusão: O erro 403 ocorre ANTES do seu código. Use tokens válidos!"
echo "═══════════════════════════════════════════════════════════════════════"

