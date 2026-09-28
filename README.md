# FlowCore

Aplicação em desenvolvimento para solicitações de reembolso de despesas.

## Estado atual

O projeto contém uma tela de apresentação em Angular e duas aplicações Spring Boot: Workflow Service e Integration Service, com builds próprios e health checks. Ainda não há formulário de reembolso, autenticação, persistência, regras de negócio ou integração externa. A interface ainda não se comunica com o backend, e os serviços ainda não trocam mensagens.

## Executar localmente

Há também uma instância PostgreSQL local via Docker Compose, com bancos e usuários separados para cada serviço. Consulte [Preparação do PostgreSQL](infra/postgres/README.md). O Workflow conecta ao banco `workflow` usando `workflow_app`. O Integration ainda não está conectado ao banco; as migrações e a persistência de dados de negócio ainda não foram implementadas.

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

O teste atual verifica o carregamento do contexto Spring; não substitui uma verificação de conexão ou de recuperação do banco. Ainda não há teste automatizado do endpoint de saúde. A execução pelo JAR foi verificada na etapa anterior, antes de acrescentar o DataSource.

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

No PowerShell, a partir da raiz do repositório:

```powershell
cd integration-service
.\mvnw.cmd spring-boot:run
```

Consulte http://localhost:8081/actuator/health. O resultado esperado contém `"status":"UP"`.

Para testar e empacotar, na pasta `integration-service`:

```powershell
.\mvnw.cmd verify
```

Encerre a execução anterior com `Ctrl+C` para liberar a porta 8081 e execute:

```powershell
java -jar target\integration-service-0.0.1-SNAPSHOT.jar
```

O teste automatizado atual verifica o carregamento do contexto Spring. Foram verificados manualmente o health check, a execução simultânea dos dois serviços e a resposta do Integration executado pelo JAR mesmo após encerrar o Workflow. Isso demonstra independência de execução nesta etapa; não comprova integração entre serviços ou com ERP, ainda não implementada.

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
