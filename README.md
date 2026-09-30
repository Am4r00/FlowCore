# FlowCore

Aplicação em desenvolvimento para solicitações de reembolso de despesas.

## Estado atual

O projeto contém uma tela de apresentação em Angular e duas aplicações Spring Boot: Workflow Service e Integration Service, com builds próprios, conexão ao PostgreSQL, Flyway e health checks. Ainda não há formulário de reembolso, autenticação, persistência de dados de negócio, regras de negócio ou integração externa. A interface ainda não se comunica com o backend, e os serviços ainda não trocam mensagens.

## Executar localmente

Há também uma instância PostgreSQL local via Docker Compose, com bancos e usuários separados para cada serviço. Consulte [Preparação do PostgreSQL](infra/postgres/README.md). O Workflow conecta ao banco `workflow` usando `workflow_app`; o Integration conecta ao banco `integration` usando `integration_app`. Cada serviço inicializa o Flyway no próprio banco. Ainda não existem arquivos de migração ou tabelas de negócio.

Ambiente utilizado nesta etapa: Node.js 24.21.0, npm 11.19.0 e Angular 22.2.0. O Angular CLI é uma dependência local; não é necessário instalá-lo globalmente.

A partir da raiz do repositório:

```sh
cd web
npm ci
npm start
```

Abra o endereço informado no terminal, normalmente http://localhost:4200. Encerre o servidor com `Ctrl+C`.

No PowerShell, caso a execução de `npm.ps1` seja bloqueada, use `npm.cmd` no lugar de `npm`.

## Verificar

Na pasta `web`:

```sh
npm test -- --watch=false
npm run build
```

Os dois testes atuais verificam a criação do componente principal e o título exibido. Eles não cobrem regras de reembolso, ainda não implementadas. O build gera arquivos em `web/dist/flowcore-web`.

Para conferir a apresentação manualmente, abra a página e verifique título, descrição e adaptação do conteúdo ao reduzir a largura da janela.

## Workflow Service

Spring Boot 4.1.1, com Java-alvo 21. Ambiente utilizado: JDK 24.0.2. Configure `JAVA_HOME` para a pasta do JDK. O Maven Wrapper está incluído; não é necessário instalar Maven globalmente. A primeira execução precisa de acesso à internet para baixar Maven e dependências.

Para executar o Workflow, prepare os bancos conforme o guia do PostgreSQL e mantenha o container saudável. No PowerShell, a partir da raiz do repositório, forneça a senha de `workflow_app` sem colocá-la no arquivo de configuração:

```powershell
cd workflow-service
$workflowSecret = Read-Host 'Senha do usuario workflow_app' -AsSecureString
$env:WORKFLOW_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $workflowSecret).Password
.\mvnw.cmd spring-boot:run
```

A variável existe nesta sessão e é herdada pelos processos iniciados nela. Não imprima seu conteúdo. O `.env` da raiz é utilizado pelo Compose; o Spring Boot não o carrega automaticamente. Para executar pela IDE, configure `WORKFLOW_DB_PASSWORD` na configuração local de execução, sem versionar a senha.

Consulte http://localhost:8080/actuator/health. O resultado esperado contém `"status":"UP"`. O health check agora inclui a conectividade do DataSource com o PostgreSQL, mas não comprova regras de reembolso ou gravação de dados de negócio.

Para testar e empacotar, na pasta `workflow-service`, use o mesmo terminal com a variável definida e mantenha o banco disponível:

```powershell
.\mvnw.cmd verify
```

O teste atual verifica o carregamento do contexto Spring, incluindo a inicialização do Flyway com acesso ao banco. Não verifica gravação de dados de negócio nem recuperação após indisponibilidade. Ainda não há teste automatizado do endpoint de saúde. A execução pelo JAR foi verificada na etapa anterior, antes de acrescentar o DataSource e o Flyway.

Encerre a execução anterior com `Ctrl+C` antes de executar o JAR, para liberar a porta 8080. O JAR também exige a variável de ambiente definida no terminal:

```powershell
java -jar target\workflow-service-0.0.1-SNAPSHOT.jar
```

Consulte novamente o health check. Em Linux/macOS, use `./mvnw` no lugar de `.\mvnw.cmd` e `/` nos caminhos. O serviço não depende de o frontend estar em execução.

### Verificação manual da indisponibilidade do banco

Com o Workflow em execução, consulte o health check e confirme `UP`. Em outro terminal, na raiz do repositório:

```powershell
docker compose stop postgres
```

Consulte novamente o health check. A resposta pode aguardar o tempo limite de conexão; o estado esperado é `DOWN`. Isso não implica que o processo Java tenha encerrado. Restaure o banco:

```powershell
docker compose start postgres
docker compose ps
```

Quando o container estiver saudável, consulte novamente o endpoint. Foi verificada manualmente a sequência `UP → DOWN → UP`, sem reiniciar o Workflow. Essa interrupção afeta os dois bancos hospedados na instância e preserva o volume.

## Integration Service

Utiliza Spring Boot 4.1.1 e Java-alvo 21, com os mesmos requisitos de JDK e `JAVA_HOME` descritos para o Workflow. Possui seu próprio Maven Wrapper e utiliza a porta 8081.

Prepare os bancos e mantenha o PostgreSQL disponível. No PowerShell, a partir da raiz do repositório, informe a senha de `integration_app`:

```powershell
cd integration-service
$integrationSecret = Read-Host 'Senha do usuario integration_app' -AsSecureString
$env:INTEGRATION_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $integrationSecret).Password
.\mvnw.cmd spring-boot:run
```

Consulte http://localhost:8081/actuator/health. O resultado esperado contém `"status":"UP"` e inclui a conectividade com o banco. A variável vale para esta sessão do terminal; ao abrir outra, defina-a novamente. Para executar pela IDE, configure `INTEGRATION_DB_PASSWORD` localmente, sem versionar a senha. O Spring Boot não carrega automaticamente o `.env` do Compose.

Para testar e empacotar, na pasta `integration-service`, com a variável definida e o banco disponível:

```powershell
.\mvnw.cmd verify
```

Encerre a execução anterior com `Ctrl+C` para liberar a porta 8081 e execute no terminal com a variável de senha definida:

```powershell
java -jar target\integration-service-0.0.1-SNAPSHOT.jar
```

O teste automatizado atual verifica o carregamento do contexto Spring, incluindo a inicialização do Flyway com acesso ao banco. O teste passou e o comando `verify` concluiu com sucesso após a configuração. Não há teste automatizado de gravação de dados de negócio ou do endpoint de saúde.

Foi verificada manualmente a sequência `UP → DOWN → UP` ao parar e iniciar o PostgreSQL, sem reiniciar o Integration. Para reproduzir, siga a verificação de indisponibilidade descrita acima e consulte a porta 8081. A instância PostgreSQL é compartilhada: pará-la afeta os bancos dos dois serviços.

A execução simultânea dos serviços e a resposta do Integration pelo JAR após encerrar o Workflow foram verificadas antes de adicionar o DataSource e o Flyway. A execução pelo JAR ainda não foi repetida com a configuração atual. Os serviços ainda não se comunicam entre si nem com ERP.

## Flyway nos dois serviços

Cada aplicação usa sua conexão existente para inicializar o Flyway e manter a tabela `public.flyway_schema_history` em seu próprio banco. Os históricos são separados, apesar de as tabelas terem o mesmo nome.

Ainda não há arquivos SQL de migração. Nesta etapa, o aviso `No migrations found` é esperado; a mensagem de banco atualizado significa apenas que não há migrações encontradas pendentes. Não significa que as tabelas de negócio estejam prontas.

Após iniciar cada serviço pela primeira vez, é possível consultar os históricos no PowerShell, na raiz do repositório:

```powershell
docker compose exec postgres psql -h 127.0.0.1 -U workflow_app -d workflow -W -c "SELECT installed_rank, version, description, success FROM public.flyway_schema_history;"
docker compose exec postgres psql -h 127.0.0.1 -U integration_app -d integration -W -c "SELECT installed_rank, version, description, success FROM public.flyway_schema_history;"
```

Informe a senha do usuário indicado em cada comando. As consultas apenas leem o histórico. Foi confirmado manualmente o resultado `(0 rows)` nos dois bancos. A aplicação de uma migração SQL ainda não foi testada. Os bancos precisam estar disponíveis durante a inicialização e os testes, pois o Flyway os acessa nessa etapa.

## Integração contínua (CI)

O GitHub Actions executa três workflows em pull requests destinados à `main` e em pushes à `main`. Os resultados e logs ficam na aba [Actions](https://github.com/Am4r00/FlowCore/actions).

| Workflow | Ambiente | Verificações |
| --- | --- | --- |
| [Web CI](.github/workflows/web-ci.yml) | Ubuntu e Node.js 24.21.0 | `npm ci`, `npm test -- --watch=false` e `npm run build` na pasta `web`. |
| [Workflow CI](.github/workflows/workflow-ci.yml) | Ubuntu, Java 21 Temurin e PostgreSQL 17.11 temporário | Preparação dos bancos e `verify` pelo Maven Wrapper na pasta `workflow-service`. |
| [Integration CI](.github/workflows/integration-ci.yml) | Ubuntu, Java 21 Temurin e PostgreSQL 17.11 temporário | Preparação dos bancos e `verify` pelo Maven Wrapper na pasta `integration-service`. |

Cada job Java possui seu próprio contêiner PostgreSQL e executa `infra/postgres/setup-database.sql`. O script cria os dois bancos, mas cada job testa apenas seu serviço, com o respectivo usuário de aplicação. As senhas fictícias dos workflows são exclusivas do ambiente temporário; os jobs não utilizam o banco nem as credenciais locais. O Flyway é inicializado durante o teste de contexto Spring.

Os três workflows passaram nos respectivos pull requests e na `main` após os merges. Os checks cobrem os testes existentes e a geração dos builds; não comprovam regras de negócio, comunicação entre serviços, aplicação de migrações SQL ou recuperação após falhas. Ainda não foi realizado um exercício de falha intencional para verificar o CI vermelho. Os workflows não fazem deploy.

## Endereços locais

| Aplicação | Endereço |
| --- | --- |
| Frontend | http://localhost:4200 |
| Workflow health check | http://localhost:8080/actuator/health |
| Integration health check | http://localhost:8081/actuator/health |

## Estrutura

- `web/`: aplicação Angular.
- `web/src/app/`: componente principal, HTML, CSS e testes.
- `web/package-lock.json`: versões resolvidas das dependências, utilizadas pelo `npm ci`.
- `workflow-service/`: backend Spring Boot, configuração e Maven Wrapper.
- `workflow-service/src/test/`: teste de inicialização do contexto Spring.
- `integration-service/`: segunda aplicação Spring Boot, com configuração, testes e Maven Wrapper próprios.

Dependências, cache e build são gerados localmente e não fazem parte do versionamento.
