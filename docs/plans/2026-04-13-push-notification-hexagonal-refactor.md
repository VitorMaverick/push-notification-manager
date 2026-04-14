# Push Notification Manager — Hexagonal Refactor Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use fulcrum:execute-plan to implement this plan task-by-task.

**Goal:** Refactor push-notification-manager to Hexagonal Architecture, adding notification history and device management.

**Architecture:** Pure Java domain layer (no framework deps), port interfaces, adapter layer (JPA + REST + FCM). Use cases orchestrate via ports. Reference: `controle-tim-incentivo` builder/factory patterns.

**Tech Stack:** Java 17, Spring Boot 3.5.8, Liquibase, JPA/Hibernate, JUnit 5 + Mockito, React 18 + Reactstrap + react-hook-form + react-toastify

**Base package:** `br.edu.acad.ifma`

---

## Context

| Existing file | Action |
|---|---|
| `domain/Notification.java` | Keep until Task 11; then delete |
| `repository/NotificationMessageRepository.java` | Keep until Task 11; then delete |
| `web/rest/FcmResource.java` | Refactor in Task 12 (keep `/api/fcm/send` contract) |
| `web/rest/FcmAckResource.java` | Refactor in Task 12 (keep `/api/internal/fcm/ack` contract) |
| `service/notification/FcmService.java` | Never touch — FcmAdapter wraps it |
| `service/notification/FcmQueueManager.java` + 6 observer files | Delete in Task 13 |
| `config/SecurityConfiguration.java` | No change needed — `/api/**` already authenticated covers `/api/v1/**` |

---

## Task 1 — Domain Enums + Exceptions + Value Objects

**Files to create:**

```
app/domain/notification/NotificationStatus.java       enum: PENDING, SENT, DELIVERED, FAILED
app/domain/device/DeviceType.java                      enum: ANDROID, IOS, WEB
app/domain/device/DeviceStatus.java                    enum: ACTIVE, INACTIVE, REVOKED
app/domain/shared/exception/DomainException.java       extends RuntimeException
app/domain/shared/exception/NotificationNotFoundException.java
app/domain/shared/exception/DeviceNotFoundException.java
app/domain/shared/exception/InvalidFcmTokenException.java
app/domain/shared/exception/DuplicateDeviceTokenException.java
app/domain/shared/exception/PushSendingException.java
app/domain/shared/FcmToken.java                        validates non-blank + min 20 chars → InvalidFcmTokenException
app/domain/shared/NotificationTitle.java               validates non-blank + max 255 chars → DomainException
app/domain/shared/NotificationBody.java                validates non-blank + max 2000 chars → DomainException
```

**Value object pattern (all identical shape):**
```java
public final class FcmToken {
    private final String value;
    public FcmToken(String value) {
        if (value == null || value.isBlank()) throw new InvalidFcmTokenException("FCM token must not be blank");
        if (value.length() < 20) throw new InvalidFcmTokenException("FCM token too short");
        this.value = value;
    }
    public String value() { return value; }
}
```

**Tests:** `src/test/java/br/edu/acad/ifma/app/domain/shared/FcmTokenTest.java` — test valid, blank, short. Same for Title and Body.

**Compile + test:** `./mvnw test -Dtest="FcmTokenTest,NotificationTitleTest,NotificationBodyTest" -DfailIfNoTests=false`

**Commit:** `feat: add domain enums, exceptions, and value objects`

---

## Task 2 — Domain Entities

**Files to create:**

```
app/domain/notification/PushNotification.java
app/domain/device/Device.java
```

**PushNotification shape:**
```java
// Fields: id, title (NotificationTitle), body (NotificationBody),
//         recipientToken (FcmToken), status (NotificationStatus),
//         fcmMessageId, sentAt, deliveredAt, createdAt
// No JPA/Spring annotations

public static PushNotificationBuilder builder() { return new PushNotificationBuilder(); }

// Behavior:
public void markSent(String fcmMessageId) { this.status = SENT; this.fcmMessageId = fcmMessageId; this.sentAt = Instant.now(); }
public void markDelivered() { this.status = DELIVERED; this.deliveredAt = Instant.now(); }
public void markFailed(String reason) { this.status = FAILED; this.fcmMessageId = reason; }

// Builder: inner static class with withTitle(), withBody(), withRecipientToken(), withStatus(), withId(), etc.
// build() does NOT validate (validation is on VOs at construction time)
```

**Device shape:**
```java
// Fields: id, fcmToken (FcmToken), deviceName, type (DeviceType), status (DeviceStatus),
//         registeredAt, lastUsedAt
// Builder: inner static class, same pattern as Contrato.java in reference project
```

**Follow exactly** the builder pattern in `/home/vitor.maverick/repo/tim-controle/controle-tim-incentivo/src/main/java/com/m4u/controletimincentivo/app/domain/contrato/Contrato.java`

**Tests:** `PushNotificationTest` — builder creates entity; markSent/markDelivered/markFailed update status. `DeviceTest` — builder creates entity.

**Compile + test:** `./mvnw test -Dtest="PushNotificationTest,DeviceTest" -DfailIfNoTests=false`

**Commit:** `feat: add PushNotification and Device domain entities`

---

## Task 3 — Port Interfaces

**Files to create:**

```
app/port/NotificationRepositoryPort.java
app/port/DeviceRepositoryPort.java
app/port/PushSenderPort.java
```

```java
// NotificationRepositoryPort
public interface NotificationRepositoryPort {
    PushNotification save(PushNotification notification);
    Optional<PushNotification> findById(Long id);
    Optional<PushNotification> findByFcmMessageId(String fcmMessageId);
    Page<PushNotification> findAll(Pageable pageable, NotificationFilter filter);
}

// DeviceRepositoryPort
public interface DeviceRepositoryPort {
    Device save(Device device);
    Optional<Device> findByFcmToken(FcmToken token);
    Page<Device> findAll(Pageable pageable);
    boolean existsByFcmToken(FcmToken token);
}

// PushSenderPort
public interface PushSenderPort {
    String sendPushNotification(FcmToken token, NotificationTitle title, NotificationBody body);
}
```

Also create: `app/usecase/notification/NotificationFilter.java` (plain POJO with status, deviceToken, fromDate, toDate + getters/setters)

No tests for interfaces. Compile check: `./mvnw compile -q`

**Commit:** `feat: add port interfaces and NotificationFilter`

---

## Task 4 — Liquibase Migrations

**Files to create:**

```
src/main/resources/config/liquibase/changelog/00000000000005_add_notification_status.xml
src/main/resources/config/liquibase/changelog/00000000000006_create_device_table.xml
```

**Changelog 5:**
```xml
<changeSet id="00000000000005" author="dev">
    <addColumn tableName="notification">
        <column name="status" type="varchar(50)" defaultValue="PENDING">
            <constraints nullable="false"/>
        </column>
        <column name="sent_at" type="timestamp"/>
    </addColumn>
</changeSet>
```

**Changelog 6:**
```xml
<changeSet id="00000000000006" author="dev">
    <createTable tableName="device">
        <column name="id" type="bigint" autoIncrement="true"><constraints primaryKey="true" nullable="false"/></column>
        <column name="fcm_token" type="varchar(500)"><constraints nullable="false" unique="true" uniqueConstraintName="uq_device_fcm_token"/></column>
        <column name="device_name" type="varchar(255)"/>
        <column name="type" type="varchar(50)"/>
        <column name="status" type="varchar(50)" defaultValue="ACTIVE"><constraints nullable="false"/></column>
        <column name="registered_at" type="timestamp"><constraints nullable="false"/></column>
        <column name="last_used_at" type="timestamp"/>
    </createTable>
</changeSet>
```

**Add both to master.xml** (before the jhipster-needle comment).

**Compile check:** `./mvnw compile -q`

**Commit:** `feat: add liquibase migrations for notification status and device table`

---

## Task 5 — JPA Entities + Factories + Repository Adapters

**Files to create:**

```
adapters/model/NotificationJpaEntity.java
adapters/model/DeviceJpaEntity.java
adapters/factory/NotificationFactory.java
adapters/factory/DeviceFactory.java
adapters/repository/NotificationJpaRepository.java
adapters/repository/DeviceJpaRepository.java
adapters/repository/NotificationRepositoryAdapter.java   implements NotificationRepositoryPort
adapters/repository/DeviceRepositoryAdapter.java         implements DeviceRepositoryPort
```

**NotificationJpaEntity** — maps to `notification` table, same fields as old `Notification.java` PLUS `status` and `sentAt`. Use distinct generator name to avoid conflict with old `Notification.java` that still exists:
```java
@Entity @Table(name = "notification")
public class NotificationJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notificationJpaSeq")
    @SequenceGenerator(name = "notificationJpaSeq", sequenceName = "hibernate_sequence", allocationSize = 1)
    private Long id;
    @Column(name = "subject") private String title;   // 'subject' is the existing column name
    @Column(name = "body", length = 2000) private String body;
    @Column(name = "recipient_token") private String recipientToken;
    @Column(name = "fcm_message_id") private String fcmMessageId;
    @Enumerated(EnumType.STRING) @Column(name = "status") private NotificationStatus status;
    @Column(name = "sent_at") private Instant sentAt;
    @Column(name = "delivered_at") private Instant deliveredAt;
    @Column(name = "created_at") private Instant createdAt;
    @Enumerated(EnumType.ORDINAL) @Column(name = "channel") private NotificationChannel channel;
    // getters + setters (no Lombok — matches reference project style)
}
```

**DeviceJpaEntity** — maps to `device` table:
```java
@Entity @Table(name = "device")
public class DeviceJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "fcm_token") private String fcmToken;
    @Column(name = "device_name") private String deviceName;
    @Enumerated(EnumType.STRING) @Column(name = "type") private DeviceType type;
    @Enumerated(EnumType.STRING) @Column(name = "status") private DeviceStatus status;
    @Column(name = "registered_at") private Instant registeredAt;
    @Column(name = "last_used_at") private Instant lastUsedAt;
    // getters + setters
}
```

**NotificationJpaRepository:**
```java
public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long>, JpaSpecificationExecutor<NotificationJpaEntity> {
    Optional<NotificationJpaEntity> findByFcmMessageId(String fcmMessageId);
}
```

**DeviceJpaRepository:**
```java
public interface DeviceJpaRepository extends JpaRepository<DeviceJpaEntity, Long> {
    Optional<DeviceJpaEntity> findByFcmToken(String fcmToken);
    boolean existsByFcmToken(String fcmToken);
}
```

**NotificationFactory** (static methods, `abstract class`, private constructor):
```java
public abstract class NotificationFactory {
    private NotificationFactory() {}

    public static NotificationJpaEntity toEntity(PushNotification domain) {
        NotificationJpaEntity e = new NotificationJpaEntity();
        e.setId(domain.getId());
        e.setTitle(domain.getTitle().value());
        e.setBody(domain.getBody().value());
        e.setRecipientToken(domain.getRecipientToken().value());
        e.setStatus(domain.getStatus());
        e.setFcmMessageId(domain.getFcmMessageId());
        e.setSentAt(domain.getSentAt());
        e.setDeliveredAt(domain.getDeliveredAt());
        e.setCreatedAt(domain.getCreatedAt());
        e.setChannel(NotificationChannel.FCM_PUSH);
        return e;
    }

    public static PushNotification toDomain(NotificationJpaEntity e) {
        return PushNotification.builder()
            .withId(e.getId())
            .withTitle(new NotificationTitle(e.getTitle()))
            .withBody(new NotificationBody(e.getBody()))
            .withRecipientToken(new FcmToken(e.getRecipientToken()))
            .withStatus(e.getStatus())
            .withFcmMessageId(e.getFcmMessageId())
            .withSentAt(e.getSentAt())
            .withDeliveredAt(e.getDeliveredAt())
            .withCreatedAt(e.getCreatedAt())
            .build();
    }
}
```

**DeviceFactory** — same pattern for Device ↔ DeviceJpaEntity.

**NotificationRepositoryAdapter** — implements `NotificationRepositoryPort`:
- `save()`: `NotificationFactory.toEntity()` → `jpaRepo.save()` → `NotificationFactory.toDomain()`
- `findById()`: `jpaRepo.findById()` → map
- `findByFcmMessageId()`: `jpaRepo.findByFcmMessageId()` → map
- `findAll(pageable, filter)`: build `Specification<NotificationJpaEntity>` from filter fields using `where().and()` chaining, then `jpaRepo.findAll(spec, pageable).map(NotificationFactory::toDomain)`

**NotificationFilter → Specification building:**
```java
private Specification<NotificationJpaEntity> toSpec(NotificationFilter filter) {
    Specification<NotificationJpaEntity> spec = Specification.where(null);
    if (filter.getStatus() != null)
        spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), filter.getStatus()));
    if (filter.getDeviceToken() != null)
        spec = spec.and((root, q, cb) -> cb.equal(root.get("recipientToken"), filter.getDeviceToken()));
    if (filter.getFromDate() != null)
        spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFromDate()));
    if (filter.getToDate() != null)
        spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), filter.getToDate()));
    return spec;
}
```

**DeviceRepositoryAdapter** — same pattern.

**Compile check:** `./mvnw compile -q`

**Commit:** `feat: add JPA entities, factories, and repository adapters`

---

## Task 6 — FcmAdapter + Use Cases

**Files to create:**

```
adapters/fcm/FcmAdapter.java                                  implements PushSenderPort
app/usecase/notification/SendPushNotificationCommand.java
app/usecase/notification/SendPushNotificationUseCase.java     @Service
app/usecase/notification/NotificationHistoryQuery.java
app/usecase/notification/GetNotificationHistoryUseCase.java   @Service
app/usecase/notification/GetNotificationByIdUseCase.java      @Service
app/usecase/device/RegisterDeviceCommand.java
app/usecase/device/RegisterDeviceUseCase.java                 @Service
app/usecase/device/ListDevicesUseCase.java                    @Service
app/usecase/device/GetDeviceByTokenUseCase.java               @Service
```

**FcmAdapter:**
```java
@Service
public class FcmAdapter implements PushSenderPort {
    private final FcmService fcmService;
    public FcmAdapter(FcmService fcmService) { this.fcmService = fcmService; }

    @Override
    public String sendPushNotification(FcmToken token, NotificationTitle title, NotificationBody body) {
        try {
            NotificationMessageTO msg = NotificationMessageTO.builder()
                .titulo(title.value())
                .corpo(body.value())
                .build();
            return fcmService.sendToToken(token.value(), msg);
        } catch (Exception e) {
            throw new PushSendingException("FCM send failed: " + e.getMessage(), e);
        }
    }
}
```

**SendPushNotificationCommand** — plain POJO: `deviceToken`, `title`, `body` (all String).

**SendPushNotificationUseCase:**
```java
@Service
@Transactional
public class SendPushNotificationUseCase {
    // constructor inject: NotificationRepositoryPort, PushSenderPort

    public PushNotification execute(SendPushNotificationCommand command) {
        FcmToken token = new FcmToken(command.getDeviceToken());
        NotificationTitle title = new NotificationTitle(command.getTitle());
        NotificationBody body = new NotificationBody(command.getBody());

        PushNotification notification = PushNotification.builder()
            .withRecipientToken(token).withTitle(title).withBody(body)
            .withStatus(NotificationStatus.PENDING)
            .withCreatedAt(Instant.now())
            .build();
        notification = notificationRepository.save(notification);

        try {
            String fcmId = pushSender.sendPushNotification(token, title, body);
            notification.markSent(fcmId);
        } catch (PushSendingException e) {
            notification.markFailed(e.getMessage());
            notificationRepository.save(notification);
            throw e;
        }
        return notificationRepository.save(notification);
    }
}
```

**NotificationHistoryQuery** — plain POJO: `pageable`, `filter` (NotificationFilter).

**GetNotificationHistoryUseCase:**
```java
public Page<PushNotification> execute(NotificationHistoryQuery query) {
    NotificationFilter filter = query.getFilter();
    if (query.getPageable().getPageSize() > 500) throw new DomainException("Page size must not exceed 500");
    if (filter.getFromDate() != null && filter.getToDate() != null
        && filter.getFromDate().isAfter(filter.getToDate()))
        throw new DomainException("fromDate must not be after toDate");
    return notificationRepository.findAll(query.getPageable(), filter);
}
```

**GetNotificationByIdUseCase:**
```java
public PushNotification execute(Long id) {
    return notificationRepository.findById(id)
        .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + id));
}
```

**RegisterDeviceCommand** — plain POJO: `fcmToken`, `platform`, `userAgent`.

**RegisterDeviceUseCase:**
```java
public Device execute(RegisterDeviceCommand command) {
    FcmToken token = new FcmToken(command.getFcmToken());
    if (deviceRepository.existsByFcmToken(token)) throw new DuplicateDeviceTokenException("Token already registered");
    Device device = Device.builder()
        .withFcmToken(token)
        .withDeviceName(command.getUserAgent())
        .withType(parseType(command.getPlatform()))
        .withStatus(DeviceStatus.ACTIVE)
        .withRegisteredAt(Instant.now())
        .build();
    return deviceRepository.save(device);
}
```

**ListDevicesUseCase:** `deviceRepository.findAll(pageable)`

**GetDeviceByTokenUseCase:** `deviceRepository.findByFcmToken(new FcmToken(token)).orElseThrow(...)`

**Tests:**
- `SendPushNotificationUseCaseTest` — mock ports; happy path PENDING→SENT; FCM exception → FAILED
- `GetNotificationHistoryUseCaseTest` — page size > 500 throws; invalid date range throws
- `RegisterDeviceUseCaseTest` — duplicate token throws; valid saves

**Compile + test:** `./mvnw test -Dtest="SendPushNotificationUseCaseTest,GetNotificationHistoryUseCaseTest,RegisterDeviceUseCaseTest" -DfailIfNoTests=false`

**Commit:** `feat: add FcmAdapter and all use cases`

---

## Task 7 — REST Adapters

**Files to create:**

```
adapters/api/rest/inbound/SendNotificationRequest.java
adapters/api/rest/inbound/RegisterDeviceRequest.java
adapters/api/rest/outbound/NotificationSummaryResponse.java
adapters/api/rest/outbound/NotificationDetailResponse.java
adapters/api/rest/outbound/NotificationResponse.java
adapters/api/rest/outbound/DeviceResponse.java
adapters/api/rest/outbound/ResponseError.java
adapters/api/rest/presenter/NotificationPresenter.java
adapters/api/rest/presenter/DevicePresenter.java
adapters/api/rest/exception/RestExceptionHandler.java    @RestControllerAdvice
adapters/api/rest/NotificationController.java
adapters/api/rest/DeviceController.java
```

**SendNotificationRequest:**
```java
public class SendNotificationRequest {
    @NotBlank private String deviceToken;
    @NotBlank private String title;
    @NotBlank private String body;
    // getters + setters
}
```

**RegisterDeviceRequest:**
```java
public class RegisterDeviceRequest {
    @NotBlank private String fcmToken;
    private String platform;   // optional: ANDROID, IOS, WEB
    private String userAgent;  // optional
    // getters + setters
}
```

**NotificationSummaryResponse** — fields: id, status, fcmMessageId, sentAt, createdAt

**NotificationDetailResponse** — all fields including title, body, recipientToken

**NotificationResponse** — fields: id, status, fcmMessageId, sentAt, createdAt (202 response)

**DeviceResponse** — fields: id, fcmToken, platform, userAgent (deviceName), registeredAt, lastUsedAt

**ResponseError:**
```java
public class ResponseError {
    private int code;
    private String message;
    // getters + setters, all-args constructor
}
```

**NotificationPresenter (static methods):**
```java
public abstract class NotificationPresenter {
    private NotificationPresenter() {}
    public static NotificationResponse toResponse(PushNotification n) { ... }
    public static NotificationSummaryResponse toSummary(PushNotification n) { ... }
    public static NotificationDetailResponse toDetail(PushNotification n) { ... }
}
```

**DevicePresenter (static methods):** same pattern for DeviceResponse.

**RestExceptionHandler:**
```java
@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(InvalidFcmTokenException.class)
    public ResponseEntity<ResponseError> handle(InvalidFcmTokenException e) {
        return ResponseEntity.badRequest().body(new ResponseError(400, e.getMessage()));
    }
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ResponseError> handle(DomainException e) {
        return ResponseEntity.badRequest().body(new ResponseError(400, e.getMessage()));
    }
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ResponseError> handle(NotificationNotFoundException e) {
        return ResponseEntity.status(404).body(new ResponseError(404, e.getMessage()));
    }
    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ResponseError> handle(DeviceNotFoundException e) {
        return ResponseEntity.status(404).body(new ResponseError(404, e.getMessage()));
    }
    @ExceptionHandler(DuplicateDeviceTokenException.class)
    public ResponseEntity<ResponseError> handle(DuplicateDeviceTokenException e) {
        return ResponseEntity.status(409).body(new ResponseError(409, e.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseError> handle(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
            .map(fe -> "Field " + fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ResponseError(400, msg));
    }
    @ExceptionHandler(PushSendingException.class)
    public ResponseEntity<ResponseError> handle(PushSendingException e) {
        return ResponseEntity.status(500).body(new ResponseError(500, e.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseError> handleGeneric(Exception e) {
        return ResponseEntity.status(500).body(new ResponseError(500, "Internal server error"));
    }
}
```

**NotificationController:**
```java
@RestController
@RequestMapping("/api/v1/notifications")
@Validated
public class NotificationController {
    // constructor inject: SendPushNotificationUseCase, GetNotificationHistoryUseCase, GetNotificationByIdUseCase

    @PostMapping
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody SendNotificationRequest req) {
        SendPushNotificationCommand cmd = new SendPushNotificationCommand(req.getDeviceToken(), req.getTitle(), req.getBody());
        PushNotification result = sendUseCase.execute(cmd);
        return ResponseEntity.accepted().body(NotificationPresenter.toResponse(result));
    }

    @GetMapping
    public ResponseEntity<Page<NotificationSummaryResponse>> list(
        @RequestParam(required = false) NotificationStatus status,
        @RequestParam(required = false) String deviceToken,
        @RequestParam(required = false) Instant fromDate,
        @RequestParam(required = false) Instant toDate,
        Pageable pageable) {
        NotificationFilter filter = new NotificationFilter();
        filter.setStatus(status); filter.setDeviceToken(deviceToken);
        filter.setFromDate(fromDate); filter.setToDate(toDate);
        NotificationHistoryQuery query = new NotificationHistoryQuery(pageable, filter);
        return ResponseEntity.ok(historyUseCase.execute(query).map(NotificationPresenter::toSummary));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDetailResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(NotificationPresenter.toDetail(getByIdUseCase.execute(id)));
    }
}
```

**DeviceController:**
```java
@RestController
@RequestMapping("/api/v1/devices")
@Validated
public class DeviceController {
    @PostMapping — status 201, delegates to RegisterDeviceUseCase
    @GetMapping  — status 200 paginated, delegates to ListDevicesUseCase
    @GetMapping("/{token}") — status 200, delegates to GetDeviceByTokenUseCase
}
```

**Tests:**
- `NotificationControllerTest` (MockMvc): POST→202, POST invalid→400, GET→200, GET/{id} not found→404
- `DeviceControllerTest` (MockMvc): POST→201, POST duplicate→409, GET→200

**Compile + test:** `./mvnw test -Dtest="NotificationControllerTest,DeviceControllerTest" -DfailIfNoTests=false`

**Commit:** `feat: add REST controllers, DTOs, presenters, and exception handler`

---

## Task 8 — Migrate Legacy Endpoints + Delete Old Code

**Files to modify:**
- `web/rest/FcmResource.java`
- `web/rest/FcmAckResource.java`

**Files to delete:**
- `domain/Notification.java`
- `repository/NotificationMessageRepository.java`
- `service/notification/FcmQueueManager.java`
- `service/notification/NotificationObserver.java`
- `service/notification/NotificationSubject.java`
- `service/notification/NotificationService.java`
- `service/notification/FcmNotificationStrategy.java`
- `service/notification/EmailNotificationStrategy.java`
- `service/notification/SmsNotificationStrategy.java`
- `service/notification/LoggingObserver.java`

**FcmResource** — keep `/api/fcm/send`, delegate to `SendPushNotificationUseCase`:
```java
// Remove NotificationService dependency, inject SendPushNotificationUseCase
@PostMapping("/send")
public ResponseEntity<String> sendPush(@Valid @RequestBody FcmRequest request) {
    SendPushNotificationCommand cmd = new SendPushNotificationCommand(
        request.getToken(), request.getTitulo(), request.getCorpo());
    sendUseCase.execute(cmd);
    return ResponseEntity.accepted().body("Queued via NotificationService");
}
```

**FcmAckResource** — keep `/api/internal/fcm/ack`, delegate to `NotificationRepositoryPort`:
```java
// Remove NotificationMessageRepository dependency, inject NotificationRepositoryPort
@PostMapping("/ack")
public ResponseEntity<Void> ack(@RequestBody FcmAckRequest request) {
    if (request.getMessageId() != null) {
        notificationRepository.findByFcmMessageId(request.getMessageId()).ifPresent(n -> {
            n.markDelivered();
            notificationRepository.save(n);
        });
    }
    return ResponseEntity.ok().build();
}
```

**Delete** all files listed above. Compile will guide any missed usages.

**Compile + test all:** `./mvnw test -DfailIfNoTests=false`

**Commit:** `refactor: migrate legacy endpoints to use cases, delete observer pattern`

---

## Task 9 — Update TechnicalStructureTest

**File to modify:** `src/test/java/br/edu/acad/ifma/TechnicalStructureTest.java`

Replace existing ArchUnit rule with one that accommodates the hexagonal packages. The old `..web..` / `..service..` layers still exist (for JHipster user management). Add hexagonal layers without breaking existing:

```java
@ArchTest
static final ArchRule domainMustNotDependOnAdapters =
    noClasses().that().resideInAPackage("..app.domain..")
        .should().dependOnClassesThat().resideInAPackage("..adapters..");

@ArchTest
static final ArchRule portsMustNotDependOnAdapters =
    noClasses().that().resideInAPackage("..app.port..")
        .should().dependOnClassesThat().resideInAPackage("..adapters..");
```

Keep the existing `respectsTechnicalArchitectureLayers` rule but add `ignoreDependency` for the new `app.*` and `adapters.*` packages.

**Test:** `./mvnw test -Dtest="TechnicalStructureTest" -DfailIfNoTests=false`

**Commit:** `test: update ArchUnit rules for hexagonal architecture`

---

## Task 10 — Frontend: FcmSend.tsx

**File to modify:** `src/main/webapp/app/entities/notification/fcm/FcmSend.tsx`

**File to modify:** `src/main/webapp/app/entities/notification/fcm/notification.service.ts`

**Changes to FcmSend.tsx:**
- Change `h2` from "Enviar Push via FCM" → "Send Push Notification via FCM"
- Change all labels to English: "Token", "Title", "Body", "Data (JSON)", "Send Notification"
- Replace `alert('Token obtido...')` → `toast.success('Token obtained and filled in Token field')`
- Replace `alert('Erro obtendo token...')` → `toast.error('Error obtaining token: ' + ...)`
- Replace `alert('Campo Dados contém JSON inválido...')` → show inline error or `toast.error('Data field contains invalid JSON: ' + ...)`
- Replace `alert('Enviado para fila...')` → `toast.success('Notification sent successfully')`
- Replace `alert('Erro ao enviar...')` → `toast.error('Error sending: ' + ...)`
- Add `rules={{ required: 'Token is required' }}` to token field Controller
- Show field error inline: `{errors.token && <span className="text-danger">{errors.token.message}</span>}`
- Button text: `{loading ? 'Sending...' : 'Send Notification'}`
- "Obter token FCM do navegador" → "Obtain FCM Token from Browser"
- Add `formState: { errors }` to `useForm` destructure

**Changes to notification.service.ts:**
- Keep `/api/fcm/send` for FcmSend (legacy endpoint still works)
- Add new `getHistory(params)` and `getById(id)` methods pointing to `/api/v1/notifications`

**Compile check:** `npm run webapp:build 2>&1 | tail -5` (or just proceed, fix on error)

**Commit:** `feat: update FcmSend.tsx to English with toast notifications`

---

## Task 11 — Frontend: NotificationHistory + DeviceManagement + Navigation

**Files to create:**
```
src/main/webapp/app/entities/notification/history/NotificationHistory.tsx
src/main/webapp/app/entities/notification/history/notificationHistory.service.ts
src/main/webapp/app/entities/device/DeviceManagement.tsx
src/main/webapp/app/entities/device/device.service.ts
```

**Files to modify:**
```
src/main/webapp/app/entities/routes.tsx
src/main/webapp/app/entities/menu.tsx
```

**notificationHistory.service.ts:**
```typescript
import axios from 'axios';
const BASE = '/api/v1/notifications';
export const getHistory = (params: Record<string, any>) => axios.get(BASE, { params });
export const getById = (id: number) => axios.get(`${BASE}/${id}`);
```

**NotificationHistory.tsx** — table with columns: ID, Title, Status, Token, Created At, Sent At, Delivered At:
- Status filter dropdown (All / PENDING / SENT / DELIVERED / FAILED)
- Device token text input filter
- From/To date inputs (type="date", convert to ISO)
- "Refresh" button triggers fetch
- Status badges: PENDING=secondary, SENT=primary, DELIVERED=success, FAILED=danger
- Pagination with page size selector (10/20/50)
- All text in English
- Use `useState` for filters, `useEffect` for initial load, `axios` via service

**device.service.ts:**
```typescript
import axios from 'axios';
const BASE = '/api/v1/devices';
export const registerDevice = (data: { fcmToken: string; platform?: string; userAgent?: string }) => axios.post(BASE, data);
export const listDevices = (params: { page: number; size: number }) => axios.get(BASE, { params });
```

**DeviceManagement.tsx:**
- Registration form: FCM Token (required), Platform (optional: Android/iOS/Web dropdown), User Agent (optional text)
- "Register Device" button with loading state
- `toast.success` on 201, `toast.error` on 400 ("Invalid FCM token") / 409 ("Token already registered")
- Device list table: ID, Token (masked `${token.substring(0,8)}...`, copy button), Platform, User Agent, Registered At, Last Used At
- Copy button: `navigator.clipboard.writeText(token)` → `toast.success('Token copied to clipboard')`
- Pagination
- All text in English

**Update menu.tsx:**
```tsx
<MenuItem to="/notification/fcm/send" icon="bell">FCM</MenuItem>
<MenuItem to="/notifications/history" icon="list">Notification History</MenuItem>
<MenuItem to="/devices" icon="mobile">Device Management</MenuItem>
```

**Update entities/routes.tsx:**
```tsx
import NotificationHistory from './notification/history/NotificationHistory';
import DeviceManagement from './device/DeviceManagement';
// Add routes:
<Route path="notifications/history" element={<NotificationHistory />} />
<Route path="devices" element={<DeviceManagement />} />
```

**Compile check:** `npm run webapp:build 2>&1 | tail -5`

**Commit:** `feat: add NotificationHistory and DeviceManagement pages with navigation`

---

## Acceptance Checklist

Before marking done, verify:

- [ ] `./mvnw test` passes (all backend tests green)
- [ ] `POST /api/v1/notifications` → 202
- [ ] `GET /api/v1/notifications` → 200 paginated
- [ ] `GET /api/v1/notifications/{id}` → 200 or 404
- [ ] `POST /api/v1/devices` → 201 or 409
- [ ] `GET /api/v1/devices` → 200 paginated
- [ ] `POST /api/fcm/send` → 202 (backward compat)
- [ ] `POST /api/internal/fcm/ack` → 200 (backward compat)
- [ ] No Portuguese text in FcmSend, NotificationHistory, DeviceManagement
- [ ] No `alert()` calls in frontend
