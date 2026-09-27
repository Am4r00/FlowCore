# FlowCore

Aplicação em desenvolvimento para solicitações de reembolso de despesas.

## Estado atual

O projeto contém uma tela de apresentação em Angular e duas aplicações Spring Boot: Workflow Service e Integration Service, com builds próprios e health checks. Ainda não há formulário de reembolso, autenticação, persistência, regras de negócio ou integração externa. A interface ainda não se comunica com o backend, e os serviços ainda não trocam mensagens.

## Executar localmente

Há também uma instância PostgreSQL local via Docker Compose, com bancos e usuários separados para cada serviço. Consulte [Preparação do PostgreSQL](infra/postgres/README.md). Os serviços Java ainda não estão conectados a esses bancos; as migrações e a persistência da aplicação ainda não foram implementadas.

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

No PowerShell, a partir da raiz do repositório:

```powershell
cd workflow-service
.\mvnw.cmd spring-boot:run
```

Consulte http://localhost:8080/actuator/health. O resultado esperado contém `"status":"UP"`. Esse resultado representa os indicadores atuais do serviço, não a validação de um processo de reembolso ou de um banco de dados.

Para testar e empacotar, na pasta `workflow-service`:

```powershell
.\mvnw.cmd verify
```

O teste atual verifica o carregamento do contexto Spring. A resposta HTTP do health check e a execução pelo JAR foram verificadas manualmente; ainda não há teste automatizado desse endpoint.

Encerre a execução anterior com `Ctrl+C` antes de executar o JAR, para liberar a porta 8080:

```powershell
java -jar target\workflow-service-0.0.1-SNAPSHOT.jar
```

Consulte novamente o health check. Em Linux/macOS, use `./mvnw` no lugar de `.\mvnw.cmd` e `/` nos caminhos. O serviço não depende de o frontend estar em execução.

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
