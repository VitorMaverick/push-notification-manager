# PushNotificationManager

A full-stack push notification management system for web browsers, built as a Computer Science undergraduate thesis at **IFMA (Instituto Federal do Maranhão)**.

The project serves as a practical case study on applying **Hexagonal Architecture** and **GoF design patterns** to build modular, testable, and technology-resilient notification systems.

---

## Entendendo a Arquitetura do Ecossistema

Este não é um projeto monolítico tradicional. Ao longo do desenvolvimento, o sistema evoluiu de um monolito JHipster para uma **arquitetura de microserviços orientada a eventos**. Para entender o porquê e o como, vamos começar pelos conceitos fundamentais.

### Por que microserviços?

Imagine que você tem uma loja física onde o caixa, o estoque e o atendimento ao cliente são feitos por uma única pessoa. Enquanto a loja é pequena, funciona. Mas quando cresce, essa pessoa vira gargalo: se o estoque trava, o caixa para; se o atendimento demora, tudo atrasa.

No software, isso é um **monolito** — todo o código vive em um único processo. O PushNotificationManager nasceu assim: autenticação, registro de dispositivos, envio de notificações e frontend, tudo no mesmo JAR. Funcionava, mas tinha três dores:

1. **Acoplamento de implantação** — qualquer mudança (mesmo no envio de push) exigia reimplantar a aplicação inteira
2. **Acoplamento de escala** — se o Firebase ficasse lento, não era possível escalar apenas o componente de envio
3. **Fragilidade** — uma falha no Firebase significava notificação perdida para sempre, sem retry

A solução foi **extrair responsabilidades em serviços independentes**, cada um com seu banco de dados, sua lógica e seu ciclo de deploy.

### A decomposição em 3 serviços

```
┌──────────────────────────────────────────────────────────────────┐
│                         BROWSER                                   │
│            React SPA + Service Worker (FCM)                       │
└──────┬────────────────────┬────────────────────┬─────────────────┘
       │ :8080              │ :8081              │ :8082
       │                    │                    │
┌──────▼──────────┐  ┌─────▼──────────┐  ┌─────▼──────────────────┐
│   MONOLITO      │  │ DEVICE-SERVICE │  │ NOTIFICATION-SERVICE    │
│   (Gateway)     │  │                │  │                         │
│                 │  │ • Registro de  │  │ • Envio push via FCM    │
│ • Login JWT     │  │   tokens FCM   │  │ • Histórico e filtros   │
│ • Gestão users  │  │ • Ciclo de vida│  │ • ACK de entrega        │
│ • Frontend React│  │   do device    │  │ • Retry automático      │
│ • Service Worker│  │ • Publica evento│  │ • Dead Letter Queue     │
│                 │  │   no RabbitMQ  │  │                         │
└────────┬────────┘  └───────┬────────┘  └────────┬───────────────┘
         │                   │ publish             │ consume
         │           ┌───────▼─────────────────────▼───────┐
         │           │            RABBITMQ                  │
         │           │                                      │
         │           │  • device.registered.queue           │
         │           │  • notification.retry.queue          │
         │           │  • notification.dlq                  │
         └───────────┴──────────────────────────────────────┘
```

| Serviço | Porta | Responsabilidade | Banco |
|---------|-------|------------------|-------|
| **Monolito** (Gateway) | 8080 | Autenticação JWT, gestão de usuários, serve o frontend React | PostgreSQL (:5432) |
| **device-service** | 8081 | Registro e gestão de tokens FCM dos dispositivos | PostgreSQL (:5434) |
| **notification-service** | 8082 | Envio de push via Firebase, histórico, retry/DLQ | PostgreSQL (:5433) |

### Arquitetura Hexagonal: o coração dos microserviços

Cada microserviço segue a **Arquitetura Hexagonal** (ou Ports and Adapters), proposta por Alistair Cockburn. A ideia é simples e poderosa: o código de negócio (domínio) fica no centro, completamente isolado do mundo externo. A comunicação acontece por meio de **portas** (interfaces) e **adaptadores** (implementações concretas).

```
          ┌─────────────────────────┐
          │      ADAPTADORES        │  ← Mundo externo
          │  (REST, FCM, JPA, MQ)   │
          └────────────┬────────────┘
                       │ implementa
          ┌────────────▼────────────┐
          │        PORTAS           │  ← Contratos (interfaces)
          │  (PushSenderPort,       │
          │   DeviceRepositoryPort) │
          └────────────┬────────────┘
                       │ usa
          ┌────────────▼────────────┐
          │       DOMÍNIO           │  ← Regras de negócio puras
          │  (Use Cases, Entities,  │
          │   Value Objects)        │
          └─────────────────────────┘
```

**Por que isso importa?** Porque amanhã, se o Google descontinuar o Firebase (como já fez com o Google Cloud Messaging), basta criar um novo adaptador — digamos `ApnsSenderAdapter` — que implementa a mesma porta `PushSenderPort`. O domínio não muda. Os testes do domínio continuam passando. Zero impacto nas regras de negócio.

### Comunicação assíncrona: RabbitMQ e eventos

Os microserviços não se chamam diretamente (nada de HTTP síncrono entre eles). Eles se comunicam por **eventos** via RabbitMQ:

1. Quando um dispositivo é registrado, o `device-service` publica um evento `DeviceRegisteredEvent`
2. O `notification-service` consome esse evento e pode, por exemplo, enviar uma notificação de boas-vindas

Para lidar com falhas no envio (Firebase fora do ar, token expirado etc.), o sistema implementa um **mecanismo de retry com Dead Letter Queue**:

- Notificação falha → vai para `notification.retry.queue` (TTL de 15 segundos)
- Após o TTL, é reprocessada automaticamente (até 3 tentativas)
- Falhas permanentes (token inválido) → vão direto para `notification.dlq`

### O monolito: por que ainda existe?

O monolito não desapareceu — ele evoluiu para um **gateway**. Sua função agora é:

- **Servir o frontend React** (Single Page Application)
- **Autenticar usuários** e emitir tokens JWT
- **Hospedar o Service Worker** que recebe pushes do Firebase

Ele segue o padrão **MVC convencional** do JHipster (model, repository, service, web), sem a complexidade hexagonal — porque autenticação não precisa de troca de provedor como o push precisa.

### Segurança: JWT compartilhado

Os três serviços compartilham a mesma chave secreta JWT (`JWT_BASE64_SECRET`). O monolito **emite** o token; os microserviços **validam**. Assim, um único login no frontend garante acesso autenticado a todos os serviços.

### Padrões de Projeto aplicados

| Padrão | Onde | Problema resolvido |
|--------|------|--------------------|
| **Strategy** | `PushSenderPort` → `FcmService` | Trocar provedor de push sem alterar regras |
| **Observer** | Service Worker + FCM | Dispositivo reage automaticamente a eventos push |
| **Builder** | `PushNotification.builder()` | Construção segura de objetos complexos |
| **Adapter** | `FcmService` wrapping Firebase SDK | Traduz domínio para API externa |
| **Factory** | `DeviceRepositoryAdapter` | Isola entidades JPA do domínio |
| **Specification** | Filtros de histórico | Queries dinâmicas sem if-else em cascata |

### Como tudo se conecta na prática

O ciclo completo de uma notificação push:

1. **Usuário abre o app** → frontend (React) pede permissão ao browser
2. **Service Worker se registra** → obtém token FCM do Firebase
3. **Frontend envia token** → `POST device-service/api/v1/devices` → persiste no banco
4. **device-service publica evento** → `DeviceRegisteredEvent` no RabbitMQ
5. **Admin envia notificação** → `POST notification-service/api/v1/notifications`
6. **Use case orquestra** → persiste (PENDING), chama `PushSenderPort.send()`, atualiza (SENT)
7. **Firebase entrega** → Service Worker recebe, exibe notificação nativa
8. **Service Worker envia ACK** → `POST /api/v1/notifications/ack` → status vira DELIVERED
9. **Se falhou** → evento de retry → RabbitMQ → reprocessa até 3x → DLQ se permanente

---

## What it does

- **Device registration** — registers browser FCM tokens with metadata (platform, user-agent, status ACTIVE/INACTIVE)
- **Push notification delivery** — sends notifications (title, body, payload) to registered devices via Firebase Cloud Messaging
- **Full lifecycle tracking** — persists each notification with status transitions: `PENDING → SENT → DELIVERED / FAILED`
- **Delivery confirmation (ACK)** — Service Worker sends an ACK to the backend upon receiving the push event, updating status to `DELIVERED` with timestamp
- **Notification history** — paginated and filterable REST endpoints (by status, device, and date range)
- **Admin interface (React)** — requests browser permission, registers device token, sends notifications, and displays real-time history

---

## Architecture

The system is built on **Hexagonal Architecture (Ports and Adapters)**, isolating the business core from infrastructure details. This means the push provider (FCM), database, or any external service can be swapped without touching business rules.

```
┌─────────────────────────────────────────────┐
│                   Frontend                  │
│        React + Service Worker (FCM)         │
└───────────────────┬─────────────────────────┘
                    │ REST API
┌───────────────────▼─────────────────────────┐
│              Input Adapters                 │
│         NotificationController              │
│            DeviceController                 │
└───────────────────┬─────────────────────────┘
                    │
┌───────────────────▼─────────────────────────┐
│               Domain Core                  │
│    SendPushNotificationUseCase              │
│    RegisterDeviceUseCase                    │
│    Ports (PushSenderPort, DeviceRepository) │
└──────────┬────────────────────┬─────────────┘
           │                    │
┌──────────▼──────┐   ┌─────────▼─────────────┐
│ Output Adapters │   │   Output Adapters      │
│   FcmService    │   │  JPA Repositories      │
│ (Firebase FCM)  │   │   (PostgreSQL)         │
└─────────────────┘   └────────────────────────┘
```

---

## Design Patterns Applied

| Pattern                           | Where applied                      | Problem solved                                                                   |
| --------------------------------- | ---------------------------------- | -------------------------------------------------------------------------------- |
| **Hexagonal Architecture**        | Entire system                      | Decouples business logic from FCM, database, and frameworks                      |
| **Strategy**                      | `PushSenderPort` + `FcmService`    | Push provider can be swapped (FCM → APNs → OneSignal) without changing use cases |
| **Observer**                      | Service Worker + FCM               | Browser subscribes to push events and notifies the UI automatically              |
| **Builder**                       | Notification object construction   | Safe construction of complex objects with many optional fields                   |
| **Adapter**                       | `FcmService` wrapping Firebase SDK | Translates domain port calls to Firebase-specific API calls                      |
| **Factory / Mapper**              | Persistence layer                  | Isolates domain entities from JPA models                                         |
| **Presenter**                     | REST response shaping              | Decouples API response format from domain objects                                |
| **Centralized Exception Handler** | `@ControllerAdvice`                | Consistent error responses across all endpoints                                  |
| **Facade** _(frontend)_           | Firebase client helper             | Simplifies Firebase SDK initialization for React components                      |
| **Gateway** _(frontend)_          | axios modules                      | Centralizes all HTTP communication with the backend                              |
| **Boundary/Adapter** _(frontend)_ | Service Worker                     | Bridges browser push events to application logic                                 |

---

## Tech Stack

**Backend**

- Java 21 + Spring Boot 3
- Spring Security, Spring Data JPA
- Firebase Admin SDK (FCM)
- PostgreSQL
- Maven
- JHipster 9

**Frontend**

- React + TypeScript
- Firebase JS SDK
- Service Worker (Web Push API)
- Axios

**Infrastructure**

- Docker + Docker Compose
- Prometheus + Grafana (monitoring)
- Swagger / OpenAPI (API docs)

**Testing**

- JUnit 5 (unit tests)
- Cypress (end-to-end tests)
- SonarQube (code quality)

---

## API Endpoints

| Method | Endpoint                     | Description                                       |
| ------ | ---------------------------- | ------------------------------------------------- |
| `POST` | `/api/devices`               | Register a device FCM token                       |
| `GET`  | `/api/devices`               | List registered devices                           |
| `POST` | `/api/notifications`         | Send a push notification                          |
| `GET`  | `/api/notifications`         | List notification history (paginated, filterable) |
| `POST` | `/api/internal/firebase/ack` | Confirm delivery (called by Service Worker)       |

---

## Running locally

The project runs in two modes: **monolith only** (quick start) or **full microservices stack** (all services). For testing the complete push notification lifecycle, use the full stack.

### Prerequisites

| Tool                    | Version    | Notes                                                                                    |
| ----------------------- | ---------- | ---------------------------------------------------------------------------------------- |
| Java                    | 21         | Use [SDKMAN](https://sdkman.io/): `sdk use java 21.0.2-open` — a `.sdkmanrc` is provided |
| Node.js                 | 18+ LTS    | Required for the React frontend                                                          |
| Docker + Docker Compose | any recent | Runs RabbitMQ, PostgreSQL databases, and microservices                                   |

A `.sdkmanrc` file at the project root pins Java 21 automatically if you use SDKMAN.

---

### Option 1 — Monolith only (quickest)

```bash
# 1. Start the monolith PostgreSQL
docker compose -f src/main/docker/postgresql.yml up -d

# 2. Start the application (builds frontend + backend)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

App: `http://localhost:8080`

> If the frontend shows "An error has occurred", the webpack cache may be stale.
> Fix: `rm -rf target/webpack/ && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`

---

### Option 2 — Full microservices stack (recommended for notification testing)

This runs the monolith (gateway + UI), device-service, notification-service, RabbitMQ, and both PostgreSQL databases.

#### Step 1 — Configure environment variables

Create a `.env` file at the project root (already provided in the repo):

```env
JWT_BASE64_SECRET=<base64-encoded-512-bit-secret>   # must match monolith jhipster config
FIREBASE_KEY_PATH=./secrets/firebase-service-account.json
RABBITMQ_USER=guest
RABBITMQ_PASS=guest
```

Place the Firebase service account JSON at `secrets/firebase-service-account.json`.

#### Step 2 — Start infrastructure + microservices (Docker)

```bash
# Start RabbitMQ, device-db, notification-db, device-service, notification-service
docker compose up -d

# Verify all containers are healthy
docker compose ps
```

Expected output:

| Container              | Port                         | Status  |
| ---------------------- | ---------------------------- | ------- |
| `rabbitmq`             | 5672 / 15672 (management UI) | healthy |
| `device-db`            | 5434                         | healthy |
| `notification-db`      | 5433                         | healthy |
| `device-service`       | 8081                         | running |
| `notification-service` | 8082                         | running |

#### Step 3 — Start the monolith PostgreSQL

```bash
docker compose -f src/main/docker/postgresql.yml up -d
```

#### Step 4 — Start the monolith

```bash
# Skip npm rebuild if assets are already built (faster restart)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dskip.npm=true -Denforcer.skip=true

# Or full build (first run / after frontend changes):
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Step 5 — Verify everything is up

```bash
curl http://localhost:8080/management/health   # monolith
curl http://localhost:8081/actuator/health     # device-service
curl http://localhost:8082/actuator/health     # notification-service
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/firebase-messaging-sw.js  # must return 200
```

All should return `{"status":"UP"}` (200).

**Service map:**

```
┌──────────────────────┬──────────────────────────────────────┬─────────┐
│       Serviço        │                 URL                  │ Status  │
├──────────────────────┼──────────────────────────────────────┼─────────┤
│ Monolith (UI)        │ http://localhost:8080                │ UP      │
├──────────────────────┼──────────────────────────────────────┼─────────┤
│ device-service       │ http://localhost:8081                │ UP      │
├──────────────────────┼──────────────────────────────────────┼─────────┤
│ notification-service │ http://localhost:8082                │ UP      │
├──────────────────────┼──────────────────────────────────────┼─────────┤
│ RabbitMQ UI          │ http://localhost:15672 (guest/guest) │ UP      │
├──────────────────────┼──────────────────────────────────────┼─────────┤
│ device-db            │ :5434                                │ healthy │
├──────────────────────┼──────────────────────────────────────┼─────────┤
│ notification-db      │ :5433                                │ healthy │
└──────────────────────┴──────────────────────────────────────┴─────────┘
```

| Service                               | Purpose                              |
| ------------------------------------- | ------------------------------------ |
| Monolith UI                           | React frontend + Spring Boot gateway |
| Swagger UI (`/swagger-ui/index.html`) | API documentation                    |
| device-service                        | Registers FCM device tokens          |
| notification-service                  | Sends push notifications via FCM     |
| RabbitMQ management                   | Message broker UI                    |

---

### Testing the push notification lifecycle in the browser

The complete lifecycle is: **browser requests permission → FCM token registered → notification sent → Service Worker delivers it → ACK sent back**.

#### Step 1 — Open the app and sign in

1. Open `http://localhost:8080` in a browser that supports Web Push (Chrome, Edge, Firefox)
2. Sign in with admin credentials (default JHipster: `admin` / `admin`)

#### Step 2 — Grant notification permission

1. Navigate to the **Notifications** or **Device** section in the UI
2. Click **"Obtain FCM Token From Browser"**
3. The browser will prompt: **"Allow notifications?"** — click **Allow**
4. The app registers your FCM token with `device-service` (`POST /api/devices`)

> **Troubleshooting:** If you see `ServiceWorker script evaluation failed`, check that:
>
> - `http://localhost:8080/firebase-messaging-sw.js` returns HTTP 200
> - The `Content-Security-Policy` header includes `https://www.gstatic.com` in `script-src`

#### Step 3 — Send a test notification

1. Navigate to **Send Notification** in the UI
2. Fill in **Title** and **Body**
3. Select the device you just registered
4. Click **Send**
5. The monolith publishes a message to RabbitMQ → `notification-service` consumes it → sends to FCM

Expected result: a native browser notification appears in the corner of your screen.

#### Step 4 — Observe the full event flow via RabbitMQ

Open `http://localhost:15672` (guest/guest) and check:

- **Exchanges:** `device.exchange` (topic) and `notification.retry.exchange` (direct)
- **Queues:** `device.registered.queue`, `notification.retry.queue`, `notification.dead.letter.queue`
- **Message rates:** watch the publish/deliver graph while sending notifications

#### Step 5 — Verify notification status history

```bash
# List notifications (replace <token> with a valid JWT from the login response)
curl -H "Authorization: Bearer <token>" http://localhost:8082/api/notifications
```

Status transitions: `PENDING → SENT → DELIVERED` (after Service Worker ACK) or `FAILED` (if FCM rejects).

#### Step 6 — Test the retry flow (optional)

To trigger the retry/DLQ path, send a notification with an invalid FCM token:

```bash
curl -X POST http://localhost:8081/api/devices \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"fcmToken":"invalid-token-for-retry-test","platform":"WEB","userId":"test"}'
```

After ~15 seconds the message moves from `notification.retry.queue` to `notification.dead.letter.queue`. Monitor in the RabbitMQ UI.

---

### Stopping all services

```bash
# Stop microservices + infrastructure
docker compose down

# Stop monolith PostgreSQL
docker compose -f src/main/docker/postgresql.yml down

# Stop monolith (if running in foreground: Ctrl+C)
# If running in background:
pkill -f "spring-boot:run"
```

---

## Development reference

> Generated with [JHipster 9.0.0-beta.0](https://www.jhipster.tech/documentation-archive/v9.0.0-beta.0). See full docs for advanced configuration.

**Common commands**

```bash
# Run tests
./mvnw verify                  # backend unit tests
npm test                       # frontend unit tests (Jest)
npm run e2e                    # end-to-end tests (Cypress)

# Build for production
./mvnw -Pprod clean verify     # backend production build
npm run build                  # frontend production build

# Code quality
./mvnw sonar:sonar             # SonarQube analysis

# Docker (full stack)
npm run java:docker            # build Docker image
docker compose -f src/main/docker/app.yml up -d
```

---

## Academic context

**Title:** _Utilização de Padrões de Projeto Arquiteturais e de Design na Implementação de Sistemas de Notificação Push_

**Institution:** IFMA — Instituto Federal de Educação, Ciência e Tecnologia do Maranhão

**Course:** Bacharelado em Sistemas de Informação

**Advisor:** Prof. Dr. Helder Pereira Borges

**Author:** Vitor Maverick Fonseca dos Santos
