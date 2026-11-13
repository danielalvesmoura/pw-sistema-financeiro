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
| `JWT_SECRET` | Chave para assinatura do JWT, com pelo menos 32 caracteres | **sem padrão; obrigatória** |
| `JWT_EXPIRATION` | Expiração do token em ms | `86400000` (24h) |
| `APP_CORS_ALLOWED_ORIGINS` | Origens autorizadas, separadas por vírgula | `http://localhost:3000,http://localhost:3001` |
| `EXPOSE_RESET_TOKEN` | Expõe o token de recuperação apenas para teste local | `false` |


## Segurança

O projeto aplica controles alinhados ao OWASP nos pontos mais relevantes para este sistema financeiro:

- **Segredos fora do código:** `JWT_SECRET`, credenciais de banco e chave do Gemini são lidos por variáveis de ambiente. O segredo JWT não possui valor padrão.
- **Autenticação e senhas:** senhas são armazenadas com BCrypt, o JWT é validado em todas as rotas `/api/**` e senhas novas precisam conter maiúscula, minúscula e número.
- **Rate limiting:** login e recuperação de senha possuem limite de tentativas por IP e identidade. Quando o limite é atingido, a API responde `429 Too Many Requests` com o header `Retry-After`.
- **Controle de acesso:** operações de carteira continuam separadas entre `OWNER`, `EDITOR` e `VIEWER`, e convites pendentes não aparecem como carteiras acessíveis.
- **Validação e sanitização:** DTOs limitam tamanho, formato e valores; textos persistidos são normalizados e têm tags HTML removidas; URLs de anexo aceitam apenas `http` e `https`.
- **Consultas:** o backend utiliza Spring Data JPA/Hibernate, sem concatenação manual de SQL, e a paginação de transações limita tamanho e campos permitidos para ordenação.
- **Headers HTTP:** a API envia políticas como CSP, `X-Frame-Options`, `Referrer-Policy`, `Permissions-Policy` e HSTS quando acessada por HTTPS.
- **CORS:** as origens permitidas são configuráveis por ambiente e o wildcard `*` não é aceito com credenciais.
- **CSRF:** permanece desabilitado porque a API é stateless e o JWT é enviado no header `Authorization`, não em cookie de sessão.
- **Recuperação de senha:** o token não é retornado pela API por padrão. Para testar localmente o fluxo existente, use `EXPOSE_RESET_TOKEN=true`; não habilite essa opção em produção.

Exemplo no PowerShell antes de iniciar o backend:

```powershell
$env:JWT_SECRET="troque-por-uma-chave-longa-com-pelo-menos-32-caracteres"
$env:APP_CORS_ALLOWED_ORIGINS="http://localhost:3000"
cd backend
.\mvnw.cmd spring-boot:run
```

Exemplo no Linux/macOS:

```bash
export JWT_SECRET="troque-por-uma-chave-longa-com-pelo-menos-32-caracteres"
export APP_CORS_ALLOWED_ORIGINS="http://localhost:3000"
cd backend
./mvnw spring-boot:run
```

O arquivo `backend/.env.example` serve apenas como referência e não contém credenciais reais. O arquivo `.env` permanece ignorado pelo Git.

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
