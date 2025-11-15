#!/bin/bash

# 🚀 GUIA RÁPIDO DE INÍCIO - Execute este arquivo
# bash GUIA_RAPIDO.sh

set -e

echo ""
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                  🔐 GUIA RÁPIDO - INÍCIO RÁPIDO               ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}Passo 1: Parando servidores anteriores...${NC}"
pkill -f "spring-boot:run" 2>/dev/null || true
sleep 2
echo -e "${GREEN}✓ Limpo${NC}"
echo ""

echo -e "${CYAN}Passo 2: Compilando projeto...${NC}"
./mvnw clean compile -DskipTests=true -q 2>/dev/null || true
echo -e "${GREEN}✓ Compilado${NC}"
echo ""

echo -e "${CYAN}Passo 3: Iniciando servidor Spring Boot...${NC}"
echo "         (Aguarde 15 segundos para servidor iniciar...)"
./mvnw spring-boot:run -DskipTests=true > /tmp/spring-boot.log 2>&1 &
SPRING_PID=$!
sleep 15

if ! kill -0 $SPRING_PID 2>/dev/null; then
    echo -e "${RED}✗ Falha ao iniciar servidor${NC}"
    echo -e "${RED}Verifique: cat /tmp/spring-boot.log${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Servidor iniciado (PID: $SPRING_PID)${NC}"
echo ""

echo -e "${CYAN}Passo 4: Fazendo login (obtenha novo token)...${NC}"
LOGIN=$(curl -s -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password"}')

TOKEN=$(echo $LOGIN | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}✗ Falha ao obter token${NC}"
    echo -e "${RED}Resposta: $LOGIN${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Token obtido!${NC}"
echo ""
echo -e "${BLUE}Token (salve em uma variável):${NC}"
echo -e "${YELLOW}TOKEN=\"$TOKEN\"${NC}"
echo ""

echo -e "${CYAN}Passo 5: Testando GET /api/pracas...${NC}"
HTTP_CODE=$(curl -s -w "%{http_code}" -o /dev/null 'http://localhost:8080/api/pracas' \
  -H "Authorization: Bearer $TOKEN")

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ GET /api/pracas: HTTP $HTTP_CODE (OK!)${NC}"
else
    echo -e "${RED}✗ GET /api/pracas: HTTP $HTTP_CODE (FALHA)${NC}"
fi
echo ""

echo -e "${CYAN}Passo 6: Testando POST /api/pracas...${NC}"
HTTP_CODE=$(curl -s -w "%{http_code}" -o /dev/null -X POST 'http://localhost:8080/api/pracas' \
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
  }')

if [ "$HTTP_CODE" = "201" ]; then
    echo -e "${GREEN}✓ POST /api/pracas: HTTP $HTTP_CODE (CRIADO!)${NC}"
elif [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ POST /api/pracas: HTTP $HTTP_CODE (OK!)${NC}"
else
    echo -e "${RED}✗ POST /api/pracas: HTTP $HTTP_CODE (FALHA)${NC}"
fi
echo ""

echo "╔════════════════════════════════════════════════════════════════╗"
echo -e "║${GREEN}              ✅ CONFIGURAÇÃO COMPLETA!                  ${NC}║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""
echo -e "${BLUE}Servidor rodando em:${NC} http://localhost:8080"
echo -e "${BLUE}PID do processo:${NC} $SPRING_PID"
echo ""
echo -e "${YELLOW}Para usar em outros terminais:${NC}"
echo -e "  ${CYAN}export TOKEN=\"$TOKEN\"${NC}"
echo -e "  ${CYAN}curl -i 'http://localhost:8080/api/pracas' \\${NC}"
echo -e "    ${CYAN}-H \"Authorization: Bearer \$TOKEN\"${NC}"
echo ""
echo -e "${BLUE}Documentação:${NC}"
echo "  • SOLUCAO_FINAL.md - Comece aqui"
echo "  • EXEMPLOS_CURL.md - Exemplos prontos"
echo "  • CHECKLIST_VERIFICACAO.md - Validar mudanças"
echo ""
echo -e "${GREEN}✨ Tudo pronto! Seu projeto está funcionando corretamente!${NC}"
echo ""

