# Roteiro de validação da entrega

## Testes automatizados

Backend (Java 21): `cd backend && mvn test` ou `./mvnw test`. O contexto usa H2 em memória com modo MySQL e um segredo exclusivo de teste, sem depender do banco de produção. Testes unitários cobrem totais, autorização, sanitização, rate limiting, progresso de orçamento e CSV; o teste de integração usa JWT, transação e orçamento persistidos no H2.

Frontend (Node 22): `cd frontend && npm ci`, depois `npm test -- --watchAll=false --runInBand` e `npm run build`. Os testes de utilitários verificam datas locais e mês bissexto. O workflow `.github/workflows/ci.yml` executa esses passos a cada push/PR. H2 não substitui a validação final em MySQL.

## Teste manual no endereço de entrega

- [ ] Criar conta e entrar com senha válida; verificar rejeição de senha incorreta.
- [ ] Criar carteira, lançar receita/despesa e conferir o resumo após recarregar a página.
- [ ] Abrir o dashboard em duas sessões com acesso à mesma carteira, lançar uma transação e verificar atualização SSE.
- [ ] Abrir Relatórios, escolher período, gerar extrato, conferir totais e abrir CSV.
- [ ] Configurar Gemini e gerar insights de uma carteira com transações; conferir ausência da chave no navegador.
- [ ] Abrir Orçamento mensal, definir limite como OWNER, lançar despesa e conferir o percentual; verificar que outro usuário sem acesso recebe 403 e que VIEWER não pode editar.
- [ ] Instalar PWA a partir de HTTPS ou localhost. Após registro do service worker, simular modo offline e recarregar: deve aparecer a página de aviso, sem dados da API em cache.
- [ ] Verificar Swagger, JSON OpenAPI, botão Authorize e health check.
- [ ] Em HTTPS, abrir diretamente `/app/orcamento` e atualizar a página; o React deve carregar, sem erro de roteamento.
- [ ] Confirmar que `EXPOSE_RESET_TOKEN` está desativado e que segredos não aparecem no Git.

## Limitações conhecidas

O envio de e-mail de recuperação de senha ainda não tem integração SMTP no código original; o token de debug deve permanecer desabilitado em produção. Não confundir a resposta genérica de recuperação com confirmação de envio de e-mail. Os testes e este roteiro são entregues para execução; a publicação depende do banco externo e da conta de hospedagem.
