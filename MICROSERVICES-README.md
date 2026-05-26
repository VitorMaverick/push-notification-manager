# Push Notification Manager — Microservices

Microservice extraction of the push notification monolith following DDD + Hexagonal Architecture.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     API Gateway (:9000)                  │
│              JWT validation + request routing            │
└───────────────┬──────────────────┬──────────────────────┘
                │                  │
    ┌───────────▼──────┐  ┌────────▼─────────┐
    │  device-service  │  │notification-svc  │
    │     (:8081)      │  │     (:8082)      │
    │                  │  │                  │
    │  device_db       │  │  notification_db │
    │  (postgres:5432) │  │  (postgres:5433) │
    └──────────────────┘  └──────────────────┘
                                   │
                          ┌────────▼─────────┐
                          │  Firebase FCM    │
                          │  (Google Cloud)  │
                          └──────────────────┘
```

## Services

| Service              | Port | Database        | Responsibility                       |
| -------------------- | ---- | --------------- | ------------------------------------ |
| device-service       | 8081 | device_db       | FCM token registration and lifecycle |
| notification-service | 8082 | notification_db | Push dispatch + delivery tracking    |

## Prerequisites

- Java 21
- Maven 3.9+
- Docker 24+ and Docker Compose v2
- Firebase project with a service account key JSON

## Running with Docker Compose

```bash
# 1. Set required environment variables
export JWT_BASE64_SECRET=<your-base64-encoded-256bit-secret>
export FIREBASE_KEY_PATH=./secrets/firebase-service-account.json

# 2. Place your Firebase service account JSON
mkdir -p secrets
cp /path/to/firebase-service-account.json secrets/

# 3. Start all services
cd push-notification-manager
docker compose up -d

# 4. Check health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

## Running Locally (development)

```bash
# Start databases only
docker compose up -d device-db notification-db

# device-service
cd services/device-service
DEVICE_DB_URL=jdbc:postgresql://localhost:5432/device_db \
DEVICE_DB_USER=device \
DEVICE_DB_PASS=device \
JWT_BASE64_SECRET=<secret> \
mvn spring-boot:run

# notification-service (separate terminal)
cd services/notification-service
NOTIFICATION_DB_URL=jdbc:postgresql://localhost:5433/notification_db \
NOTIFICATION_DB_USER=notification \
NOTIFICATION_DB_PASS=notification \
JWT_BASE64_SECRET=<secret> \
FIREBASE_SERVICE_ACCOUNT_KEY=file:///path/to/firebase-key.json \
mvn spring-boot:run
```

## API Reference

### device-service (`:8081`)

| Method | Path                      | Description             |
| ------ | ------------------------- | ----------------------- |
| POST   | `/api/v1/devices`         | Register FCM token      |
| GET    | `/api/v1/devices`         | List registered devices |
| GET    | `/api/v1/devices/{token}` | Get device by FCM token |

**Register device:**

```json
POST /api/v1/devices
{
  "fcmToken": "eXaMpLe-FcM-ToKeN",
  "platform": "ANDROID",
  "userAgent": "MyApp/1.0 Android/14"
}
```

### notification-service (`:8082`)

| Method | Path                         | Description               |
| ------ | ---------------------------- | ------------------------- |
| POST   | `/api/v1/notifications`      | Send push notification    |
| GET    | `/api/v1/notifications`      | List notification history |
| GET    | `/api/v1/notifications/{id}` | Get notification detail   |
| POST   | `/api/v1/notifications/ack`  | Acknowledge delivery      |

**Send notification:**

```json
POST /api/v1/notifications
{
  "recipientToken": "eXaMpLe-FcM-ToKeN",
  "title": "Hello",
  "body": "World",
  "data": { "key": "value" }
}
```

**Acknowledge delivery (FCM webhook):**

```json
POST /api/v1/notifications/ack
{ "fcmMessageId": "projects/xxx/messages/yyy" }
```

## Security

All endpoints (except `/actuator/health` and `/actuator/info`) require a valid JWT Bearer token.

Both services share the same `JWT_BASE64_SECRET` — the same secret used by the API gateway to sign tokens. No inter-service calls occur; each service validates the JWT independently.

## Project Structure

```
push-notification-manager/
├── docker-compose.yml
├── MICROSERVICES-SPEC.md       ← Architecture decisions
├── MICROSERVICES-README.md     ← This file
└── services/
    ├── device-service/
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/main/java/br/edu/acad/ifma/device/
    │       ├── domain/          ← Entities + value objects
    │       ├── port/            ← Input/output ports
    │       ├── usecase/         ← Application business rules
    │       ├── adapter/
    │       │   ├── persistence/ ← JPA adapters
    │       │   └── rest/        ← HTTP controllers + DTOs
    │       └── config/          ← Security + beans
    └── notification-service/
        ├── Dockerfile
        ├── pom.xml
        └── src/main/java/br/edu/acad/ifma/notification/
            ├── domain/          ← Entities + value objects
            ├── port/            ← Input/output ports
            ├── usecase/         ← Application business rules
            ├── adapter/
            │   ├── persistence/ ← JPA adapters
            │   ├── rest/        ← HTTP controllers + DTOs
            │   └── fcm/         ← Firebase adapter
            └── config/          ← Security + Firebase beans
```
