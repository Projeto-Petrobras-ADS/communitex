#!/bin/bash

# Script de Teste - Cadastro de Empresa com Representante
# Certifique-se de que a aplicação está rodando em http://localhost:8080

BASE_URL="http://localhost:8080/api/empresas"
TIMESTAMP=$(date +%s)

echo "======================================"
echo "Teste 1: Criar Empresa com Representante (SUCESSO ESPERADO)"
echo "======================================"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "razaoSocial": "Empresa Tech Solutions LTDA",
    "cnpj": "12345678000195",
    "nomeFantasia": "TechSolutions",
    "email": "contato@techsolutions.com",
    "telefone": "4733333333",
    "nomeRepresentante": "João Silva",
    "emailRepresentante": "joao.silva'$TIMESTAMP'@techsolutions.com",
    "senhaRepresentante": "SenhaSegura@123"
  }' \
  -w "\nHTTP Status: %{http_code}\n\n"

sleep 2

echo "======================================"
echo "Teste 2: Criar Empresa com Email de Representante Duplicado (ERRO ESPERADO)"
echo "======================================"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "razaoSocial": "Empresa Tech Solutions 2 LTDA",
    "cnpj": "98765432000100",
    "nomeFantasia": "TechSolutions2",
    "email": "contato2@techsolutions.com",
    "telefone": "4733333334",
    "nomeRepresentante": "Maria Santos",
    "emailRepresentante": "joao.silva'$TIMESTAMP'@techsolutions.com",
    "senhaRepresentante": "OutraSenha@456"
  }' \
  -w "\nHTTP Status: %{http_code}\n\n"

sleep 2

echo "======================================"
echo "Teste 3: Criar Empresa com CNPJ Duplicado (ERRO ESPERADO)"
echo "======================================"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "razaoSocial": "Empresa Tech Solutions 3 LTDA",
    "cnpj": "12345678000195",
    "nomeFantasia": "TechSolutions3",
    "email": "contato3@techsolutions.com",
    "telefone": "4733333335",
    "nomeRepresentante": "Pedro Costa",
    "emailRepresentante": "pedro.costa@techsolutions.com",
    "senhaRepresentante": "MaisSenha@789"
  }' \
  -w "\nHTTP Status: %{http_code}\n\n"

sleep 2

echo "======================================"
echo "Teste 4: Criar Empresa com Campo Obrigatório Faltando (ERRO ESPERADO)"
echo "======================================"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "razaoSocial": "Empresa Tech Solutions 4 LTDA",
    "cnpj": "11111111000195",
    "nomeFantasia": "TechSolutions4",
    "email": "contato4@techsolutions.com",
    "telefone": "4733333336",
    "nomeRepresentante": "Ana Paula",
    "emailRepresentante": "ana.paula@techsolutions.com"
  }' \
  -w "\nHTTP Status: %{http_code}\n\n"

sleep 2

echo "======================================"
echo "Teste 5: Listar Todas as Empresas (VERIFICAR SE FOI CRIADA)"
echo "======================================"
curl -X GET "$BASE_URL" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n\n"

sleep 2

echo "======================================"
echo "Teste 6: Buscar Empresa por ID (USAR ID DO TESTE 1)"
echo "======================================"
# Você precisará substituir {ID} pelo ID retornado do Teste 1
echo "Nota: Substitua {ID} pelo ID retornado no Teste 1"
# curl -X GET "$BASE_URL/{ID}" \
#   -H "Content-Type: application/json" \
#   -w "\nHTTP Status: %{http_code}\n\n"

echo ""
echo "======================================"
echo "Testes Concluídos!"
echo "======================================"

