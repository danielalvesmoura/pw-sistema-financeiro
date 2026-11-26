# Publicação no Render

O repositório contém `Dockerfile` (build React + Spring Boot em uma imagem) e `render.yaml` (Blueprint). O banco é MySQL externo, não um banco efêmero do container. Não existe URL pública confirmada nesta entrega.

## Publicar

1. Envie os arquivos ao GitHub e mantenha o `render.yaml` na raiz.
2. Crie ou disponibilize uma instância MySQL acessível ao Render com banco `financeiro` e usuário com permissões de leitura, escrita e criação/alteração das tabelas. Use TLS e restrinja acesso de rede quando o provedor permitir. Não utilize a senha de root do seu computador.
3. No painel [Render](https://dashboard.render.com/), escolha **New → Blueprint**, conecte o repositório e selecione a branch de entrega. Revise os recursos e o custo antes de confirmar; o plano configurado é `free` para a aplicação, o banco é contratado separadamente e pode ter custo.
4. Preencha as variáveis solicitadas no painel, sem adicioná-las ao Git:
   - `DB_URL`: URL JDBC do provedor, por exemplo `jdbc:mysql://HOST:PORTA/financeiro?sslMode=REQUIRED&serverTimezone=UTC` (adapte as opções de TLS ao provedor, de preferência valide o certificado e o hostname com `VERIFY_IDENTITY`).
   - `DB_USERNAME`, `DB_PASSWORD`: credenciais do banco.
   - `GEMINI_API_KEY`: chave do Google AI Studio.
   - `GEMINI_MODEL`: modelo disponível para a chave; ajuste se o provedor retornar erro de modelo.
   - `JWT_SECRET`: gerado pelo Blueprint; mantenha o valor entre deploys.
   - `EXPOSE_RESET_TOKEN`: `false`.
5. Após o build, confira `/actuator/health` (esperado `UP`), `/swagger-ui.html` e a página inicial da URL HTTPS real atribuída pelo Render. O frontend usa a mesma origem e não depende de CORS para produção. Caso utilize outra origem, ajuste `APP_CORS_ALLOWED_ORIGINS` e a URL de API do frontend antes do build.
6. Execute o roteiro de teste em `docs/VALIDACAO.md` no endereço publicado. Copie a URL **real e funcional** para a seção Implantação do README e faça um commit desse ajuste. Não use o endereço de exemplo como link de entrega.

A aplicação usa `PORT` fornecida pela plataforma. O plano gratuito pode hibernar após inatividade; reconexões SSE e inicialização a frio podem levar tempo. Não escale horizontalmente sem adaptar o SSE e o rate limiting, que usam memória de uma instância.

Referência: [Render Blueprint YAML](https://render.com/docs/blueprint-spec) e [variáveis de ambiente](https://render.com/docs/configure-environment-variables).

## Docker local

Na raiz, copie `.env.example` para `.env` e preencha valores reais de desenvolvimento (`DB_PASSWORD`, `MYSQL_ROOT_PASSWORD`, `JWT_SECRET` com no mínimo 32 bytes; Gemini opcional). O Compose usa volume persistente para MySQL:

```bash
docker compose up --build -d
docker compose logs -f app
```

Abra `http://localhost:8080`. Para parar, `docker compose down` preserva o volume; `docker compose down -v` **apaga o banco**. Não execute o último comando se quiser manter os dados.
