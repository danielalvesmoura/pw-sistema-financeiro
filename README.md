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

.\mvnw.cmd spring-boot:run

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
