# PostgreSQL local

## Escopo

Uma instância PostgreSQL 17.11 em Docker, com dois bancos lógicos: `workflow`, pertencente a `workflow_app`, e `integration`, pertencente a `integration_app`. Os usuários não são superusuários e não podem criar outros bancos ou usuários. A permissão padrão de conexão de `PUBLIC` é removida de ambos os bancos.

O script prepara bancos e usuários; não contém tabelas de negócio nem substitui as migrações de cada serviço. O Workflow já possui configuração de conexão ao seu banco, descrita no [README principal](../../README.md). O Integration ainda não está conectado.

## Iniciar a instância

Pré-requisitos: Docker em execução com containers Linux e Docker Compose disponível. Execute os comandos abaixo no PowerShell, na raiz do repositório.

Crie `.env` na raiz com uma senha local exclusiva:

```dotenv
POSTGRES_ADMIN_PASSWORD=SUBSTITUA_POR_UMA_SENHA_LOCAL
```

O arquivo `.env` é ignorado pelo Git. Não use credenciais de trabalho. Confirme a exclusão antes de adicionar arquivos ao Git:

```powershell
git check-ignore .env
docker compose config --quiet
docker compose up -d
docker compose ps
```

O resultado esperado é `healthy`. A porta 5432 é publicada apenas em `127.0.0.1`. Os dados ficam no volume nomeado `postgres_data` do projeto Compose `flowcore`.

## Preparar uma instância nova

Execute esta seção somente quando os usuários e bancos do projeto ainda não existirem. O script não é idempotente: uma segunda execução falha e para no primeiro erro. Não remova o volume para repetir a preparação de um ambiente com dados.

```powershell
docker compose cp infra/postgres/setup-database.sql postgres:/tmp/setup-database.sql
docker compose exec postgres psql -U postgres -d postgres -f /tmp/setup-database.sql
```

São esperadas duas sequências de `CREATE ROLE`, `CREATE DATABASE` e `REVOKE`. Se houver falha, inspecione o estado antes de repetir: comandos anteriores ao erro podem já ter sido aplicados.

Defina as senhas separadamente. Abra o console:

```powershell
docker compose exec postgres psql -U postgres -d postgres
```

Dentro do psql, execute uma linha por vez e responda aos prompts de senha:

```text
\password workflow_app
\password integration_app
\q
```

Use senhas distintas e guarde-as privadamente. O script versionado não contém essas senhas. Alterar `POSTGRES_ADMIN_PASSWORD` no `.env` após a inicialização não muda automaticamente a senha administrativa já armazenada no banco.

## Verificar acesso

Os comandos abaixo usam TCP dentro do container e solicitam a senha do usuário indicado em `-U`.

```powershell
docker compose exec postgres psql -h 127.0.0.1 -U workflow_app -d workflow -W -c "SELECT current_database(), current_user;"
docker compose exec postgres psql -h 127.0.0.1 -U integration_app -d integration -W -c "SELECT current_database(), current_user;"
```

Cada usuário deve acessar seu próprio banco. Os acessos cruzados devem falhar com `permission denied for database`, não com erro de senha:

```powershell
docker compose exec postgres psql -h 127.0.0.1 -U workflow_app -d integration -W -c "SELECT current_user;"
docker compose exec postgres psql -h 127.0.0.1 -U integration_app -d workflow -W -c "SELECT current_user;"
```

Essas verificações não comprovam a conexão a partir das aplicações Java ou o isolamento entre organizações.

## Parar e retomar

```powershell
docker compose stop
docker compose start
```

Esses comandos preservam os dados. `docker compose down` remove os containers, mas mantém o volume nomeado por padrão. Adicionar `--volumes` remove os volumes e seus dados; não use essa opção como solução genérica para erros.

## Evidência da preparação

O script foi executado manualmente em uma instância temporária vazia da mesma imagem. Criou os dois usuários e bancos. A consulta `has_database_privilege` retornou `true` para conexão ao próprio banco e `false` para as duas conexões cruzadas. A instância temporária foi encerrada após a verificação.

No ambiente local, foram verificadas conexões por senha aos bancos próprios e negativas de conexão aos bancos cruzados. Ainda não foi verificada a persistência após recriar o container, nem foram implementados testes automatizados dessa infraestrutura.
