# Histórico do projeto

Diário do que foi feito no estudo, em ordem cronológica. O `git log` registra **o que mudou** em cada commit. Este arquivo registra também o que não aparece no código: decisões e seus motivos, conceitos estudados, problemas encontrados e como foram resolvidos.

Cada dia tem as seções: **Feito**, **Decisões**, **Conceitos estudados**, **Problemas e soluções** e **Pendências**. A entrada mais recente fica no fim.

---

## 2026-11-12

### Feito
- Leitura do projeto e criação do repositório Git (`.gitignore`, `.env.example`). O `.env` com as credenciais fica fora do Git.
- Revisão dos testes da `application`. O pacote foi trocado de `RuntimeException` genérica para exceções próprias.
- Construtor de `Movie` tornado privado: a criação passa por `create`/`restore`, como em `Room` e `Session` (`f3fd886`).
- Tabela `rooms_seats` corrigida para `room_seats` na V2 (`68c3ea9`).
- `application.yaml` configurado (`42f57ce`):
  - datasource via variáveis de ambiente;
  - `.env` importado com `spring.config.import`;
  - `ddl-auto: validate`;
  - `open-in-view: false`.
- Validação do tamanho máximo do título do filme (200) no domínio, com testes de valor-limite (`54668fa`).

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
- `SessionPersistenceAdapter` implementado (`8d7277f`): `saveAndFlush` mais a tradução do SQLState `23P01` para `SessionOverlapException`.
- `SessionQueryPersistenceAdapter` implementado, com projeções JPQL que juntam sessão, filme e sala (`8d7277f`).
- Portas `MovieQueryRepository` e `RoomQueryRepository` removidas (`88b735d`):
  - a leitura de filmes e salas passa a usar `MovieRepository`/`RoomRepository` com `View.from`;
  - campo de `RoomView` renomeado de `title` para `name`;
  - criado `MovieQueryServiceTest`.
- `LoggingSessionEventPublisher` criado como publicador temporário (`c8ee63f`).
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

---

## 2026-11-14

### Feito
- Fatia de Sessão (6.4):
  - `ScheduleSessionRequest`, com `@NotNull` e `OffsetDateTime`;
  - `SessionController`, com `POST` com `@Valid`, `GET` com os filtros `date` (obrigatório) e `movieId` (opcional) e `GET` por id;
  - handler 409 para `SessionOverlapException`.
- Testado com curl:
  - 201;
  - 409 na sobreposição;
  - **borda aceita**: uma sessão que começa exatamente no fim da anterior é aceita nas três camadas, porque o domínio usa `isBefore` e o `tstzrange` usa `[)`;
  - 400 sem `date` ("Required parameter 'date' is not present.");
  - 400 por validação ("Invalid request content.");
  - 400 do domínio (preço negativo, sessão no passado);
  - 404.

### Conceitos estudados
- Uma sessão às 21:50 em Fortaleza (`00:50Z` do dia seguinte) aparece em `GET /sessions?date=<dia local>`, porque o dia é calculado no fuso do cinema.
- Sem `@Valid`, as anotações do request são ignoradas.
- `@RequestParam` é obrigatório por padrão e é o equivalente do `@NotNull` para parâmetros de URL.
- O nulo é tratado em três lugares, cada um com um papel:
  - o adapter web barra a entrada inválida do cliente (400);
  - o `requireNonNull` em commands e filtros pega erro de programação (NPE);
  - o domínio trata as regras de negócio (`DomainException`).
- Quando dois handlers servem para a mesma exceção, o Spring escolhe o da exceção mais específica (`SessionOverlapException` antes de `DomainException`).

### Decisões
- **Validação de entrada refinada: sintaxe no request, semântica no domínio** (plano, 3.7).
  - O request usa `@NotNull` + `@Valid` apenas para verificar se o campo veio.
  - As regras de negócio continuam só no domínio.
  - **Motivo:** na fatia de Sessão, campos de objeto ausentes (`UUID`, `BigDecimal`, data) chegavam nulos ao `requireNonNull` e viravam `NullPointerException` → 500.
  - **Alternativa rejeitada:** mapear `NullPointerException` para 400. Um bug real passaria a parecer erro do cliente.
- **`startsAt` em ISO 8601 com offset** (`OffsetDateTime` → `toInstant()`).
  - **Motivo:** quem lê enxerga a hora local, o valor não é ambíguo e o adapter não precisa do fuso do cinema.
  - **Alternativas:**
    - UTC puro: fácil errar a conta à mão;
    - hora local sem offset: ambígua em fusos com horário de verão, e o controller dependeria do fuso.
- **Outbox (plano, 3.6)**:
  - **Linha genérica com a mensagem pronta** (`topic`, `aggregate_id`, `payload` JSONB).
    - **Motivo:** o relay serve para qualquer evento, inclusive no `booking-service`, e o contrato fica congelado no momento do evento.
    - **Alternativa descartada:** o relay conhecer cada tipo de evento e montar a mensagem na hora do envio (um `switch` por tipo).
  - **Relay síncrono, parando no primeiro erro.**
    - **Motivo:** só marca o evento como publicado depois da confirmação do Kafka e preserva a ordem.
    - **Alternativa descartada:** envio assíncrono. Ou perde mensagem (se marcar antes da confirmação), ou complica (o callback roda fora da transação).
  - **`eventId` em UUID v4.**
    - **Motivo:** consistência com os outros IDs. O v7 fica para uma migração geral.

### Pendências
- ~~Outbox + Kafka (6.5): substituir o `LoggingSessionEventPublisher`.~~ Feito em 2026-11-18.

---

## 2026-11-18

### Feito
- Fatia de Outbox e Kafka (6.5):
  - `V4__create_outbox.sql`, com índice parcial nas linhas pendentes;
  - `OutboxEventJpaEntity` (payload JSONB via `@JdbcTypeCode(SqlTypes.JSON)` sobre `String`) e `OutboxJpaRepository` (`FOR UPDATE SKIP LOCKED`);
  - contrato `SessionScheduledMessage` + `SessionScheduledMessageTest`;
  - `OutboxSessionEventPublisher` grava o evento na mesma transação da sessão; o `LoggingSessionEventPublisher` foi apagado;
  - `OutboxRelay` (`@Scheduled(fixedDelay = 1000)`, lote de 100, envio síncrono) e `KafkaConfig` (`@EnableScheduling`, tópico `catalog.session.scheduled` com 3 partições).
- Verificado:
  - mensagem no tópico com chave = id da sessão e headers `eventId` e `eventType`;
  - `payload` gravado como objeto JSON no banco (a hipótese do mapeamento `String` → JSONB se confirmou);
  - **teste de falha:** com o Kafka parado, o `POST` respondeu 201 e a linha ficou com `published_at` nulo; ao subir o Kafka, foi publicada (~50 s depois da criação);
  - 48 testes passando, incluindo o ArchUnit. (Na hora foi informado "50": a soma incluía um relatório antigo do surefire.)

### Conceitos estudados
- **Dual write:** gravar no banco e publicar no Kafka são duas operações; uma pode falhar sem a outra. O outbox grava o evento na mesma transação da sessão, e o **relay** (retransmissor) leva o evento do banco ao Kafka.
- **Outbox não é fallback:** todo evento passa pela tabela, sempre. "Salvar só o que falhou" perde o evento se a aplicação cair entre o commit e o envio (o evento só existia na memória), quebra a ordem e cria um caminho de código raramente executado.
- **Replicação do Kafka complementa, não substitui:** aumenta a disponibilidade do broker, mas não cobre a queda da aplicação.
- **Polling vs CDC:** o relay consulta o banco a cada 1 s. A alternativa sem polling é CDC (Debezium lendo o log do Postgres), que exige um serviço a mais.
- **`FOR UPDATE SKIP LOCKED`:** trava as linhas lidas até o fim da transação e faz outra instância pular as travadas, em vez de esperar. A trava é liberada no commit ou no rollback (inclusive se a conexão cair).
- **Dirty checking:** entidades carregadas dentro da transação ficam gerenciadas; no commit, o Hibernate compara com o estado original e gera o `UPDATE`. Por isso o `markPublished` dispensa `save`. O commit é feito pelo proxy do `@Transactional` quando o método retorna.
- **Exceções do `Future.get`:** `ExecutionException` e `TimeoutException` são falhas de envio (log e tentar no próximo ciclo); `InterruptedException` é pedido de parada (reativar o sinal com `interrupt()` e sair). Nenhum caminho de erro pode chegar ao `markPublished`.
- **`JSONB` reordena as chaves** do objeto; em JSON a ordem não tem significado.
- Nome de tópico em classe de constantes: enum não aceita o ponto no nome e não serve em anotações (`@KafkaListener(topics = ...)`); `application.yaml` é para valores que mudam por ambiente, e o nome do tópico é contrato entre serviços.

### Decisões
- **Corrigir a V4 editando o arquivo, e não com uma V5.**
  - **Motivo:** a V4 só tinha rodado no banco local e não estava commitada. A regra "não editar migration aplicada" protege migrations compartilhadas.
  - **Como:** `DROP TABLE outbox` + `DELETE FROM flyway_schema_history WHERE version = '4'` no banco local.
  - **Custo:** passo manual. Se a V4 já estivesse no git, a única saída correta seria uma V5.
- **Limpeza do outbox fica para depois** (plano, seção 10): job que apaga linhas publicadas há mais de alguns dias. Hoje o índice parcial ignora as publicadas e o volume é mínimo.

### Problemas e soluções
- `Not a managed type: interface ...OutboxJpaRepository`: o primeiro tipo do `JpaRepository<...>` era a própria interface, e não a entidade.
- Construtor da entidade sem `eventType` e com `publishedAt`, e a chamada do publisher não compilava.
- V4 sem `event_type` e com `published_at NOT NULL`, já aplicada no banco local: ver a decisão acima.
- `required a single bean, but 2 were found`: o `LoggingSessionEventPublisher` ainda existia. Apagado.
- Mensagem "sumida" no Kafka UI: o tópico tinha sido criado como `catolog.session.scheduled` (erro de digitação na constante). Corrigido e o tópico errado apagado.
- `catch` trocados no relay: um `TimeoutException` marcaria o evento como publicado sem confirmação do Kafka. O teste com o Kafka parado não pegava o bug, porque essa falha chega como `ExecutionException`.
- `docker compose` precisa rodar na raiz do projeto, onde está o `docker-compose.yml`.

### Pendências
- 6.6: testes de integração, incluindo "sessão agendada gera mensagem no tópico", concorrência (23P01) e "POST sem campo → 400".
- Seção 10: limpeza das linhas publicadas do outbox.
- Pendências antigas: asserts extras em `failsWhenRoomNotFound`; renomear `acceptTitleAtMaxLength`; teste de "não encontrado" em `SessionQueryService.get`; divergências com o plano (UUID v7, nome do `transaction-service`, ~~versões das imagens do Testcontainers~~ feito em 2026-11-19).

---

## 2026-11-19

### Feito
- Testes de integração (6.6) no catalog:
  - `IntegrationTest`: classe base com `@SpringBootTest`, `@AutoConfigureMockMvc` e os containers, e `TRUNCATE` das tabelas antes de cada teste;
  - `SessionPersistenceAdapterIntegrationTest`: a constraint `EXCLUDE` rejeita sobreposição (traduzida para `SessionOverlapException`) e aceita sessões encostadas;
  - `SessionControllerIntegrationTest` com `MockMvcTester`: 201 + `Location`, 400 por campo ausente, 409, 404 e 400 sem `date`;
  - `OutboxRelayIntegrationTest`: a sessão agendada chega ao tópico com a chave, o header `eventType` e o payload.
- Imagens do Testcontainers fixadas em `postgres:18-alpine` e `apache/kafka:4.4.0` (as mesmas do compose) nos quatro serviços.
- 56 testes passando. Os containers sobem uma vez só na execução inteira.
- Seção 7 planejada: fatias 7.0 a 7.6 e decisões da 7.1 registradas no plano.
- 7.0 (booking): `application.yaml` com `.env`, banco 5434, Kafka e porta 8081; configuração de execução no IntelliJ. A aplicação sobe.
- 7.1, domínio da projeção: `Session` (`register`/`restore`), `SessionId`, `SeatCode` (regex `[A-Z][1-9][0-9]*`), cópias de `Money` e `DomainException`. 17 testes no booking (`SeatCodeTest` parametrizado com `@ValueSource` e `@NullAndEmptySource`).

### Conceitos estudados
- **Cache de contexto do Spring:** classes de teste com configuração idêntica reaproveitam o mesmo contexto (e os mesmos containers). Uma anotação a mais em uma classe cria outro contexto. Por isso a configuração fica toda na classe base, inclusive o `@AutoConfigureMockMvc`.
- **Isolamento por `TRUNCATE` no `@BeforeEach`:**
  - o banco é compartilhado entre os testes e o JUnit não garante a ordem, então cada teste começa limpo;
  - o lixo do último teste some com o container;
  - rollback com `@Transactional` não serve, porque o relay roda em outra thread e o teste da constraint precisa de commits reais.
- **Teste de concorrência determinístico:** com duas threads não se controla quem chega primeiro, e o teste poderia passar pela verificação do service sem tocar na constraint. Chamar a porta com `Session.restore` isola a última linha de defesa.
- **Problem Details:** o campo `detail` está em toda resposta de erro deste projeto, porque o handler herda de `ResponseEntityExceptionHandler`. `$.detail` é JsonPath (`$` = raiz).
- **Dado de teste escolhido de propósito:** com os assentos `A1..A4`, um `HashSet` também devolve a ordem certa (conferido), então o teste não provava o `LinkedHashSet`. Com `B1` primeiro, prova. Do mesmo jeito, `hasMessageContaining` garante qual regra lançou a `DomainException`: sem ela, um `SeatCode` inválido no próprio teste o faria passar.
- **`Clock.fixed` só serve quando é injetado no código testado.** A `Session` do booking não recebe `Clock`, então o teste usa uma constante `Instant`.
- **Awaitility:** repete uma asserção até passar ou estourar o tempo; serve para resultados assíncronos (relay a cada 1 s) no lugar de `Thread.sleep`.
- Um UUID malformado no JSON dá 400 ("Failed to read request") antes de chegar ao service. Para testar 404, o ID precisa ser válido e inexistente (`UUID.randomUUID()`).

### Decisões
- **Imagens fixas e iguais às do compose:** com `latest`, os testes mudariam de versão sozinhos.
- **`MockMvcTester` em vez de servidor HTTP real:** exercita a pilha inteira do Spring MVC, sem porta de rede; para esta API, o servidor real acrescentaria pouco.
- **Booking sem biblioteca compartilhada com o catalog:** `Money`, `DomainException` e o contrato da mensagem são copiados. Uma lib comum acoplaria versões e deploys dos serviços; o custo é duplicar classes pequenas.
- **Projeção mínima e imutável no booking, inbox para idempotência, retry + DLQ:** motivos no plano, seção 7.
- **Ordem da 7.1 trocada: persistência (Parte 2) antes do caso de uso (Parte 3).** Um `@Service` que depende de uma porta sem implementação impede o contexto do Spring de subir e quebraria o `BookingServiceApplicationTests`. A tabela `processed_events` foi para a Parte 4, junto com o consumer, que é quem a usa.
- **Aviso do Mockito ("self-attaching") ignorado por ora:** é só um aviso, no Java 25 tudo funciona. A correção (Mockito como `-javaagent` no surefire) exige configuração extra para o IntelliJ.

### Problemas e soluções
- `returnsNotFoundForUnknownMovie` recebia 400: `"000000-000000-000000"` não é UUID válido. Trocado por `UUID.randomUUID()`.
- `ticket-service` não compilava (string da imagem cortada). Corrigido.
- Contagem de testes errada desde a 6.5 (relatório antigo do surefire somado).
- Booking falhou com `password authentication failed for user "${BOOKING_DB_USER}"`: a configuração de execução nova estava com o Working directory padrão. Com `$MODULE_WORKING_DIR$`, o `../.env` é encontrado. Mesmo problema da configuração do catalog: toda configuração nova precisa desse ajuste.

### Pendências
- 7.1 (booking): Parte 2 (persistência + ArchUnit), Parte 3 (caso de uso `RegisterSession` com fake), Parte 4 (consumer com inbox, retry e DLQ).
- Seção 10: limpeza das linhas publicadas do outbox.
- Aviso do Mockito sobre carregamento dinâmico de agente: configurar `-javaagent` no surefire antes de uma versão do JDK que bloqueie agentes dinâmicos.
- Testes de integração de filmes e salas (só sessões têm, por enquanto), se valer a pena.
- Pendências antigas: asserts extras em `failsWhenRoomNotFound`; renomear `acceptTitleAtMaxLength`; teste de "não encontrado" em `SessionQueryService.get`; UUID v7; nome do `transaction-service`.

## 2026-11-23

### Feito
- 7.1, Parte 2 (persistência da projeção no booking):
  - porta `SessionRepository` (`save`, `findById`);
  - `V1__create_sessions.sql`: `sessions` e `session_seats` (PK `(session_id, seat_code)`, `UNIQUE (session_id, seat_order)`, `CHECK` com a mesma regex do `SeatCode`);
  - `SessionJpaEntity` com `@ElementCollection` + `@OrderColumn(name = "seat_order")`, `SessionJpaRepository` com `@EntityGraph` no `findById`, `SessionPersistenceMapper` e `SessionPersistenceAdapter`;
  - testes: classe base `IntegrationTest`, `SessionPersistenceAdapterIntegrationTest` (ordem B1, A1, A2 preservada após ida e volta ao banco) e `HexagonalArchitectureTest` com ArchUnit.
- 7.1, Parte 3 (caso de uso): `RegisterSessionUseCase` + `RegisterSessionCommand`, `RegisterSessionService` e o fake `InMemorySessionRepository`. Teste escrito antes, visto falhando com o service vazio, depois implementado.
- 24 testes passando no booking.

### Conceitos estudados
- **Proxy CGLIB e construtor visível:** o `@Repository` faz o Spring envolver o bean em um proxy de tradução de exceções. Esse proxy é uma subclasse gerada em tempo de execução e precisa chamar o construtor da classe-mãe; com o construtor `private`, o contexto não sobe ("No visible constructors"). Construtor privado é padrão do domínio; adapter é instanciado pelo Spring e precisa de construtor `public`.
- **`register`/`create` × `restore` (criação × reconstituição):** criar e reconstituir são momentos diferentes do ciclo de vida do agregado (Evans, *Domain-Driven Design*, cap. 6, "Reconstituting Stored Objects").
  - A criação gera a identidade e aplica regras e efeitos que só valem no nascimento (data no futuro, evento de domínio).
  - A reconstituição preserva o ID salvo e não reaplica essas regras: uma sessão já exibida violaria "começa no futuro" e deixaria de carregar; um evento de criação seria republicado a cada leitura.
  - As invariantes estruturais ficam no construtor privado e valem nos dois caminhos. Dois construtores não resolvem, porque as assinaturas podem ser iguais; o nome do *factory method* carrega a intenção (Bloch, *Effective Java*, item 1).
  - Na hexagonal, o `restore` é a porta oficial do adapter de persistência para montar o agregado, sem deixar um construtor público para qualquer camada.
  - No booking os dois corpos ainda são iguais (o ID vem do evento): a separação segue o modelo do catalog, onde a diferença já existe.
- **Teste visto falhando antes da implementação:** o service começa com o corpo vazio, que compila; os testes falham pelo motivo certo (nada salvo, nenhuma exceção). Isso prova que eles conseguem pegar o erro.
- **Assento inválido no meio da lista** (`B1`, `a1`, `A2`): prova que um código inválido derruba a sessão inteira, e o `count()` do fake (método só do fake, fora da porta) prova que nada foi salvo.
- **ArchUnit com pacote vazio:** uma regra cujo `that()` não encontra nenhuma classe falha ("failed to check any classes"). Foi o que denunciou o teste copiado do catalog ainda apontando para `com.cinema.catalog`.

### Decisões
- **Tabela `session_seats`** (igual ao `room_seats` do catalog). A V1 já tinha sido aplicada com outro nome no banco local; o banco foi recriado antes da troca, então o checksum do Flyway confere.
- **Mapper usa `Session.restore`:** ler do banco é reconstituir, não registrar uma sessão nova.
- **Command com tipos do JDK (`UUID`, `Instant`, `BigDecimal`, `List<String>`):** a conversão para objetos de valor fica só no service, então dado inválido sempre vira `DomainException` no mesmo ponto; o listener (Parte 4) só traduz o formato de transporte. Mesmo padrão do `CreateRoomCommand` do catalog.
- **`register` retorna `void`:** quem chama é o consumer, que já tem o ID vindo no evento.
- **Sem checar "sessão já existe" no service:** o catalog publica cada sessão uma vez, com `eventId` único; reentregas trazem o mesmo `eventId` e o inbox da Parte 4 as descarta. Checar com `findById` custaria uma leitura por evento para duplicar essa garantia.
- **`seatCodes` no command, mesmo nome do campo da mensagem do catalog:** a conversão no listener fica um mapeamento direto, sem tradução de nomes.

### Problemas e soluções
- Contexto não subia: construtor `private` no `SessionPersistenceAdapter`. Trocado para `public`.
- ArchUnit falhando: pacote `com.cinema.catalog` no teste copiado. Trocado para `com.cinema.booking`.
- Testes de integração com "Could not find a valid Docker environment": o Docker Desktop estava fechado.

### Pendências
- 7.1 (booking): Parte 4 (consumer de `SessionScheduled` com inbox `processed_events`, retry e DLQ).
- Exercício `returnsEmptyForUnknownSession` no `SessionPersistenceAdapterIntegrationTest`.
- Seção 10: limpeza das linhas publicadas do outbox.
- Aviso do Mockito sobre carregamento dinâmico de agente: configurar `-javaagent` no surefire.
- Testes de integração de filmes e salas, se valer a pena.
- Pendências antigas: asserts extras em `failsWhenRoomNotFound`; renomear `acceptTitleAtMaxLength`; teste de "não encontrado" em `SessionQueryService.get`; UUID v7; nome do `transaction-service`.
