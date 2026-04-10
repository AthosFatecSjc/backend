#!/bin/bash

# Script de teste para POST /login com dados de teste

BASE_URL="http://localhost:8181"
ENDPOINT="/login"

echo "=========================================="
echo "Teste de Autenticação BACK-03"
echo "=========================================="
echo ""

# Teste 1: Login com usuário ATIVO (deve retornar 200 + token)
echo "1️⃣  Teste ATIVO - Deve retornar HTTP 200:"
echo "---"
curl -X POST "$BASE_URL$ENDPOINT" \
  -H "Content-Type: application/json" \
  -d '{"email":"active@example.com","senha":"senha123456"}' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | python3 -m json.tool 2>/dev/null || cat
echo ""
echo ""

# Teste 2: Login com usuário PENDENTE (deve retornar 403)
echo "2️⃣  Teste PENDENTE - Deve retornar HTTP 403:"
echo "---"
curl -X POST "$BASE_URL$ENDPOINT" \
  -H "Content-Type: application/json" \
  -d '{"email":"pending@example.com","senha":"senha123456"}' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | python3 -m json.tool 2>/dev/null || cat
echo ""
echo ""

# Teste 3: Login com usuário REJEITADO (deve retornar 403)
echo "3️⃣  Teste REJEITADO - Deve retornar HTTP 403:"
echo "---"
curl -X POST "$BASE_URL$ENDPOINT" \
  -H "Content-Type: application/json" \
  -d '{"email":"rejected@example.com","senha":"senha123456"}' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | python3 -m json.tool 2>/dev/null || cat
echo ""
echo ""

# Teste 4: Credenciais inválidas (deve retornar 401)
echo "4️⃣  Teste COM CREDENCIAL INVÁLIDA - Deve retornar HTTP 401:"
echo "---"
curl -X POST "$BASE_URL$ENDPOINT" \
  -H "Content-Type: application/json" \
  -d '{"email":"active@example.com","senha":"senhaerrada"}' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | python3 -m json.tool 2>/dev/null || cat
echo ""
echo "=========================================="
