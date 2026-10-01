# FlowCore

Aplicação em desenvolvimento para solicitações de reembolso de despesas.

## Estado atual

O projeto contém uma tela de apresentação em Angular e duas aplicações Spring Boot: Workflow Service e Integration Service, com builds próprios, conexão ao PostgreSQL, Flyway e health checks. Ainda não há formulário de reembolso, autenticação, persistência de dados de negócio, regras de negócio ou integração externa. A interface ainda não se comunica com o backend, e os serviços ainda não trocam mensagens.

## Executar localmente

Há também uma instância PostgreSQL local via Docker Compose, com bancos e usuários separados para cada serviço. Consulte [Preparação do PostgreSQL](infra/postgres/README.md). O Workflow conecta ao banco `workflow` usando `workflow_app`; o Integration conecta ao banco `integration` usando `integration_app`. Cada serviço inicializa o Flyway no próprio banco. O Workflow possui a primeira migração, que cria a tabela de contas `user_account`, sem inserir usuários. O Integration ainda não possui arquivos de migração.

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

O Workflow aplica [V1__create_user_account.sql](workflow-service/src/main/resources/db/migration/V1__create_user_account.sql) ao iniciar ou carregar o contexto nos testes. A migração cria `user_account` com identificador UUID, nome, login, e-mail, campo para hash de senha, situação de acesso e instante de criação. Login e e-mail são obrigatórios e únicos; o login deve ser armazenado em minúsculas, e a unicidade do e-mail ignora diferenças de maiúsculas e minúsculas. A tabela ainda não possui contas de demonstração, e autenticação e geração de hashes não estão implementadas.

Após aplicada, a V1 deve ser preservada; alterações posteriores na estrutura devem entrar em novas migrações. O aviso `No migrations found` continua esperado somente no Integration. No Workflow, espera-se validação da V1 e, após sua primeira aplicação, nenhuma migração pendente.

Após iniciar cada serviço pela primeira vez, é possível consultar os históricos no PowerShell, na raiz do repositório:

```powershell
docker compose exec postgres psql -h 127.0.0.1 -U workflow_app -d workflow -W -c "SELECT installed_rank, version, description, success FROM public.flyway_schema_history;"
docker compose exec postgres psql -h 127.0.0.1 -U integration_app -d integration -W -c "SELECT installed_rank, version, description, success FROM public.flyway_schema_history;"
```

Informe a senha do usuário indicado em cada comando. As consultas apenas leem o histórico. No Workflow foi confirmada a versão `1`, descrição `create user account` e `success = true`; no Integration o histórico continua vazio. Os bancos precisam estar disponíveis durante a inicialização e os testes, pois o Flyway os acessa nessa etapa.

Foram verificados manualmente no Workflow: criação da tabela, não reaplicação ao reiniciar, inserção com valores padrão e rejeição de login duplicado, e-mail duplicado com diferença de maiúsculas, e-mail nulo ou vazio e login com maiúsculas. Os registros usados nessas verificações foram removidos ou desfeitos por rollback. O `verify` local passou com a V1 já aplicada. As restrições ainda não possuem testes automatizados específicos, e gravações concorrentes não foram testadas. A primeira execução desta migração no banco temporário do CI ainda está pendente de validação no PR.

## Integração contínua (CI)

O GitHub Actions executa três workflows em pull requests destinados à `main` e em pushes à `main`. Os resultados e logs ficam na aba [Actions](https://github.com/Am4r00/FlowCore/actions).

| Workflow | Ambiente | Verificações |
| --- | --- | --- |
| [Web CI](.github/workflows/web-ci.yml) | Ubuntu e Node.js 24.21.0 | `npm ci`, `npm test -- --watch=false` e `npm run build` na pasta `web`. |
| [Workflow CI](.github/workflows/workflow-ci.yml) | Ubuntu, Java 21 Temurin e PostgreSQL 17.11 temporário | Preparação dos bancos e `verify` pelo Maven Wrapper na pasta `workflow-service`. |
| [Integration CI](.github/workflows/integration-ci.yml) | Ubuntu, Java 21 Temurin e PostgreSQL 17.11 temporário | Preparação dos bancos e `verify` pelo Maven Wrapper na pasta `integration-service`. |

Cada job Java possui seu próprio contêiner PostgreSQL e executa `infra/postgres/setup-database.sql`. O script cria os dois bancos, mas cada job testa apenas seu serviço, com o respectivo usuário de aplicação. As senhas fictícias dos workflows são exclusivas do ambiente temporário; os jobs não utilizam o banco nem as credenciais locais. O Flyway é inicializado durante o teste de contexto Spring.

Os três workflows passaram nos respectivos pull requests e na `main` após os merges de sua configuração inicial. Os checks cobrem os testes existentes e a geração dos builds. O teste de contexto do Workflow passa a executar também as migrações disponíveis em seu banco temporário; isso não substitui testes específicos das restrições. Os checks não comprovam regras de reembolso, comunicação entre serviços ou recuperação após falhas. Os workflows não fazem deploy.

No [PR #5](https://github.com/Am4r00/FlowCore/pull/5), uma expectativa incorreta no teste do título provocou a mesma falha localmente e no Web CI, enquanto os checks dos serviços Java passaram. Após restaurar a expectativa `FlowCore`, a nova execução passou. O PR foi incorporado à `main` com a correção, preservando no histórico os commits do exercício. Essa verificação demonstra a detecção de falha e a recuperação do CI; não comprova que as configurações do repositório impeçam o merge de um PR com checks falhando.

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
