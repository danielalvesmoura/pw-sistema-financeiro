# Sistema de Gestão Financeira Compartilhada

Aplicação para controle financeiro pessoal e compartilhado, com **frontend em React** e **API REST em Spring Boot**. O sistema possui autenticação JWT, carteiras compartilhadas, categorias, transações, resumo financeiro e níveis de acesso por membro.

## Arquitetura

O backend segue arquitetura em camadas:

```text
Controller -> Service -> Repository -> MySQL
              |
           Security/JWT
```

Principais pacotes: `controller`, `service`, `repository`, `model`, `dto`, `security`, `exception` e `config`.

## Pré-requisitos

- Java 21+
- MySQL (pode ser pelo XAMPP)
- Maven ou Maven Wrapper incluído no projeto
- Node.js e npm para o frontend

## Banco de dados

Configuração padrão:

```text
Banco: financeiro
Host: localhost
Porta: 3306
Usuário: root
Senha: vazia
```

O banco pode ser criado automaticamente pelo Spring ou manualmente importando o arquivo `criar_banco.sql` no phpMyAdmin.

As configurações ficam em `backend/src/main/resources/application.properties`.

### Variáveis de configuração

| Variável | Descrição | Padrão do projeto |
|---|---|---|
| `DB_URL` | URL de conexão com o banco | `jdbc:mysql://localhost:3306/financeiro...` |
| `DB_USERNAME` | Usuário do banco | `root` |
| `DB_PASSWORD` | Senha do banco | vazia |
| `JWT_SECRET` | Chave para assinatura do JWT (mín. 256 bits) | definida em `application.properties` |
| `JWT_EXPIRATION` | Expiração do token em ms | `86400000` (24h) |

## Como executar

### 1. Banco

Inicie o MySQL no XAMPP ou em outra instalação local.

### 2. Backend

Na pasta `backend`:

mvn spring-boot:run

API: `http://localhost:8080`

### 3. Frontend

Na raiz do projeto:

cd frontend
npm install
npm start

Frontend: `http://localhost:3000`

## Swagger

Com o backend em execução:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health check: `http://localhost:8080/actuator/health`

## Decisões de projeto

- MySQL/XAMPP para facilitar a execução local.
- Arquitetura em camadas mantendo regras de negócio nos services.
- DTOs para não retornar entidades JPA diretamente.
- JWT stateless para autenticação da API.

## Tecnologias principais

Backend: Java 21, Spring Boot, Spring Security, JWT, Spring Data JPA/Hibernate, MySQL, Maven e SpringDoc/Swagger.

Frontend: React, Axios, React Router e Recharts.

## Integração com IA

O sistema possui uma funcionalidade de **análise financeira com IA** usando a API Gemini. Na tela **Análise com IA**, o usuário escolhe uma carteira e um período. O backend resume dados reais das transações (receitas, despesas, resultado e maiores categorias de gasto) e envia somente esse contexto agregado para o Gemini, que devolve um diagnóstico, pontos de atenção e ações práticas.

A chave da API fica somente no backend e **não deve ser adicionada ao repositório**.

### Configurar a API Gemini

1. Crie uma chave no Google AI Studio.
2. Defina a variável de ambiente `GEMINI_API_KEY` antes de iniciar o backend.
3. Opcionalmente, defina `GEMINI_MODEL`. O padrão do projeto é `gemini-3.8-flash`.

PowerShell:

```powershell
$env:GEMINI_API_KEY="sua-chave-aqui"
$env:GEMINI_MODEL="gemini-3.8-flash"
cd backend
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
export GEMINI_API_KEY="sua-chave-aqui"
export GEMINI_MODEL="gemini-3.8-flash"
cd backend
./mvnw spring-boot:run
```

Variáveis relacionadas à IA:

| Variável | Descrição | Padrão |
|---|---|---|
| `GEMINI_API_KEY` | Chave usada pelo backend para autenticar na API Gemini | sem valor; obrigatória para gerar análises |
| `GEMINI_MODEL` | Modelo Gemini usado para gerar os insights | `gemini-3.8-flash` |

A API key nunca é enviada ao navegador. O frontend chama apenas o endpoint autenticado do próprio backend: `POST /api/v1/wallets/{walletId}/ai/insights`.
