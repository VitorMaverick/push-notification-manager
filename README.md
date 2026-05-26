# PushNotificationManager

A full-stack push notification management system for web browsers, built as a Computer Science undergraduate thesis at **IFMA (Instituto Federal do Maranhão)**.

The project serves as a practical case study on applying **Hexagonal Architecture** and **GoF design patterns** to build modular, testable, and technology-resilient notification systems.

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

| Service              | URL                                           | Purpose                              |
| -------------------- | --------------------------------------------- | ------------------------------------ |
| Monolith UI          | `http://localhost:8080`                       | React frontend + Spring Boot gateway |
| Swagger UI           | `http://localhost:8080/swagger-ui/index.html` | API documentation                    |
| device-service       | `http://localhost:8081`                       | Registers FCM device tokens          |
| notification-service | `http://localhost:8082`                       | Sends push notifications via FCM     |
| RabbitMQ management  | `http://localhost:15672` (guest/guest)        | Message broker UI                    |

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
