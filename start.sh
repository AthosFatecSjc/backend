#!/bin/bash

# echo "Iniciando ambiente de desenvolvimentoS"

# echo "Iniciando containers Docker..."
# docker-compose up -d

# echo "Aguardando bancos de dados..."
# sleep 10

echo "☕ Iniciando backend Java..."
cd backend-java
mvn spring-boot:run &
cd ..

echo "Iniciando backend Python..."
cd backend-python
source venv/bin/activate
pip install -r requirements.txt

uvicorn app.main:app --reload --port 8000 

echo "Ambiente inicializado!"
echo "==================================="
echo "Adminer (PostgreSQL): http://localhost:8080"
echo "Mongo Express: http://localhost:8081"
echo "Frontend Vue: http://localhost:3000"
echo "Backend Java: http://localhost:8181"  
echo "Backend Python: http://localhost:8000"
echo "Documentação Python API: http://localhost:8000/docs"