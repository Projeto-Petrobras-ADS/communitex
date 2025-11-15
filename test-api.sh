#!/bin/bash

# Script para testar a API de Praças após correção do JWT

set -e

echo "🚀 Script de Teste da API de Praças"
echo "===================================="
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Parar servidores antigos
echo -e "${YELLOW}[1/5] Limpando servidores anteriores...${NC}"
pkill -f "spring-boot:run" 2>/dev/null || true
sleep 2
echo -e "${GREEN}✓ Limpo${NC}"
echo ""

# Compilar projeto
echo -e "${YELLOW}[2/5] Compilando projeto...${NC}"
cd "$(dirname "$0")"
./mvnw clean compile -DskipTests=true -q
echo -e "${GREEN}✓ Compilado${NC}"
echo ""

# Iniciar servidor
echo -e "${YELLOW}[3/5] Iniciando servidor Spring Boot...${NC}"
echo "      (Aguarde 15 segundos...)"
./mvnw spring-boot:run -DskipTests=true > /tmp/spring-boot.log 2>&1 &
SPRING_PID=$!
sleep 15

# Verificar se servidor iniciou
if ! kill -0 $SPRING_PID 2>/dev/null; then
    echo -e "${RED}✗ Falha ao iniciar servidor${NC}"
    cat /tmp/spring-boot.log
    exit 1
fi
echo -e "${GREEN}✓ Servidor iniciado (PID: $SPRING_PID)${NC}"
echo ""

# Fazer login
echo -e "${YELLOW}[4/5] Autenticando como admin...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}✗ Falha ao obter token${NC}"
    echo "Resposta: $LOGIN_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Token obtido${NC}"
echo -e "${BLUE}Token: ${TOKEN:0:50}...${NC}"
echo ""

# Testar endpoints
echo -e "${YELLOW}[5/5] Testando endpoints...${NC}"
echo ""

# GET /api/pracas
echo -e "${BLUE}→ GET /api/pracas${NC}"
GET_RESPONSE=$(curl -s -w "\n%{http_code}" http://localhost:8080/api/pracas \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$GET_RESPONSE" | tail -n1)
BODY=$(echo "$GET_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Status: 200${NC}"
    echo "Resposta: $BODY"
else
    echo -e "${RED}✗ Status: $HTTP_CODE${NC}"
    echo "Resposta: $BODY"
fi
echo ""

# POST /api/pracas
echo -e "${BLUE}→ POST /api/pracas${NC}"
POST_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "nome": "Praça da Matriz",
    "logradouro": "Rua Sete de Setembro, 100",
    "bairro": "Centro Histórico",
    "cidade": "Porto Alegre",
    "latitude": -30.033056,
    "longitude": -51.230000,
    "descricao": "Uma praça histórica no coração da cidade",
    "fotoUrl": "https://exemplo.com/imagens/praca_matriz.jpg",
    "status": "ATIVA"
  }')

HTTP_CODE=$(echo "$POST_RESPONSE" | tail -n1)
BODY=$(echo "$POST_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "201" ]; then
    echo -e "${GREEN}✓ Status: 201 (Criado com sucesso)${NC}"
    echo "Resposta: $BODY"
elif [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Status: 200 (Sucesso)${NC}"
    echo "Resposta: $BODY"
else
    echo -e "${RED}✗ Status: $HTTP_CODE${NC}"
    echo "Resposta: $BODY"
fi
echo ""

echo -e "${GREEN}===================================="
echo "✅ Testes completados!"
echo "Servidor rodando em http://localhost:8080"
echo "PID: $SPRING_PID"
echo "====================================${NC}"

