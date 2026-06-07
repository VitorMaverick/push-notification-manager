# Spec: Push Notification Manager — Hexagonal Architecture Refactoring

**Date:** 2026-04-13  
**Branch:** feature/push-notification-hexagonal-refactor  
**Quality Score:** 3.8/5.0 (Completeness: 4 | Clarity: 3.5 | Consistency: 4 | Feasibility: 4 | Testability: 3.5)  
**Status:** Approved

---

## Problem Statement

The push-notification-manager currently lacks structured business logic encapsulation and systematic notification lifecycle tracking. The application exhibits:

- **Weak architectural boundaries**: business logic intertwined with REST endpoints (monolithic structure)
- **No notification history tracking**: sent notifications not persisted with delivery status
- **Missing device management**: device registration and token lifecycle not formally managed
- **UX friction**: modal alerts (Portuguese UI) instead of modern toast notifications and inline validation
- **Limited observability**: no clear separation between domain logic, ports/adapters, and external dependencies (FCM)

This refactoring establishes Hexagonal Architecture (Ports & Adapters + DDD) matching the `controle-tim-incentivo` reference project, introduces `PushNotification` and `Device` domain entities with builder patterns, implements notification history with status tracking, and modernizes the React frontend with English localization and professional UX patterns.

---

## Constraints

### Backend
- **Backward Compatibility**: Existing endpoints (`POST /api/fcm/send`, `POST /api/internal/fcm/ack`) must remain functional during and after refactoring
- **Architecture Adherence**: Must match Hexagonal Architecture pattern from `controle-tim-incentivo` reference:
  - Domain entities use builder pattern (no setters in domain layer)
  - Ports define contracts; adapters provide implementations
  - Use cases orchestrate business logic via ports
  - JPA entities isolated in adapter layer (`adapters/model/`)
  - Value objects for type-safe primitives
- **FcmService.java**: Stays intact — `FcmAdapter` wraps it without modification
- **Manual mapping**: Factories and presenters (no MapStruct), consistent with reference project
- **Java 17+**, Spring Boot 3.5.8, Lombok, Spring Data JPA (JHipster-based)

### Frontend
- **Tech stack**: React 18 + Reactstrap + react-hook-form + react-toastify (existing)
- **Localization**: ALL UI text in English — no Portuguese in new/updated pages
- **No modal alerts**: `alert()` replaced with inline validation + toast notifications
- **Accessibility**: Form labels, ARIA attributes, responsive design on new pages

---

## Architecture Decisions

### Package Structure

```
src/main/java/br/edu/acad/ifma/
├── app/
│   ├── domain/
│   │   ├── notification/
│   │   │   ├── PushNotification.java        (aggregate root, builder, no JPA)
│   │   │   └── NotificationStatus.java      (enum: PENDING, SENT, DELIVERED, FAILED)
│   │   ├── device/
│   │   │   ├── Device.java                  (aggregate root, builder, no JPA)
│   │   │   ├── DeviceType.java              (enum: ANDROID, IOS, WEB)
│   │   │   └── DeviceStatus.java            (enum: ACTIVE, INACTIVE, REVOKED)
│   │   └── shared/
│   │       ├── FcmToken.java                (value object, validates non-blank, min 20 chars)
│   │       ├── NotificationTitle.java       (value object, max 255 chars)
│   │       ├── NotificationBody.java        (value object, max 2000 chars)
│   │       └── exception/
│   │           ├── DomainException.java     (base runtime exception)
│   │           ├── NotificationNotFoundException.java
│   │           ├── DeviceNotFoundException.java
│   │           ├── InvalidFcmTokenException.java
│   │           └── DuplicateDeviceTokenException.java
│   ├── port/
│   │   ├── NotificationRepositoryPort.java  (interface, pure Java)
│   │   ├── DeviceRepositoryPort.java        (interface, pure Java)
│   │   └── PushSenderPort.java              (interface, pure Java)
│   └── usecase/
│       ├── notification/
│       │   ├── SendPushNotificationUseCase.java      (@Service)
│       │   ├── SendPushNotificationCommand.java
│       │   ├── GetNotificationHistoryUseCase.java    (@Service)
│       │   ├── NotificationHistoryQuery.java
│       │   ├── NotificationFilter.java
│       │   └── GetNotificationByIdUseCase.java       (@Service)
│       └── device/
│           ├── RegisterDeviceUseCase.java             (@Service)
│           ├── RegisterDeviceCommand.java
│           ├── ListDevicesUseCase.java                (@Service)
│           └── GetDeviceByTokenUseCase.java           (@Service)
└── adapters/
    ├── api/
    │   └── rest/
    │       ├── NotificationController.java           (/api/v1/notifications)
    │       ├── DeviceController.java                 (/api/v1/devices)
    │       ├── inbound/
    │       │   ├── SendNotificationRequest.java
    │       │   └── RegisterDeviceRequest.java
    │       ├── outbound/
    │       │   ├── NotificationResponse.java
    │       │   ├── NotificationDetailResponse.java
    │       │   ├── NotificationSummaryResponse.java
    │       │   ├── DeviceResponse.java
    │       │   └── ResponseError.java
    │       ├── presenter/
    │       │   ├── NotificationPresenter.java
    │       │   └── DevicePresenter.java
    │       └── exception/
    │           └── RestExceptionHandler.java         (@RestControllerAdvice)
    ├── repository/
    │   ├── NotificationRepositoryAdapter.java        (implements NotificationRepositoryPort)
    │   ├── NotificationJpaRepository.java            (Spring Data JPA)
    │   ├── DeviceRepositoryAdapter.java              (implements DeviceRepositoryPort)
    │   └── DeviceJpaRepository.java                 (Spring Data JPA)
    ├── model/
    │   ├── NotificationJpaEntity.java               (@Entity — renamed from Notification.java)
    │   └── DeviceJpaEntity.java                     (@Entity — new)
    ├── factory/
    │   ├── NotificationFactory.java                 (domain ↔ JPA bidirectional)
    │   └── DeviceFactory.java                       (domain ↔ JPA bidirectional)
    └── fcm/
        └── FcmAdapter.java                          (implements PushSenderPort, wraps FcmService)
```

### Domain Entity Contracts

**PushNotification:**
```java
// Fields: id, title (NotificationTitle), body (NotificationBody),
//         recipientToken (FcmToken), status (NotificationStatus),
//         fcmMessageId, sentAt, deliveredAt, createdAt
// Builder: PushNotification.builder().withTitle(...).withBody(...).build()
// Behavior methods:
void markSent(String fcmMessageId);
void markDelivered();
void markFailed(String reason);
```

**Device:**
```java
// Fields: id, fcmToken (FcmToken), deviceName, type (DeviceType),
//         status (DeviceStatus), registeredAt, lastUsedAt
// Builder: Device.builder().withFcmToken(...).withType(...).build()
```

**Value Objects (all immutable, validate in constructor):**
- `FcmToken`: non-blank, min 20 chars → throws `InvalidFcmTokenException`
- `NotificationTitle`: non-blank, max 255 chars → throws `DomainException`
- `NotificationBody`: max 2000 chars → throws `DomainException`

### Port Interfaces

```java
// NotificationRepositoryPort
PushNotification save(PushNotification notification);
Optional<PushNotification> findById(Long id);
Page<PushNotification> findAll(Pageable pageable, NotificationFilter filter);

// DeviceRepositoryPort
Device save(Device device);
Optional<Device> findByFcmToken(FcmToken token);
Page<Device> findAll(Pageable pageable);
boolean existsByFcmToken(FcmToken token);

// PushSenderPort
String sendPushNotification(FcmToken token, NotificationTitle title, NotificationBody body);
// Returns FCM messageId on success; throws PushSendingException on failure
```

### Use Case Contracts

**SendPushNotificationUseCase.execute(command):**
1. Validate: create `FcmToken`, `NotificationTitle`, `NotificationBody` VOs (throws on invalid)
2. Build `PushNotification` with `PENDING` status
3. Save via `NotificationRepositoryPort`
4. Call `PushSenderPort.sendPushNotification()` → fcmMessageId
5. On success: `notification.markSent(fcmMessageId)`, save
6. On failure: `notification.markFailed(reason)`, save, re-throw

**GetNotificationHistoryUseCase.execute(query):**
- Accepts: status (optional), deviceToken (optional), fromDate (optional), toDate (optional), Pageable
- Returns: `Page<PushNotification>`

**RegisterDeviceUseCase.execute(command):**
1. Create `FcmToken` VO (throws `InvalidFcmTokenException` on invalid)
2. Check `DeviceRepositoryPort.existsByFcmToken()` → throws `DuplicateDeviceTokenException` if exists
3. Build `Device` with `ACTIVE` status
4. Save via `DeviceRepositoryPort`

### REST API Contracts

#### Notification Endpoints

**POST /api/v1/notifications**
```json
// Request
{ "deviceToken": "string (required)", "title": "string (required)", "body": "string (required)" }

// Response 202 Accepted
{ "id": 1001, "status": "SENT", "fcmMessageId": "string", "sentAt": "ISO-8601", "createdAt": "ISO-8601" }

// Errors: 400 (validation), 500 (FCM failure)
```

**GET /api/v1/notifications**
```
Query params: status (PENDING|SENT|DELIVERED|FAILED), deviceToken, fromDate (ISO-8601), toDate (ISO-8601), page (default 0), size (default 20, max 500)
Response 200: Page<NotificationSummaryResponse>
Errors: 400 (invalid params, page size > 500, fromDate > toDate)
```

**GET /api/v1/notifications/{id}**
```
Response 200: NotificationDetailResponse (all fields including title, body, recipientToken)
Errors: 404 (not found)
```

#### Device Endpoints

**POST /api/v1/devices**
```json
// Request
{ "fcmToken": "string (required)", "platform": "string (optional)", "userAgent": "string (optional)" }

// Response 201 Created
{ "id": 1, "fcmToken": "...", "platform": "...", "registeredAt": "ISO-8601", "lastUsedAt": null }

// Errors: 400 (invalid token), 409 (duplicate token)
```

**GET /api/v1/devices**
```
Query params: page (default 0), size (default 20)
Response 200: Page<DeviceResponse>
```

**GET /api/v1/devices/{token}**
```
Response 200: DeviceResponse
Errors: 404 (not found)
```

### RestExceptionHandler Mappings

| Exception | HTTP Status |
|---|---|
| `InvalidFcmTokenException` | 400 Bad Request |
| `DomainException` (validation) | 400 Bad Request |
| `NotificationNotFoundException` | 404 Not Found |
| `DeviceNotFoundException` | 404 Not Found |
| `DuplicateDeviceTokenException` | 409 Conflict |
| `MethodArgumentNotValidException` | 400 Bad Request |
| `PushSendingException` (FCM failure) | 500 Internal Server Error |
| `Exception` (fallback) | 500 Internal Server Error |

### Database Schema Changes

**ALTER TABLE notification** (existing):
```sql
ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'PENDING';
ADD COLUMN sent_at TIMESTAMP;
-- Existing: id, body, channel, recipient_token, created_at, fcm_message_id, delivered_at
-- Note: 'subject' column maps to title concept in domain
```

**CREATE TABLE device** (new):
```sql
CREATE TABLE device (
  id BIGSERIAL PRIMARY KEY,
  fcm_token VARCHAR(500) NOT NULL,
  device_name VARCHAR(255),
  type VARCHAR(50),
  status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
  registered_at TIMESTAMP NOT NULL,
  last_used_at TIMESTAMP,
  CONSTRAINT uq_device_fcm_token UNIQUE (fcm_token)
);
```

### Tradeoff Analysis

| Decision | Alternatives | Chosen | Rationale |
|---|---|---|---|
| Manual factory mapping | MapStruct | Manual factories | Consistent with reference project; simpler to debug |
| Spring `Pageable` in ports | Custom pagination VO | Spring Pageable (pragmatic) | Spring Data is stable; avoids boilerplate |
| FcmAdapter wraps FcmService | Rewrite FcmService | Wrap (non-breaking) | FcmService tested and working; zero risk |
| Keep legacy endpoints | Remove them | Keep (backward compat) | Clients may depend on them |
| Value objects as classes | Java records | Class with constructor validation | Better encapsulation; builder pattern compatibility |

### Integration Strategy (JHipster-specific)

1. **SecurityConfiguration.java**: Add `/api/v1/**` to permitted paths (same auth as `/api/**`)
2. **Liquibase**: 2 new migration files (notification extension + device table)
3. **Rename `Notification.java` → `NotificationJpaEntity.java`** in `adapters/model/`; new `PushNotification.java` domain entity in `app/domain/notification/`
4. **FcmResource.java**: Refactor internals to delegate to `SendPushNotificationUseCase` while preserving `/api/fcm/send` contract
5. **FcmAckResource.java**: Keep as-is or delegate to `NotificationRepositoryPort` to update `deliveredAt`
6. **Observer pattern removal** (after migration): Delete `FcmQueueManager`, `NotificationObserver`, `NotificationSubject`, `FcmNotificationStrategy`, `EmailNotificationStrategy`, `SmsNotificationStrategy`, `LoggingObserver`, `NotificationService`

### Risks

| Risk | Probability | Impact | Mitigation |
|---|---|---|---|
| Observer pattern removal breaks existing tests | Medium | High | Migrate tests alongside; keep legacy endpoints working |
| `Notification.java` split breaks imports | High | Medium | Create new domain entity first; rename JPA entity after; update all imports atomically |
| Liquibase migration conflicts with existing data | Low | High | Forward-compatible migrations only; test with existing data via `@SpringBootTest` |
| Spring Security blocks `/api/v1/` | Medium | Medium | Explicitly add permit rule in SecurityConfiguration before testing controllers |

---

## Acceptance Criteria

### Domain Layer

- **AC-1**: Given the new hexagonal architecture, when a developer creates a `PushNotification`, then: entity exists in `app/domain/notification/` (no JPA/Spring annotations), builder pattern with `withTitle()`/`withBody()`/`withToken()` methods, behavior methods `markSent(fcmMessageId)`, `markDelivered()`, `markFailed(reason)` update status correctly, `build()` validates required fields.

- **AC-2**: Given the device management feature, when a developer creates a `Device`, then: entity exists in `app/domain/device/` (no JPA/Spring), builder pattern, `FcmToken` value object embedded, `DeviceType` enum (ANDROID, IOS, WEB), `DeviceStatus` enum (ACTIVE, INACTIVE, REVOKED).

- **AC-3**: Given value objects in `app/domain/shared/`, when instantiated with invalid data, then: `FcmToken("")` throws `InvalidFcmTokenException`; `NotificationTitle("x".repeat(256))` throws `DomainException`; `NotificationBody("x".repeat(2001))` throws `DomainException`; all VOs are immutable.

- **AC-4**: Given port interfaces in `app/port/`, then: `NotificationRepositoryPort`, `DeviceRepositoryPort`, `PushSenderPort` are pure Java interfaces with zero Spring/JPA annotations.

### Use Cases

- **AC-5**: Given `SendPushNotificationUseCase`, when `execute(command)` is called with valid data, then: notification saved with PENDING, FCM sent via `PushSenderPort`, notification updated to SENT with `fcmMessageId`, domain entity returned. When FCM fails: notification marked FAILED, exception propagated.

- **AC-6**: Given `GetNotificationHistoryUseCase`, when `execute(query)` with filters, then: returns `Page<PushNotification>` filtered by status (AND) deviceToken (AND) date range; throws `DomainException` if page size > 500 or fromDate > toDate.

- **AC-7**: Given device use cases, when `RegisterDeviceUseCase.execute()` called with duplicate token, then throws `DuplicateDeviceTokenException`. When called with valid unique token, saves and returns `Device`.

### Adapters — REST

- **AC-8**: Given `NotificationController` at `/api/v1/notifications`, then: POST delegates to `SendPushNotificationUseCase`, returns 202; GET delegates to `GetNotificationHistoryUseCase`, returns paginated 200; GET/{id} delegates to `GetNotificationByIdUseCase`, returns 200 or 404.

- **AC-9**: Given `DeviceController` at `/api/v1/devices`, then: POST delegates to `RegisterDeviceUseCase`, returns 201; GET returns paginated 200; GET/{token} returns 200 or 404.

- **AC-10**: Given `RestExceptionHandler`, then each domain exception maps to the correct HTTP status (see Mappings table above). `MethodArgumentNotValidException` formats field errors as `{"code": 400, "message": "Field token: must not be blank"}`.

### Adapters — Persistence & FCM

- **AC-11**: Given `FcmAdapter` implementing `PushSenderPort`, when `sendPushNotification()` called, then delegates to existing `FcmService.sendToToken()` without modifying it; FCM exceptions converted to `PushSendingException`.

- **AC-12**: Given repository adapters and factories, when `NotificationRepositoryAdapter.save(domain)` called, then `NotificationFactory.toEntity(domain)` converts to `NotificationJpaEntity`, JPA saves, `NotificationFactory.toDomain(entity)` converts back. Same pattern for `DeviceRepositoryAdapter`.

### Backward Compatibility

- **AC-13**: Given existing clients use legacy endpoints, then `POST /api/fcm/send` continues returning 202; `POST /api/internal/fcm/ack` continues returning 200; request/response formats unchanged.

### Frontend

- **AC-14**: Given the updated `FcmSend.tsx`, then: all labels in English ("Token", "Title", "Body", "Data (JSON)", "Send Notification", "Obtain FCM Token from Browser"); no `alert()` calls; `react-hook-form` inline error messages for required fields and invalid JSON; success toast on 202; error toast on failure; loading state disables button.

- **AC-15**: Given the new `NotificationHistory` page at `/notifications/history`, then: table with columns ID, Title, Status, Token, Created At, Sent At, Delivered At; status filter (All/PENDING/SENT/DELIVERED/FAILED dropdown); device token text search; from/to date range pickers; "Refresh" button; status badges (PENDING=gray, SENT=blue, DELIVERED=green, FAILED=red); pagination (10/20/50 page size, prev/next); all text in English.

- **AC-16**: Given the new `DeviceManagement` page at `/devices`, then: registration form with FCM Token (required), Platform (optional dropdown: Android/iOS/Web), User Agent (optional text); "Register Device" button with loading state; success toast on 201; error toast on 400/409; device list table with columns ID, Token (masked with copy button), Platform, User Agent, Registered At, Last Used At; copy button shows "Token copied to clipboard" toast; pagination; all text in English.

- **AC-17**: Given updated `menu.tsx` and `routes.tsx`, then: navigation includes "Notification History" and "Device Management" links pointing to correct routes; no Portuguese text in new menu items.

---

## Edge Cases

| ID | Scenario | Expected Behavior |
|---|---|---|
| EC-1 | FCM token null/empty/< 20 chars | `FcmToken` constructor throws `InvalidFcmTokenException` → REST 400 |
| EC-2 | Duplicate device FCM token | `DuplicateDeviceTokenException` → REST 409 Conflict |
| EC-3 | FCM send failure (network/credentials) | Notification persisted as FAILED; REST returns 202 (async pattern) |
| EC-4 | NotificationBody > 2000 chars | `NotificationBody` VO throws `DomainException` → REST 400 |
| EC-5 | Concurrent registration same token | DB `UNIQUE` constraint → `DataIntegrityViolationException` → REST 409 |
| EC-6 | fromDate > toDate in history query | Use case validation → `DomainException` → REST 400 |
| EC-7 | Spring Security blocks `/api/v1/` | Add `requestMatchers("/api/v1/**").authenticated()` to SecurityConfiguration |
| EC-8 | page size > 500 | Use case or controller validation → REST 400 |
| EC-9 | Null status in DB (data corruption) | Factory throws NPE → `RestExceptionHandler` catches → REST 500 with `ResponseError` |
| EC-10 | Firebase offline during send | `FcmAdapter` throws `PushSendingException` → notification FAILED → REST 202 |

---

## Codebase Impact

### Files to DELETE (after migration complete)
- `service/notification/FcmQueueManager.java` (unused)
- `service/notification/NotificationObserver.java`
- `service/notification/NotificationSubject.java`
- `service/notification/NotificationService.java`
- `service/notification/FcmNotificationStrategy.java`
- `service/notification/EmailNotificationStrategy.java`
- `service/notification/SmsNotificationStrategy.java`
- `service/notification/LoggingObserver.java`

### Files to MODIFY
- `domain/Notification.java` → rename/move to `adapters/model/NotificationJpaEntity.java`
- `web/rest/FcmResource.java` → delegate internals to `SendPushNotificationUseCase`
- `web/rest/FcmAckResource.java` → delegate to `NotificationRepositoryPort.findById()` + `markDelivered()`
- `config/SecurityConfiguration.java` → add `/api/v1/**` permit rule
- `webapp/app/entities/notification/fcm/FcmSend.tsx` → English + no alert() + toasts
- `webapp/app/entities/notification/fcm/notification.service.ts` → update to `/api/v1/notifications`
- `webapp/app/entities/menu.tsx` → add new menu items
- `webapp/app/entities/routes.tsx` → add new routes

### Files to CREATE (all new)
All classes listed in the Package Structure section above, plus:
- `resources/config/liquibase/changelog/00000000000005_add_notification_status.xml`
- `resources/config/liquibase/changelog/00000000000006_create_device_table.xml`
- `webapp/app/entities/notification/history/NotificationHistory.tsx`
- `webapp/app/entities/notification/history/notificationHistory.service.ts`
- `webapp/app/entities/device/DeviceManagement.tsx`
- `webapp/app/entities/device/device.service.ts`

---

## Implementation Steps

| # | Step | Files | Depends on |
|---|---|---|---|
| 1 | Create domain enums + value objects | `NotificationStatus.java`, `DeviceType.java`, `DeviceStatus.java`, `FcmToken.java`, `NotificationTitle.java`, `NotificationBody.java` | — |
| 2 | Create domain exceptions | `DomainException.java` + 4 specific exceptions | Step 1 |
| 3 | Create `PushNotification` domain entity with builder | `PushNotification.java`, `PushNotificationBuilder` inner class | Steps 1, 2 |
| 4 | Create `Device` domain entity with builder | `Device.java`, `DeviceBuilder` inner class | Steps 1, 2 |
| 5 | Create repository port interfaces | `NotificationRepositoryPort.java`, `DeviceRepositoryPort.java` | Steps 3, 4 |
| 6 | Create `PushSenderPort` interface | `PushSenderPort.java` | Step 3 |
| 7 | Create `SendPushNotificationUseCase` + command | `SendPushNotificationUseCase.java`, `SendPushNotificationCommand.java` | Steps 3, 5, 6 |
| 8 | Create history + query use cases | `GetNotificationHistoryUseCase.java`, `GetNotificationByIdUseCase.java`, `NotificationHistoryQuery.java`, `NotificationFilter.java` | Steps 3, 5 |
| 9 | Create device use cases | `RegisterDeviceUseCase.java`, `RegisterDeviceCommand.java`, `ListDevicesUseCase.java`, `GetDeviceByTokenUseCase.java` | Steps 2, 4, 5 |
| 10 | Liquibase migrations | `00000000000005_add_notification_status.xml`, `00000000000006_create_device_table.xml` | — (parallel) |
| 11 | JPA entities + Spring Data repos + adapters + factories | `NotificationJpaEntity.java`, `DeviceJpaEntity.java`, `NotificationJpaRepository.java`, `DeviceJpaRepository.java`, `NotificationRepositoryAdapter.java`, `DeviceRepositoryAdapter.java`, `NotificationFactory.java`, `DeviceFactory.java` | Steps 3, 4, 5, 10 |
| 12 | `FcmAdapter` (wraps existing `FcmService`) | `FcmAdapter.java` | Steps 3, 6 |
| 13 | Notification REST adapter | `NotificationController.java`, `SendNotificationRequest.java`, `NotificationResponse.java`, `NotificationDetailResponse.java`, `NotificationSummaryResponse.java`, `NotificationPresenter.java` | Steps 7, 8 |
| 14 | Device REST adapter | `DeviceController.java`, `RegisterDeviceRequest.java`, `DeviceResponse.java`, `DevicePresenter.java` | Step 9 |
| 15 | Exception handler + error DTO | `RestExceptionHandler.java`, `ResponseError.java` | Steps 2, 13, 14 |
| 16 | Frontend updates | Modify `FcmSend.tsx`; create `NotificationHistory.tsx`, `DeviceManagement.tsx`, service files; update `routes.tsx`, `menu.tsx` | Steps 13, 14 |

**Parallelizable:** Step 10 (Liquibase) can run in parallel with Steps 1–9.  
**Frontend (Step 16)** can begin after Step 13 stabilizes the notification API.

---

## Test Strategy

### Test Types

| Type | Scope | Tools | Coverage Target |
|---|---|---|---|
| Unit | Domain entities, value objects, use cases (mocked ports) | JUnit 5, Mockito | >80% branch on domain + use cases |
| Integration | Repository adapters (real DB), FCM adapter | Spring Boot Test, TestContainers (PostgreSQL) | >70% integration paths |
| REST Controller | HTTP endpoints, request/response mapping | MockMvc + JUnit 5 | 100% endpoint coverage (6 v1 + 2 legacy) |
| Frontend Unit | React components (rendering, interaction) | Jest + React Testing Library | >75% component coverage |
| E2E | Full user workflows | Cypress | Happy path + critical edge cases |
| Architecture | Layer dependency enforcement | ArchUnit | adapters must not import domain directly |

### Key Test Cases (representative)

**Domain:**
- `FcmTokenTest`: valid token passes; blank/null/short throw `InvalidFcmTokenException`
- `PushNotificationTest`: builder creates entity; `markSent()` updates status + fcmMessageId; `markFailed()` updates status to FAILED
- `DeviceTest`: builder creates entity; duplicate detection in use case layer

**Use Cases:**
- `SendPushNotificationUseCaseTest`: valid command → saves PENDING, calls sender, updates SENT; FCM exception → saves FAILED, re-throws
- `GetNotificationHistoryUseCaseTest`: filter by status, token, date range; page size > 500 → exception
- `RegisterDeviceUseCaseTest`: duplicate token → `DuplicateDeviceTokenException`; valid → saves and returns

**Integration:**
- `NotificationRepositoryAdapterIT`: save + findById + findAll with filters (TestContainers)
- `DeviceRepositoryAdapterIT`: save + findByFcmToken + unique constraint enforcement

**REST:**
- `NotificationControllerTest`: POST → 202; POST invalid body → 400; GET with filters → 200 paginated; GET/{id} not found → 404
- `DeviceControllerTest`: POST → 201; POST duplicate → 409; GET → 200; GET/{token} not found → 404
- `FcmLegacyControllerTest`: POST /api/fcm/send → 202 (backward compat)

**Frontend:**
- `FcmSend.test.tsx`: no `alert()` calls; inline validation errors shown; success toast on 202
- `NotificationHistory.test.tsx`: table renders, status badges colored, filters update table, pagination works
- `DeviceManagement.test.tsx`: form submits, copy button copies token, pagination works

### Edge Case Coverage

| EC | Test Class | Test Method |
|---|---|---|
| EC-1 (invalid token) | `FcmTokenTest`, `NotificationControllerTest` | `shouldRejectBlankToken`, `postWithInvalidToken_returns400` |
| EC-2 (duplicate device) | `RegisterDeviceUseCaseTest`, `DeviceControllerTest` | `shouldThrowDuplicateOnExistingToken`, `postDuplicate_returns409` |
| EC-3 (FCM failure → FAILED) | `SendPushNotificationUseCaseTest` | `whenFcmFails_notificationMarkedFailed` |
| EC-4 (large body) | `NotificationBodyTest` | `shouldRejectBodyExceeding2000Chars` |
| EC-5 (concurrent registration) | `DeviceRepositoryAdapterIT` | `concurrentSameToken_onlyOneSucceeds` |
| EC-6 (invalid date range) | `GetNotificationHistoryUseCaseTest` | `whenFromDateAfterToDate_throwsDomainException` |
| EC-7 (security) | `NotificationControllerTest` | `unauthenticated_returns401` |
| EC-8 (page size > 500) | `GetNotificationHistoryUseCaseTest` | `whenPageSizeExceeds500_throwsDomainException` |
| EC-9 (null status in DB) | `NotificationRepositoryAdapterIT` | `corruptedStatus_returns500WithResponseError` |
| EC-10 (Firebase offline) | `FcmAdapterTest` | `whenFirebaseUnreachable_throwsPushSendingException` |

---

## Verification Rubric

| # | Requirement | Pass Criteria |
|---|---|---|
| AC-1 | PushNotification domain entity | Builder works; no JPA/Spring annotations; markSent/markDelivered/markFailed update status correctly |
| AC-2 | Device domain entity | Builder works; no JPA/Spring annotations; FcmToken VO embedded |
| AC-3 | Value objects validation | Invalid data throws on construction; VOs are immutable |
| AC-4 | Port interfaces | Zero Spring/JPA annotations; pure Java interfaces |
| AC-5 | SendPushNotificationUseCase | Happy path: PENDING → SENT; failure: PENDING → FAILED; @Transactional |
| AC-6 | GetNotificationHistoryUseCase | Filters work; page size > 500 → exception; invalid date range → exception |
| AC-7 | Device use cases | RegisterDevice: duplicate → 409; valid → 201. ListDevices: paginated |
| AC-8 | NotificationController | 6 v1 endpoints correct HTTP status + response body |
| AC-9 | DeviceController | 3 v1 endpoints correct HTTP status + response body |
| AC-10 | RestExceptionHandler | All exception types map to correct HTTP status; validation errors include field names |
| AC-11 | FcmAdapter | Wraps FcmService; no FcmService modification; FCM exceptions → PushSendingException |
| AC-12 | Repository adapters + factories | Bidirectional conversion without data loss; findAll with filters works |
| AC-13 | Backward compat | `/api/fcm/send` → 202; `/api/internal/fcm/ack` → 200; response format unchanged |
| AC-14 | FcmSend.tsx English + UX | No `alert()`; English labels; inline errors; success/error toasts |
| AC-15 | NotificationHistory page | Table with filters, badges, pagination; all text English |
| AC-16 | DeviceManagement page | Registration form + device list + copy button; all text English |
| AC-17 | Navigation | Menu + routes include new pages |
| EC-1–10 | All edge cases | Each EC tested with specific test case (see Edge Case Coverage table) |
