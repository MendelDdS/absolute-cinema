# Plano de implementação — Sistema de reserva de assentos (cinema)

Projeto de estudo que simula uma implementação real: decisões de produção no código; simplificações apenas no ambiente local, sempre identificadas.

---

## 1. Visão geral

Sistema de reserva de assentos para sessões de cinema, dividido em 4 microsserviços que se comunicam por eventos no Kafka.

| Serviço | Responsabilidade |
|---|---|
| `catalog-service` | Filmes, salas (mapa de assentos) e sessões |
| `booking-service` | Reserva temporária de assentos, confirmação e expiração |
| `transactions-service` | Processamento do pagamento (gateway simulado) |
| `ticket-service` | Emissão e validação de ingressos |

### Fluxo de eventos

```
catalog-service ──SessionScheduled──▶ booking-service
booking-service ──PaymentRequested──▶ transactions-service
transactions-service ──PaymentApproved / PaymentRejected──▶ booking-service
booking-service ──BookingConfirmed──▶ ticket-service
```

### Tópicos Kafka

| Tópico | Produtor (dono) | Consumidor |
|---|---|---|
| `catalog.session.scheduled` | catalog | booking |
| `booking.payment.requested` | booking | transactions |
| `transactions.payment.result` | transactions | booking |
| `booking.booking.confirmed` | booking | ticket |
| `<tópico>.dlq` | — | um por consumidor |

Regras: o serviço produtor cria o tópico (bean `NewTopic`); a chave da mensagem é o ID do agregado (ordem garantida por agregado).

---

## 2. Stack

- Java 21 ou 25 (LTS), Spring Boot, Maven (monorepo com `../pom.xml` agregador; cada serviço com seu Maven Wrapper)
- PostgreSQL 18 (um container por serviço), Flyway
- Apache Kafka 4.x em modo KRaft, Kafka UI (kafbat)
- Testes: JUnit 5, AssertJ, Testcontainers, ArchUnit
- Docker Compose para o ambiente local
- `uuid-creator` para UUID v7 (no JDK 26+, `UUID.ofEpochMillis`)

---

## 3. Decisões de arquitetura

### 3.1 Arquitetura hexagonal por serviço
Cada microsserviço é um hexágono independente. Regras:
- **Domínio** sem nenhuma dependência de framework (verificado com ArchUnit).
- **Application** (casos de uso e portas) não depende de adapters (web, JPA, Kafka).
- **Adapters de entrada** nunca chamam **adapters de saída** diretamente; sempre passam por um caso de uso.

### 3.2 Estrutura de pacotes (por camada, agregados dentro do domínio)
```
com.cinema.<serviço>/
├── <Serviço>Application.java
├── domain/
│   ├── model/<agregado>/      # entidades, value objects, eventos de domínio
│   ├── model/shared/          # value objects compartilhados (ex.: Money)
│   └── exception/
├── application/
│   ├── port/in/               # interfaces de casos de uso + commands
│   ├── port/in/query/         # interfaces de consulta + filtros
│   ├── port/out/              # repositórios e publicadores (interfaces)
│   ├── port/out/query/        # repositórios de leitura (quando necessários)
│   ├── query/                 # views (modelos de leitura)
│   ├── service/               # implementações dos casos de uso
│   ├── service/query/
│   └── exception/             # NotFoundException e derivadas
├── adapter/
│   ├── in/web/                # controllers, DTOs de request, GlobalExceptionHandler
│   ├── in/kafka/              # consumers
│   └── out/
│       ├── persistence/       # adapters, entity/, repository/, mapper/
│       ├── outbox/            # publisher (porta) + relay
│       └── kafka/message/     # contratos das mensagens
└── config/                    # beans de infraestrutura (tempo, Kafka, casos de uso)
```
Testes espelham os pacotes em `src/test/java`.

### 3.3 Decisões de domínio
- **Construtor privado + `create` / `restore`**: objetos nunca existem inválidos; `restore` reconstrói dados do banco sem regras de criação.
- **IDs como `record`** (`MovieId`, `RoomId`…), **UUID v7 gerado na aplicação**: objeto nasce com ID, não adivinhável na URL/QR Code, único entre serviços, ordenado no índice.
- **Agregados se referenciam por ID**, não por objeto.
- **Tempo**: `Instant` para momentos, `Duration` para durações, `LocalDate` + `ZoneId` para "dia do cinema", `Clock` injetado para o "agora". Banco em `TIMESTAMPTZ`.
- **Value objects**: `Money` (não negativo, 2 casas), `Seat` (fileira A–Z, número > 0).

### 3.4 Escrita × leitura (CQRS leve)
- Escrita passa pelo domínio (repositórios devolvem agregados).
- Leitura devolve **views** prontas para tela.
- Repositório de leitura separado **só quando o agregado não atende a tela** (joins/agregações). Ex.: `SessionQueryRepository` (join sessão + filme + sala). Filmes e salas reutilizam o repositório de escrita (`findAll`).

### 3.5 Persistência
- **Database per service**: cada serviço tem seu container PostgreSQL; o serviço gerencia o schema com Flyway; o provisionamento é da infraestrutura.
- Entidades JPA separadas do domínio; mappers manuais (Data Mapper).
- `ddl-auto: validate`, `open-in-view: false`.
- Regras críticas também no banco: `CHECK`, `UNIQUE` e **exclusion constraint** (`sessions_no_overlap`) contra sobreposição de sessões.
- Violação de constraint traduzida no adapter para exceção de domínio (`saveAndFlush` + SQLState `23P01`).
- Migrations aplicadas nunca são editadas; mudanças entram como nova versão.

### 3.6 Mensageria
- **Transactional Outbox**: evento gravado na tabela `outbox` na mesma transação do agregado; `OutboxRelay` (`@Scheduled`, `FOR UPDATE SKIP LOCKED`) envia ao Kafka e marca `published_at`.
- **Contrato da mensagem separado do evento de domínio** (records em `adapter/out/kafka/message`, tipos simples, datas ISO 8601).
- Entrega **at-least-once**: header `eventId` (UUID v7) e **consumidores idempotentes** (tabela de eventos processados).
- `acks: all`; retry + **DLQ** nos consumidores.

### 3.7 API
- REST, erros em **Problem Details (RFC 9457)** via `GlobalExceptionHandler`:
  validação/`DomainException` → 400, `NotFoundException` → 404, conflitos de negócio → 409.
- DTOs de request separados dos commands; views podem ser respondidas diretamente.
- **Validação de entrada: ✅ Decidido — só no domínio** (sem Bean Validation nos requests). Cada regra existe em um lugar só, já testado; custo aceito: erros vêm um por vez. Revisitar (validar também no request) se surgir front-end com formulário ou documentação OpenAPI.
- `POST` de criação responde `201 Created` + `Location`.

### 3.8 Anotações na camada `application` — ✅ Decidido: opção A (pragmática)
| Opção | Descrição |
|---|---|
| **A — Pragmática** ✅ | `@Service` + `@Transactional` nos services. Segue a hexagonal (núcleo não depende de adapters). Mais comum no mercado. |
| **B — Estrita** | Services sem anotações; beans e transações em `config/<Agregado>UseCaseConfig` com `TransactionTemplate`. Segue também a regra "núcleo sem framework". |

**Justificativa:** a hexagonal exige que o núcleo não dependa de atores externos (banco, mensageria, web); anotações de DI e transação são metadados de container, não infraestrutura, e podem ser removidas mecanicamente se necessário. A opção B cobraria `@Bean` + decorator + `TransactionTemplate` por caso de uso em troca de uma portabilidade que o projeto não usará. É a abordagem das referências de hexagonal com Spring (Hombergs, *Get Your Hands Dirty on Clean Architecture*). Os services continuam instanciáveis com `new` nos testes unitários.

**Regras (ArchUnit):**
- `domain` não depende de nada fora do JDK.
- `application` não depende de `adapter`, `config`, `jakarta.persistence`, `org.springframework.data`, Kafka nem web.
- `application` pode usar, do Spring, apenas `org.springframework.stereotype` e `org.springframework.transaction`.
- Proibido na `application`: `@Autowired` em campo, `@Value`, `ApplicationEventPublisher` e tipos de adapters.

---

## 4. Ambiente local (Docker Compose) ✅

- [x] `catalog-db` (5433), `booking-db` (5434), `transactions-db` (5435), `ticket-db` (5436) — `postgres:18-alpine`, volumes em `/var/lib/postgresql`, healthcheck
- [x] `kafka` (`apache/kafka`, KRaft, listeners `INTERNAL kafka:9092` / `EXTERNAL localhost:9094`, auto-criação de tópicos desligada)
- [x] `kafka-ui` em `http://localhost:8090`
- [x] `.env` com credenciais (simplificação local)

Conexões: pela IDE → `localhost:<porta>`; em container → nome do serviço + porta interna.

---

## 5. Forma de trabalho

Implementação em **fatias verticais por agregado**: cada fatia vai do domínio ao HTTP antes de começar a próxima, sempre implementando uma peça e, em seguida, quem a usa.

Ordem por fatia:
1. Domínio + testes unitários
2. Portas e casos de uso + testes com fakes
3. Migration, entidade JPA, repositório Spring Data, mapper, adapter de persistência
4. Adapter de entrada (controller, DTOs) + tratamento de erros
5. Teste manual (curl) e testes de integração (Testcontainers)

---

## 6. `catalog-service`

### 6.1 Base (todas as fatias)
- [x] Domínio: `Movie`, `Room`, `Seat`, `Session`, IDs, `Money`, exceptions
- [x] Testes unitários do domínio
- [x] Evento de domínio `SessionScheduled`
- [x] Casos de uso de escrita e consulta + testes com fakes (incluindo conversão de fuso)
- [x] Migrations `V1` (movies), `V2` (rooms, room_seats), `V3` (sessions + exclusion constraint)
- [x] Entidades JPA, repositórios Spring Data, mappers
- [x] Adapters de persistência (`Movie`, `Room`, `Session`, `SessionQuery`)
- [x] `application.yml` (datasource, `cinema.time-zone`) + `flyway-database-postgresql`
- [x] `TimeConfig` (`Clock`, `ZoneId`)
- [x] Leitura de filmes e salas reutiliza `MovieRepository`/`RoomRepository` (3.4); `MovieQueryRepository`/`RoomQueryRepository` removidos
- [x] Aplicação sobe contra o `catalog-db` (Flyway V1–V3 aplicadas, `ddl-auto: validate` ok)
- [x] Decisão 3.8 aplicada (opção A; services já anotados)
- [x] `HexagonalArchitectureTest` (ArchUnit 1.4.1): domínio só JDK; application só JDK + domain + `stereotype`/`transaction`; adapter.in não usa adapter.out

### 6.2 Fatia: Filme
- [x] `CreateMovieRequest` + `MovieController` (`POST /movies`, `GET /movies`, `GET /movies/{id}`)
- [x] `GlobalExceptionHandler` (Problem Details; herda `ResponseEntityExceptionHandler` para erros do Spring saírem no mesmo formato)
- [x] `LoggingSessionEventPublisher` temporário (para a aplicação subir antes do Outbox)
- [x] Teste manual com curl (201, 400, 404)

### 6.3 Fatia: Sala
- [x] `CreateRoomRequest` + `RoomController` (`POST /rooms`, `GET /rooms`, `GET /rooms/{id}`; o `GET` por id foi adicionado para o `Location` do `POST` apontar para um recurso que existe)
- [x] Teste manual (201, 200, 400 com 27 fileiras e nome > 100, 404)
- [x] Nome da sala limitado a 100 caracteres no domínio (coluna `VARCHAR(100)`)

### 6.4 Fatia: Sessão
- [ ] `ScheduleSessionRequest` + `SessionController` (`POST /sessions`, `GET /sessions?movieId=&date=`, `GET /sessions/{id}`)
- [ ] Tratamento de 409 (sobreposição)
- [ ] Teste manual

### 6.5 Outbox e Kafka
- [ ] `V4__create_outbox.sql`
- [ ] `OutboxEventJpaEntity`, `OutboxJpaRepository`
- [ ] `SessionScheduledMessage` (contrato)
- [ ] `OutboxSessionEventPublisher` (substitui o publisher temporário)
- [ ] `OutboxRelay` + `KafkaConfig` (`NewTopic`, `@EnableScheduling`)
- [ ] Verificar evento no Kafka UI

### 6.6 Testes de integração
- [ ] Base com Testcontainers (`postgres:18-alpine`, Kafka) e `@ServiceConnection`
- [ ] Persistência: constraint de sobreposição em concorrência → `SessionOverlapException`
- [ ] Web: endpoints e códigos de erro
- [ ] Outbox: sessão agendada gera mensagem no tópico

---

## 7. `booking-service`

### Domínio
- `Booking` (agregado): `HELD` → `PAYMENT_PENDING` → `CONFIRMED`; `HELD` → `EXPIRED`; `PAYMENT_PENDING` → `CANCELLED`; `HELD` → `CANCELLED`
- `SeatHold` / reserva de assento com prazo de expiração
- Regra: um assento só pode estar em uma reserva ativa por sessão

### Implementação
- [ ] Consumer de `SessionScheduled` → projeção local de sessões e assentos (idempotente)
- [ ] `POST /bookings` (hold) com garantia no banco (constraint parcial / lock) → 409 em conflito
- [ ] `GET /sessions/{id}/seats` (mapa com status)
- [ ] `POST /bookings/{id}/payment` → `PAYMENT_PENDING` + `PaymentRequested` via Outbox
- [ ] Consumer de `PaymentApproved/Rejected` → `CONFIRMED`/`CANCELLED` + `BookingConfirmed` via Outbox
- [ ] Job de expiração de holds (`Clock` injetado)
- [ ] `DELETE /bookings/{id}` (cancelamento)
- [ ] Tabela de eventos processados (idempotência), retry + DLQ
- [ ] Testes de concorrência (dois clientes, mesmo assento)

---

## 8. `transactions-service`

- [ ] Domínio: `Transaction` (`PENDING`, `APPROVED`, `REJECTED`), `IdempotencyKey` (= `bookingId`, `UNIQUE` no banco)
- [ ] Porta `PaymentGateway` + `FakePaymentGatewayAdapter` (regras simuladas de aprovação/recusa)
- [ ] Consumer de `PaymentRequested` (idempotente)
- [ ] Resultado publicado via Outbox em `transactions.payment.result`
- [ ] Sem API pública (apenas Actuator)

---

## 9. `ticket-service`

- [ ] Domínio: `Ticket`, `TicketCode` único, status `VALID` / `USED`
- [ ] Consumer de `BookingConfirmed` → um ingresso por assento (idempotente)
- [ ] `GET /tickets?bookingId=`
- [ ] `POST /tickets/{code}/validate` → 409 se já utilizado

---

## 10. Transversal

- [ ] ArchUnit em todos os serviços
- [ ] Actuator: `health` (liveness/readiness) para healthchecks
- [ ] springdoc-openapi (`/v3/api-docs`) para gerar tipos do frontend
- [ ] Dockerfile multi-stage por serviço + serviços no `../docker-compose.yml` (profile `docker`)
- [ ] Configuração 100% por variáveis de ambiente; logs em stdout; serviços stateless
- [ ] Limpeza periódica de eventos publicados no Outbox

---

## 11. Frontend

React + TypeScript + Vite + Tailwind v4 + shadcn/ui + React Router + TanStack Query + React Hook Form/Zod + MSW + Vitest/Playwright.

- [ ] Fluxo do cliente: em cartaz → sessões → mapa de assentos → checkout com contagem regressiva → status do pagamento (polling) → ingressos com QR Code
- [ ] Área administrativa: filmes, salas, sessões, validação de ingressos
- [ ] Proxy do Vite para os serviços; mocks com MSW até o backend estar pronto

---

## 12. Futuro

- **AWS (teoria + mock)**: mapeamento para ECS Fargate/ECR, RDS PostgreSQL, MSK, Secrets Manager, CloudWatch, ALB/API Gateway, S3 + CloudFront; Terraform; LocalStack com S3 (PDF do ingresso), SES (e-mail) e Secrets Manager como novos adapters de saída.
- **Kubernetes (opcional)**: manifests em `k8s/`, kind/minikube, Helm charts para Kafka e PostgreSQL.
- Otimizações: `Persistable` nas entidades com ID atribuído (evita SELECT antes do INSERT).
