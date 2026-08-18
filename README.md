# Sistema Financeiro Compartilhado

Projeto simples da atividade final. O frontend React foi mantido com o visual existente e o backend está configurado para usar **MySQL pelo XAMPP**.

## 1. XAMPP

1. Abra o XAMPP Control Panel.
2. Inicie MySQL.
3. O projeto usa por padrão:
   - Banco: `financeiro`
   - Host: `localhost`
   - Porta: `3306`
   - Usuário: `root`
   - Senha: vazia

O Spring tenta criar o banco automaticamente. Se preferir criar manualmente, abra o phpMyAdmin e importe o arquivo `criar_banco.sql`.

## 2. Backend

Abra um terminal na pasta backend e execute:

mvnw.cmd spring-boot:run

## 3. Frontend

Abra outro terminal:

cd frontend
npm install
npm start

Frontend: `http://localhost:3000`

