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

**Prerequisites:** Java 21, Node.js 18+, Docker

```bash
# Start dependencies (PostgreSQL)
docker compose -f src/main/docker/postgresql.yml up -d

# Run backend
./mvnw spring-boot:run

# Run frontend (separate terminal)
npm install
npm start
```

App available at `http://localhost:8080`
Swagger UI at `http://localhost:8080/swagger-ui/index.html`

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
