# 🚀 Ambiente de Desenvolvimento

Este projeto utiliza uma arquitetura composta por:

- **Backend Java** (Spring Boot)
- **Backend Python** (FastAPI)
- **PostgreSQL**
- **MongoDB**
- **Docker**
- **Vue.js (Frontend)**

O ambiente pode ser iniciado de diferentes formas dependendo do que você deseja executar.

---

# 📋 Pré-requisitos

Antes de rodar o projeto, instale:

- Docker
- Docker Compose
- Java 17+
- Maven
- Python 3.10+
- Node.js (caso rode o frontend manualmente)

---

# 📂 Estrutura do Projeto

```
.
├── backend-java
├── backend-python
├── docker-compose.yml
├── start.sh
└── README.md
```

---

# 🐳 Rodar apenas os Containers (Bancos)

Se quiser iniciar **apenas os bancos de dados e serviços Docker**:

```bash
docker-compose up -d
```

Para parar os containers:

```bash
docker-compose down
```

### Serviços disponíveis

| Serviço | URL |
|------|------|
| Adminer (PostgreSQL) | http://localhost:8080 |
| Mongo Express | http://localhost:8081 |

---

# ☕ Rodar apenas o Backend Java

Entre na pasta do backend Java:

```bash
cd backend-java
```

Execute o projeto:

```bash
mvn spring-boot:run
```

O backend ficará disponível em:

```
http://localhost:8181
```

---

# 🐍 Rodar apenas o Backend Python

Entre na pasta:

```bash
cd backend-python
```

Ative o ambiente virtual:

```bash
source venv/bin/activate
```

Instale as dependências:

```bash
pip install -r requirements.txt
```

Inicie o servidor:

```bash
uvicorn app.main:app --reload --port 8000
```

A API ficará disponível em:

```
http://localhost:8000
```

Documentação automática da API:

```
http://localhost:8000/docs
```

---

# ⚡ Rodar todo o ambiente automaticamente

O projeto possui um script que inicializa **tudo automaticamente**.

Primeiro dê permissão de execução:

```bash
chmod +x start.sh
```

Depois execute:

```bash
./start.sh
```

Esse script irá:

1. Iniciar os containers Docker
2. Aguardar os bancos iniciarem
3. Iniciar o backend Java
4. Instalar dependências do Python
5. Iniciar o backend Python

---

# 🌐 URLs do ambiente

Após rodar `./start.sh` os serviços estarão disponíveis em:

| Serviço | URL |
|------|------|
| Adminer (PostgreSQL) | http://localhost:8080 |
| Mongo Express | http://localhost:8081 |
| Frontend Vue | http://localhost:3000 |
| Backend Java | http://localhost:8181 |
| Backend Python | http://localhost:8000 |
| Documentação Python | http://localhost:8000/docs |

---

# 🛑 Parar o ambiente

Para parar os containers Docker:

```bash
docker-compose down
```

Para parar os backends iniciados manualmente utilize:

```
CTRL + C
```

---

# 💡 Observação

O script `start.sh` foi criado para facilitar o desenvolvimento local, automatizando a inicialização de todos os serviços necessários para o projeto.