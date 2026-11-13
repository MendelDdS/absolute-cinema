# Histórico do projeto

Diário do que foi feito no estudo, em ordem cronológica. O `git log` registra **o que mudou** em cada commit. Este arquivo registra também o que não aparece no código: decisões e seus motivos, conceitos estudados, problemas encontrados e como foram resolvidos.

Cada dia tem as seções: **Feito**, **Decisões**, **Conceitos estudados**, **Problemas e soluções** e **Pendências**. A entrada mais recente fica no fim.

---

## 2026-11-12

### Feito
- Leitura do projeto e criação do repositório Git (`.gitignore`, `.env.example`). O `.env` com as credenciais fica fora do Git.
- Revisão dos testes da `application`. O pacote foi trocado de `RuntimeException` genérica para exceções próprias.
- Construtor de `Movie` tornado privado: a criação passa por `create`/`restore`, como em `Room` e `Session` (`455d18a`).
- Tabela `rooms_seats` corrigida para `room_seats` na V2 (`2201e99`).
- `application.yaml` configurado (`5208893`):
  - datasource via variáveis de ambiente;
  - `.env` importado com `spring.config.import`;
  - `ddl-auto: validate`;
  - `open-in-view: false`.
- Validação do tamanho máximo do título do filme (200) no domínio, com testes de valor-limite (`cb99d80`).

### Decisões
- **Anotações do Spring na `application` (plano, 3.8): opção A.** `@Service` e `@Transactional` ficam nos services.
  - **Motivo:** é a prática comum em empresas que usam hexagonal com Spring. A alternativa B (config manual e decorators) custa uma classe extra por caso de uso, para um ganho pequeno.
  - **Como a fronteira continua protegida:** regras ArchUnit. A `application` só pode usar `org.springframework.stereotype` e `org.springframework.transaction`.
- **Regras de tamanho no domínio**, não só no banco.
  - **Motivo:** o erro aparece como `DomainException`, com mensagem clara, em vez de falhar no INSERT.
- **Migrations já aplicadas não se editam**; a correção da V2 só foi permitida porque ela ainda não tinha rodado em nenhum banco.
  - **Motivo:** o Flyway confere o checksum e se recusa a subir se uma migration aplicada mudar.

### Conceitos estudados
- `@Transactional` na classe (vale para todos os métodos; usado com `readOnly = true` nos services de leitura) versus no método (só nos métodos de escrita).
- Proxy do Spring e o problema de self-invocation: uma chamada interna, `this.metodo()`, não passa pelo proxy e por isso ignora o `@Transactional`.
- Padrão Decorator (base da opção B).
- `String.formatted(...)` e os placeholders `{}` do SLF4J.
- Constante `static final` na classe versus variável local.
- Testes de valor-limite: 200, 201, e 200 com espaços nas pontas.
- Quebra de linha no fim do arquivo (caractere invisível; o Git mostra `\ No newline at end of file` quando falta).
- Mapeamento de portas do Docker: `5433:5432` (porta do host : porta do container).

### Problemas e soluções
- **Anotações removidas e depois recolocadas.**
  - Instruções da opção B foram passadas antes de a decisão ser tomada.
  - Corrigido recolocando as anotações.

---

## 2026-11-13

### Feito
- `SessionPersistenceAdapter` implementado (`053ba51`): `saveAndFlush` mais a tradução do SQLState `23P01` para `SessionOverlapException`.
- `SessionQueryPersistenceAdapter` implementado, com projeções JPQL que juntam sessão, filme e sala (`053ba51`).
- Portas `MovieQueryRepository` e `RoomQueryRepository` removidas (`8f18f82`):
  - a leitura de filmes e salas passa a usar `MovieRepository`/`RoomRepository` com `View.from`;
  - campo de `RoomView` renomeado de `title` para `name`;
  - criado `MovieQueryServiceTest`.
- `LoggingSessionEventPublisher` criado como publicador temporário (`163aa31`).
- **A aplicação subiu pela primeira vez:** Flyway V1–V3 aplicadas, `ddl-auto: validate` passou, Tomcat na porta 8080.
- 35 testes unitários passando.
- `HexagonalArchitectureTest` com ArchUnit 1.4.1. Fecha a seção 6.1 do plano. Contém 3 regras:
  - o `domain` só depende do JDK;
  - a `application` só depende do JDK, do `domain` e de `stereotype`/`transaction` do Spring;
  - `adapter.in` não usa `adapter.out`.
- Fatia de Filme (6.2): `CreateMovieRequest`, `MovieController` (`POST`, `GET` lista, `GET` por id) e `GlobalExceptionHandler` com Problem Details.
  - Testado com curl.
  - Respostas de sucesso: 201 com `Location`, 200 nos dois `GET`.
  - Respostas de erro:
    - 400 vindo do domínio;
    - 400 para JSON malformado ou sem `durationMinutes`;
    - 400 para UUID inválido;
    - 404 para filme inexistente.
- Fatia de Sala (6.3): `CreateRoomRequest` e `RoomController` (`POST`, `GET` lista, `GET` por id). Testado com curl. Junto com ela:
  - nome da sala limitado a 100 caracteres no domínio, com teste escrito antes da correção (sem o limite, o Postgres rejeitaria e o cliente receberia 500);
  - `RoomQueryUseCase.get` criado para o `Location` do `POST` apontar para um recurso que existe;
  - testes `RoomQueryServiceTest` e `CreateRoomServiceTest`.

### Decisões
- **Repositório de leitura separado só quando o agregado não atende a tela** (plano, 3.4).
  - Filmes e salas já têm exatamente os dados das telas, então uma porta separada seria duplicação.
  - Sessões mantêm `SessionQueryRepository`, porque a tela junta três tabelas.
- **O SQLState `23P01` fica no adapter de persistência.**
  - **Motivo:** traduzir erro técnico em erro de domínio é responsabilidade do adapter. A `application` só conhece `SessionOverlapException`.
  - A constraint `EXCLUDE` é a única proteção contra duas requisições simultâneas, por isso o erro não é "adivinhado": ele é a forma como o banco avisa de uma corrida que a query `findOverlapping` não consegue evitar.
- **Regras do ArchUnit como lista do que é permitido (`onlyDependOnClassesThat`) para `domain` e `application`.**
  - **Motivo:** uma lista de proibições só pega o que alguém lembrou de proibir. A lista de permissões pega qualquer dependência nova, como Lombok, Jackson, `@Value` ou `@Autowired`.
  - Para `adapter.in` a regra é uma proibição, porque o controller pode depender de muita coisa (web, validação). Ela usa `allowEmptyShould(true)` porque o pacote ainda não existe.
- **Validação da entrada HTTP só no domínio** (plano, 3.7). Não há Bean Validation nos requests.
  - **Motivo:** a regra fica em um lugar só e já está testada. Com validação também no request, o limite de 200 caracteres ficaria duplicado e poderia divergir.
  - **Custo aceito:** os erros vêm um de cada vez.
  - **Quando revisar:** se surgir um front-end com formulário ou documentação OpenAPI.
- **`saveAndFlush` em vez de `save`.**
  - **Motivo:** força o INSERT dentro do método, para que a exceção apareça ali, onde pode ser traduzida, e não no commit, fora do adapter.

### Conceitos estudados
- Sobreposição de sessões verificada em três camadas: a query, `Session.ensureNoOverlapWith` e a constraint `EXCLUDE USING gist`.
- `DataIntegrityViolationException.getMostSpecificCause()` e SQLState.
- SELECT extra do `merge` quando o ID é atribuído pela aplicação. Correção futura: `Persistable`.
- Dual write e Transactional Outbox, que vão substituir o publicador em log.
- ArchUnit:
  - analisa bytecode, então um import sem uso não conta como dependência;
  - `pacote..` inclui os subpacotes, e sem o `..` o padrão vale só para o pacote exato (foi a causa de 83 violações falsas na primeira versão);
  - `DoNotIncludeTests` tira os fakes e o JUnit da análise;
  - vale ver a regra falhar de propósito, para saber que ela verifica alguma coisa.
- Organização por **dono do contrato**, não por quem usa a classe.
  - Teste para decidir: "se o HTTP fosse trocado por outra entrada, a classe continuaria existindo?" Se sim, ela fica na `application` (`MovieView`, `CreateMovieCommand`). Se não, fica no adapter (`CreateMovieRequest`, `GlobalExceptionHandler`).
- `@ExceptionHandler(NotFoundException.class)` também captura as subclasses. Por isso a classe base abstrata permite um único handler de 404 para todos os casos.
- `POST` responde 201 + `Location` e corpo vazio: um command devolve só o ID.
- Jackson 3 (Spring Boot 4): `FAIL_ON_NULL_FOR_PRIMITIVES` vem ligado por padrão. Um `int` ausente no JSON vira 400 ("Failed to read request"), e não `0` como no Jackson 2.
- Qualidade de testes:
  - o nome da classe de teste deve corresponder à classe testada, porque é a primeira pista quando o teste falha;
  - valores do cenário não devem coincidir (numa sala 1×1, a capacidade 1 não distingue fileiras de assentos; numa 2×3, o 6 distingue);
  - um teste de valor-limite usa o primeiro valor inválido (101), não um valor qualquer acima do limite;
  - o teste com espaços nas pontas que precisa ser **aceito** é o único que prova que o `strip()` acontece antes da contagem.
- `git status` com `AM`: parte do arquivo já está no stage. `git diff --cached --stat` mostra o que vai entrar no commit.

### Problemas e soluções
- **IntelliJ:** `password authentication failed for user "${CATALOG_DB_USER}"`.
  - Causa: a pasta de trabalho era a raiz, então o `../.env` não era encontrado, e o `optional:` escondia o erro.
  - Solução: Working directory = `$MODULE_WORKING_DIR$`.
- **`required a bean of type SessionRepository`:** erro esperado enquanto o adapter não existia. Resolvido com o `@Repository`.
- **Primeiro commit da série misturou assuntos.**
  - Versões antigas de alguns arquivos já estavam no stage, e o commit levou junto exclusões que deixavam o código sem compilar.
  - Desfeito com `git reset --mixed` (os commits eram locais e recentes) e refeito, conferindo cada commit com `git diff --cached --stat`.
- **O mesmo erro se repetiu nos commits da fatia de Sala.**
  - Causa: o `git diff --cached --stat` rodava no mesmo comando do commit, então a conferência vinha tarde demais.
  - Correção do processo: esvaziar o stage com `git reset` antes de montar os commits e conferir o stage **antes** de rodar `git commit`, em passos separados.

### Pendências
- Fatia de Sessão (6.4): `ScheduleSessionRequest`, `SessionController` e o handler 409 para `SessionOverlapException`.
- Asserts extras em `failsWhenRoomNotFound`: tipo `NotFoundException` e nada salvo ou publicado.
- Renomear `acceptTitleAtMaxLength` para `acceptsTitleAtMaxLength` (`MovieTest`).
- Teste faltando: o caso "não encontrado" de `SessionQueryService.get`.
- Divergências com o plano:
  - UUID v7;
  - nome `transaction-service` vs `transactions-service`;
  - versões das imagens do Testcontainers.
