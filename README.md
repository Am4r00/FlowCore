# FlowCore

Aplicação em desenvolvimento para solicitações de reembolso de despesas.

## Estado atual

O projeto contém uma tela de apresentação em Angular e um Workflow Service em Spring Boot, executável de forma independente, com health check. Ainda não há formulário de reembolso, autenticação, persistência, regras de negócio ou integração externa. A interface ainda não se comunica com o backend.

## Executar localmente

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

## Estrutura

- `web/`: aplicação Angular.
- `web/src/app/`: componente principal, HTML, CSS e testes.
- `web/package-lock.json`: versões resolvidas das dependências, utilizadas pelo `npm ci`.
- `workflow-service/`: backend Spring Boot, configuração e Maven Wrapper.
- `workflow-service/src/test/`: teste de inicialização do contexto Spring.

Dependências, cache e build são gerados localmente e não fazem parte do versionamento.
