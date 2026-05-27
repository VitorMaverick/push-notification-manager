# Hexagonal Architecture Deduplication Report

**Date:** 2026-05-26  
**Overall result:** SUCCESS

---

## Section 1: Hexagonal Analysis — notification-service

### Package structure

```
br.edu.acad.ifma.notification
├── adapter/
│   ├── fcm/          FcmService, FcmClientException
│   ├── messaging/    DeviceRegisteredConsumer, DeviceRegisteredEvent,
│   │                 NotificationRetryConsumer, RabbitNotificationEventPublisher
│   ├── persistence/  NotificationJpaEntity, NotificationJpaRepository,
│   │                 NotificationRepositoryAdapter
│   └── rest/         NotificationController, FcmAckRequest, NotificationResponse,
│                     NotificationSummaryResponse, RestExceptionHandler, SendNotificationRequest
├── config/           FirebaseConfig, RabbitMQConfig, SecurityConfiguration,
│                     SecurityJwtConfiguration
├── domain/           FcmToken, NotificationBody, NotificationStatus, NotificationTitle,
│                     PushNotification, PushSendingException,
│                     SendNotificationFailedEvent  ← MOVED HERE (fix applied)
├── port/             NotificationEventPublisherPort, NotificationRepositoryPort,
│                     PushSenderPort
└── usecase/          GetNotificationByIdUseCase, GetNotificationHistoryUseCase,
                      MarkNotificationDeliveredUseCase, NotificationFilter,
                      SendPushNotificationCommand, SendPushNotificationUseCase
```

### Violation found and fixed

**Before:** `SendNotificationFailedEvent` lived in `adapter.messaging`. Both
`NotificationEventPublisherPort` (port layer) and `SendPushNotificationUseCase` (usecase layer)
imported it directly from the adapter package — a clear inward hexagonal violation (inner rings
depending on an outer ring).

**Fix applied:**

- Created `domain/SendNotificationFailedEvent.java` with identical content.
- Updated imports in:
  - `port/NotificationEventPublisherPort.java`
  - `usecase/SendPushNotificationUseCase.java`
  - `adapter/messaging/NotificationRetryConsumer.java` (added explicit domain import)
  - `adapter/messaging/RabbitNotificationEventPublisher.java` (added explicit domain import)
- Deleted `adapter/messaging/SendNotificationFailedEvent.java`.

The domain now owns the event; adapters depend on the domain, not the reverse.

### Domain integrity

No violations found in `domain/` or `port/` packages after the fix.

---

## Section 2: Controllers — Removed and Kept

### Controllers found

| File                                                                       | Location     | Action      |
| -------------------------------------------------------------------------- | ------------ | ----------- |
| `src/main/java/…/adapters/api/rest/NotificationController.java`            | Monolith     | **REMOVED** |
| `services/notification-service/…/adapter/rest/NotificationController.java` | Microservice | **KEPT**    |

### Why removed (not kept both)

The two controllers were **not identical** — the microservice version is more complete:

| Aspect                    | Monolith (removed)                            | Microservice (kept)                |
| ------------------------- | --------------------------------------------- | ---------------------------------- |
| POST response status      | 202 Accepted                                  | 201 Created                        |
| ACK endpoint path         | `/internal/fcm/ack`                           | `/ack`                             |
| ACK implementation        | inline repository call                        | `MarkNotificationDeliveredUseCase` |
| `SendNotificationRequest` | no `data` field                               | has `data: Map<String,String>`     |
| `getById` response type   | `NotificationDetailResponse` (extra detail)   | `NotificationResponse` (unified)   |
| `list` filter params      | `deviceToken`, `fromDate`, `toDate`, `status` | `fcmToken`, `status`               |

Decision rule applied: **microservice controller is the canonical implementation**; monolith
controller was a transitional duplicate. The monolith now acts as a transparent proxy.

### Also removed (orphaned after controller deletion)

- `adapters/api/rest/inbound/FcmAckRequest.java`
- `adapters/api/rest/inbound/SendNotificationRequest.java`
- `adapters/api/rest/outbound/NotificationDetailResponse.java`
- `adapters/api/rest/outbound/NotificationResponse.java`
- `adapters/api/rest/outbound/NotificationSummaryResponse.java`
- `adapters/api/rest/presenter/NotificationPresenter.java`
- `src/test/java/…/adapters/api/rest/NotificationControllerTest.java`

These classes had no remaining callers after the controller was deleted.

### Classes intentionally kept in monolith

The following notification-related classes remain in the monolith because they are still used by
unit tests and the persistence adapter:

- `app/usecase/notification/SendPushNotificationUseCase.java`
- `app/usecase/notification/GetNotificationHistoryUseCase.java`
- `app/usecase/notification/GetNotificationByIdUseCase.java`
- `app/usecase/notification/NotificationFilter.java`
- `app/usecase/notification/NotificationHistoryQuery.java`
- `app/usecase/notification/SendPushNotificationCommand.java`
- `adapters/repository/NotificationRepositoryAdapter.java`
- `adapters/model/NotificationJpaEntity.java`

These represent the **monolith's own notification persistence capability**, which is separate from
the microservice's database. They should be evaluated for removal in a future step if the monolith's
DB schema for notifications is deprecated in favor of the microservice's DB.

---

## Section 3: Routing Adjustment

### Problem

The monolith (`:8080`) had its own `NotificationController` at `/api/v1/notifications/**`.
The frontend calls this path via the monolith. After removing the controller, `/api/v1/notifications`
calls would return 404.

### No Spring Cloud Gateway available

The monolith's `pom.xml` uses `spring-boot-starter-web` (Servlet/MVC) with no Spring Cloud
Gateway or Zuul dependency. Adding Spring Cloud Gateway would require switching to WebFlux
(reactive), which is incompatible with the existing Servlet stack.

### Solution applied

Added `NotificationProxyController.java` at
`src/main/java/…/adapters/api/rest/NotificationProxyController.java`.

- Maps `@RequestMapping("/api/v1/notifications")` with sub-paths `{ "", "/**" }`.
- Uses `RestTemplate` to forward the full request (method, headers, body, query string) to
  `http://localhost:8082` (configurable via `notification-service.base-url`).
- Strips hop-by-hop headers before forwarding.
- Returns the raw `byte[]` response from the microservice unchanged.

Added `RestTemplate` bean to `WebConfigurer.java`.

Added to `src/main/resources/config/application.yml`:

```yaml
notification-service:
  base-url: http://localhost:8082
```

### Security rules updated

`SecurityConfiguration.java` already permitted `POST /api/v1/notifications/internal/fcm/ack`
(the old monolith ACK path). Added a permit rule for `POST /api/v1/notifications/ack`
(the microservice ACK path) to ensure service workers can ACK without a JWT.

The old `/internal/fcm/ack` rule was kept for backward compatibility during any transition period.

---

## Section 4: Compile Result

| Module                               | Result              |
| ------------------------------------ | ------------------- |
| Monolith (`./mvnw compile`)          | SUCCESS — no errors |
| notification-service (`mvn compile`) | SUCCESS — no errors |

---

## Section 5: Recommendations

### High priority

1. **Remove the legacy ACK security permit** once all clients have migrated to `/api/v1/notifications/ack`.
   Line to remove from `SecurityConfiguration.java`:

   ```java
   .requestMatchers(HttpMethod.POST, "/api/v1/notifications/internal/fcm/ack").permitAll()
   ```

2. **Evaluate the monolith's notification persistence layer** (`NotificationRepositoryAdapter`,
   `NotificationJpaEntity`, and related use cases). If the system is fully migrated to the
   microservice DB, these should be removed and the Liquibase migration for the `notification`
   table should be deprecated.

### Medium priority

3. **Replace `RestTemplate` proxy with a proper HTTP client configuration** (e.g., set connect
   and read timeouts, add circuit breaker with Resilience4j) to avoid cascading failures if the
   notification-service is unavailable.

4. **Add `notification-service.base-url` override in `application-dev.yml`** if the dev environment
   runs the notification-service on a different host or port.

5. **Consider extracting the proxy into a dedicated `@Configuration` or filter** if other
   microservices need similar routing — this avoids repeating the pattern per-controller.

### Low priority

6. The microservice's `SendNotificationRequest` record has a `data: Map<String,String>` field not
   present in the monolith's old inbound DTO. Verify the frontend sends this field correctly
   when needed.

7. The frontend's `notification.service.ts` uses `/api/v1/notifications` (matching the proxy
   path). No frontend changes are required.
