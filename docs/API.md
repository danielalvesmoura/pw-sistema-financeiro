# API REST e eventos

Base local: `http://localhost:8080`. No Docker/deploy, frontend e API compartilham a mesma origem.

- Swagger UI: `/swagger-ui.html`
- Especificação OpenAPI: `/v3/api-docs`
- Health check: `/actuator/health`

Para chamar rotas `/api/v1/**`, registre-se e faça login em `/auth/login`. Copie o `accessToken` e use **Authorize** no Swagger (somente o token, sem repetir o prefixo Bearer) ou envie `Authorization: Bearer <token>`.

| Método | Rota | Finalidade |
| --- | --- | --- |
| POST | `/auth/register` | Cadastro |
| POST | `/auth/login` | Emissão de JWT |
| GET | `/api/v1/users/me` | Perfil autenticado |
| GET/POST | `/api/v1/wallets` | Listagem e criação de carteiras |
| GET/POST | `/api/v1/wallets/{walletId}/transactions` | Consulta e lançamento de transações |
| GET | `/api/v1/wallets/{walletId}/summary` | Resumo financeiro |
| GET | `/api/v1/wallets/{walletId}/reports/transactions` | Extrato JSON |
| GET | `/api/v1/wallets/{walletId}/reports/transactions.csv` | Download CSV |
| GET | `/api/v1/wallets/{walletId}/events` | Fluxo SSE |
| POST | `/api/v1/wallets/{walletId}/ai/insights` | Análise com IA |
| GET/PUT | `/api/v1/wallets/{walletId}/budget` | Orçamento mensal |

O Swagger gera a lista completa de operações a partir dos controllers. As rotas de relatório recebem `startDate` e `endDate` em `AAAA-MM-DD`. A análise com IA recebe essas datas no JSON. O SSE usa `Accept: text/event-stream` e o mesmo JWT das outras rotas; para acompanhar o fluxo, use um cliente HTTP com streaming, por exemplo:

```bash
curl -N -H "Authorization: Bearer $TOKEN" -H "Accept: text/event-stream" http://localhost:8080/api/v1/wallets/1/events
```

## Orçamento mensal

`GET /api/v1/wallets/1/budget?month=2026-11` retorna o limite recorrente da carteira, gastos reais do mês, saldo disponível do limite e percentual consumido. Qualquer membro aceito pode consultar.

`PUT /api/v1/wallets/1/budget?month=2026-11` com `{"limit":1500.00}` define o limite (somente OWNER). `{"limit":null}` remove o limite. O mês da consulta não cria um limite histórico: o mesmo limite é aplicado a todos os meses. O cálculo considera apenas despesas com data dentro do mês e mantém a moeda da carteira.

Respostas usuais: 400 para entrada inválida, 401 para autenticação ausente/inválida, 403 para falta de permissão, 404 para recurso inexistente, 429 para limite de tentativas de autenticação. O formato de erro segue `ApiError`.
