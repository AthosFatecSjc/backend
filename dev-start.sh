#!/bin/bash

# Script de inicialização para DESENVOLVIMENTO
# Executa a aplicação com variáveis do .env
# Certifique-se de ter um arquivo .env na raiz do projeto

echo "🚀 Iniciando Backend em MODO DESENVOLVIMENTO..."
echo ""

# Carrega variáveis do .env (filtra comentários e linhas em branco)
if [ -f .env ]; then
    export $(grep -v '^#' .env | grep -v '^$' | xargs)
    echo "✅ Arquivo .env carregado com sucesso"
else
    echo "❌ Arquivo .env não encontrado. Crie um .env na raiz do projeto."
    exit 1
fi

echo ""
echo "Configuração ativa:"
echo "  PostgreSQL: ${SPRING_DATASOURCE_USERNAME}@${SPRING_DATASOURCE_URL}"
echo "  MongoDB: ${SPRING_DATA_MONGODB_URI}"
echo "  Porta: ${SERVER_PORT}"
echo "  Endpoint de teste: http://localhost:${SERVER_PORT}/auth/login"
echo ""

cd backend-java
mvn spring-boot:run

