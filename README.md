# Prioriza API

Backend do Prioriza, um sistema simples de gerenciamento de atividades.

Esse projeto foi feito em Java com Spring Boot para praticar desenvolvimento backend de um jeito mais próximo de uma aplicação real. A proposta aqui é manter uma aplicação organizada, funcional e coerente, com autenticação, persistência em banco de dados e operações básicas de cadastro.

Hoje a API cobre principalmente:

- cadastro de atividades
- consulta de atividades
- atualização e exclusão
- autenticação com JWT
- rotas protegidas
- paginação na listagem
- controle de versão do banco com Flyway
- verificação de saúde da aplicação com Actuator

---

## Produção

Base URL:

- `https://task-manager-api-njza.onrender.com`

Health:

- `https://task-manager-api-njza.onrender.com/actuator/health`

Raiz:

- `https://task-manager-api-njza.onrender.com/`

Resposta esperada na rota raiz:

~~~json
{
  "status": "ok",
  "service": "prioriza"
}
~~~

Observação:

Como a aplicação está hospedada no plano gratuito do Render, a primeira requisição depois de um tempo parada pode demorar um pouco mais para responder.

---

## Frontend do Prioriza

Frontend publicado:

- `https://gustavomprado.github.io/prioriza-frontend/`

Repositório do frontend:

- `https://github.com/GustavoMPrado/prioriza-frontend`

---

## Visão geral da API

### Autenticação

- `POST /autenticacao/login` — autentica o usuário e retorna o token JWT

Exemplo de resposta:

~~~json
{
  "token": "seu-token-aqui"
}
~~~

### Atividades

As rotas de atividades são protegidas por token.

- `POST /atividades` — cria uma atividade
- `GET /atividades` — lista atividades
- `GET /atividades/{id}` — busca uma atividade por id
- `PUT /atividades/{id}` — atualiza uma atividade
- `DELETE /atividades/{id}` — remove uma atividade

A listagem suporta paginação e ordenação com parâmetros como:

- `page`
- `size`
- `sort`

### Saúde da aplicação

- `GET /actuator/health` — retorna o estado da aplicação

Resultado esperado:

- `UP`

---

## Domínio principal

O domínio principal do projeto é **Atividade**.

Campos principais de uma atividade:

- `id`
- `titulo`
- `descricao`
- `status`
- `prioridade`
- `dataLimite`
- `criadoEm`
- `atualizadoEm`

### StatusAtividade

- `A_FAZER`
- `EM_ANDAMENTO`
- `CONCLUIDA`

### PrioridadeAtividade

- `BAIXA`
- `MEDIA`
- `ALTA`

---

## Tecnologias utilizadas

- Java 21
- Spring Boot
- Spring Web
- Spring Validation
- Spring Data JPA
- Spring Security
- JWT
- PostgreSQL
- Flyway
- Spring Boot Actuator
- SpringDoc OpenAPI
- Gradle
- Docker
- Docker Compose
- JUnit 5
- Mockito
- H2 para testes

---

## Banco de dados

O projeto usa PostgreSQL com controle de versão do schema por meio do Flyway.

Migrations atuais:

- `V1__create_tasks_table.sql`
- `V2__add_indexes_timestamps.sql`

Tabela principal utilizada hoje:

- `tasks`

Colunas atuais:

- `id`
- `titulo`
- `descricao`
- `status`
- `prioridade`
- `data_limite`
- `criado_em`
- `atualizado_em`

Índices atuais:

- `idx_tasks_criado_em`
- `idx_tasks_atualizado_em`

Observação:

A API foi mantida no Render, enquanto o banco PostgreSQL foi migrado para o Neon.

---

## Segurança da aplicação

A API tem a camada de segurança básica esperada para esse tipo de projeto:

- autenticação com JWT
- rotas protegidas
- CORS configurado para o frontend publicado
- limite básico de tentativas no login
- limite de tamanho de página na listagem
- cuidado para evitar exposição desnecessária de dados sensíveis nos logs

### Uso do token

As rotas protegidas exigem o header:

~~~http
Authorization: Bearer <token>
~~~

Sem token válido, a API responde com `401 Unauthorized`.

### CORS

O backend aceita requisições do frontend publicado em:

- `https://gustavomprado.github.io`

### Limite de tentativas no login

Foi adicionado um limite básico em memória para o endpoint de login:

- após 5 tentativas por minuto por IP, a API retorna `429`

### Limite de paginação

Para evitar consultas exageradas:

- valores muito altos em `size` são ajustados para o limite aceito pela aplicação

---

## Como executar localmente

Na pasta onde está o arquivo `docker-compose.yml`, rode:

~~~powershell
docker compose up -d --build
~~~

Isso sobe:

- banco PostgreSQL
- aplicação Spring Boot

A API fica disponível em:

- `http://localhost:8081`

Health local:

- `http://localhost:8081/actuator/health`

Para parar os containers:

~~~powershell
docker compose down
~~~

---

## Configuração local

Foi adotado o seguinte padrão de portas:

- host local: `8081`
- container/aplicação: `8080`

Banco local usado no Docker Compose:

- banco: `prioriza`
- usuário: `prioriza`
- senha: `prioriza`

---

## Exemplo de uso

### 1. Verificar health

~~~powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8081/actuator/health"
~~~

Resultado esperado:

- `status : UP`

### 2. Fazer login

~~~powershell
$base = "http://localhost:8081"
$loginBody = @{ username = "admin"; password = "admin123" } | ConvertTo-Json
$token = (Invoke-RestMethod -Method Post -Uri "$base/autenticacao/login" -ContentType "application/json" -Body $loginBody).token
$token
~~~

### 3. Criar uma atividade

~~~powershell
$body = @{
  titulo = "Estudar Java"
  descricao = "Revisar metodos e vetores"
  status = "A_FAZER"
  prioridade = "MEDIA"
  dataLimite = "2026-04-20"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$base/atividades" -ContentType "application/json" -Headers @{ Authorization = "Bearer $token" } -Body $body
~~~

### 4. Listar atividades

~~~powershell
Invoke-RestMethod -Method Get -Uri "$base/atividades?page=0&size=5&sort=id,desc" -Headers @{ Authorization = "Bearer $token" }
~~~

### 5. Buscar atividade por id

~~~powershell
Invoke-RestMethod -Method Get -Uri "$base/atividades/1" -Headers @{ Authorization = "Bearer $token" }
~~~

### 6. Atualizar atividade

~~~powershell
$bodyAtualizado = @{
  titulo = "Estudar Java"
  descricao = "Revisar metodos, vetores e condicionais"
  status = "EM_ANDAMENTO"
  prioridade = "ALTA"
  dataLimite = "2026-04-22"
} | ConvertTo-Json

Invoke-RestMethod -Method Put -Uri "$base/atividades/1" -ContentType "application/json" -Headers @{ Authorization = "Bearer $token" } -Body $bodyAtualizado
~~~

### 7. Remover atividade

~~~powershell
Invoke-RestMethod -Method Delete -Uri "$base/atividades/1" -Headers @{ Authorization = "Bearer $token" }
~~~

---

## Observação sobre testes manuais no PowerShell

Nos testes manuais no Windows, o `Invoke-RestMethod` costuma funcionar melhor do que `curl.exe`, principalmente em requisições com JSON e autenticação.

Também apareceu um ruído de encoding em alguns testes com acentos no corpo do JSON. Para evitar esse tipo de problema durante a validação manual, usar texto sem acento no request ajuda a não introduzir erro que não é da API.

---

## Repositórios

Backend:

- `https://github.com/GustavoMPrado/prioriza-api`

Frontend:

- `https://github.com/GustavoMPrado/prioriza-frontend`

---

## Contato

Gustavo Marinho Prado Alves

- GitHub: `https://github.com/GustavoMPrado`
- Email: `gmarinhoprado@gmail.com`




