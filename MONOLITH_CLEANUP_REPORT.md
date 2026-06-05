# Relatório de Limpeza do Monolito — Autenticação Pura

**Data:** 2026-05-27
**Branch:** `feature/hexagonal-dedup-notification-controller`
**Resultado:** ✅ SUCESSO

---

## 1. Sumário

| Métrica                       | Valor                                         |
| ----------------------------- | --------------------------------------------- |
| Classes removidas do monolito | 48 (produção) + 13 (testes) = **61 arquivos** |
| Funcionalidades migradas      | 0 (tudo já existia nos microserviços)         |
| Linhas deletadas              | ~2.538                                        |
| Build compile                 | ✅ SUCCESS                                    |
| Build test-compile            | ✅ SUCCESS                                    |
| Smoke tests                   | ✅ PASS                                       |

O monolito agora contém exclusivamente: autenticação JHipster (JWT/BCrypt, User/Authority, AccountResource, AuthenticateController), frontend React e infraestrutura de configuração Spring Boot.

---

## 2. Classes Removidas do Monolito

### adapters/api/rest (controllers + DTOs de Device)

- `DeviceController.java`
- `NotificationProxyController.java` ← proxy transparente para notification-service (removido junto com o proxy)
- `exception/RestExceptionHandler.java`
- `inbound/RegisterDeviceRequest.java`
- `outbound/DeviceResponse.java`
- `outbound/ResponseError.java`
- `presenter/DevicePresenter.java`

### adapters/factory

- `DeviceFactory.java`
- `NotificationFactory.java`

### adapters/fcm (Firebase no monolito)

- `FcmClientException.java`
- `FcmService.java`
- `NotificationMessageTO.java`

### adapters/model (entidades JPA duplicadas)

- `DeviceJpaEntity.java`
- `NotificationJpaEntity.java`

### adapters/repository (repositórios duplicados)

- `DeviceJpaRepository.java`
- `DeviceRepositoryAdapter.java`
- `NotificationJpaRepository.java`
- `NotificationRepositoryAdapter.java`

### app/domain/device

- `Device.java`, `DeviceStatus.java`, `DeviceType.java`

### app/domain/notification

- `NotificationStatus.java`, `PushNotification.java`

### app/domain/shared

- `FcmToken.java`, `NotificationBody.java`, `NotificationTitle.java`
- `exception/DeviceNotFoundException.java`
- `exception/DomainException.java`
- `exception/DuplicateDeviceTokenException.java`
- `exception/InvalidFcmTokenException.java`
- `exception/NotificationNotFoundException.java`
- `exception/PushSendingException.java`

### app/port

- `DeviceRepositoryPort.java`, `NotificationRepositoryPort.java`, `PushSenderPort.java`

### app/usecase/device

- `GetDeviceByTokenUseCase.java`, `ListDevicesUseCase.java`
- `RegisterDeviceCommand.java`, `RegisterDeviceUseCase.java`

### app/usecase/notification

- `GetNotificationByIdUseCase.java`, `GetNotificationHistoryUseCase.java`
- `MarkNotificationDeliveredUseCase.java` (já havia sido removido anteriormente)
- `NotificationFilter.java`, `NotificationHistoryQuery.java`
- `SendPushNotificationCommand.java`, `SendPushNotificationUseCase.java`

### config

- `FirebaseConfig.java` ← Firebase exclusivo da notification-service

### adapters/auth/web/rest/internal

- `FirebaseHealthResource.java` ← endpoint de saúde Firebase (depende do bean removido)

### Testes removidos (13 arquivos)

- `DeviceControllerTest.java`
- `FcmServiceTest.java` (adapters/fcm)
- `FcmServiceTest.java` (adapters/auth/service/notification — legacy disabled)
- `FcmResourceTest.java`
- `DeviceTest.java`, `PushNotificationTest.java`
- `FcmTokenTest.java`, `NotificationBodyTest.java`, `NotificationTitleTest.java`
- `RegisterDeviceUseCaseTest.java`
- `GetNotificationHistoryUseCaseTest.java`, `SendPushNotificationUseCaseTest.java`
- `app/domain/device/DeviceTest.java`

---

## 3. Funcionalidades Migradas para Microserviços

**Nenhuma.** Toda a lógica de Device e Notification já estava corretamente implementada nos microserviços desde a extração anterior. A remoção foi puramente de código duplicado.

---

## 4. Ajustes no Frontend

### Variáveis de ambiente (webpack)

- `webpack/environment.js`: adicionado `DEVICE_SERVICE_URL` (default `http://localhost:8081`) e `NOTIFICATION_SERVICE_URL` (default `http://localhost:8082`)
- `webpack/webpack.common.js`: DefinePlugin agora injeta as duas constantes no bundle
- `src/main/webapp/app/typings.d.ts`: declarações TypeScript para as novas constantes globais

### Service files — URLs absolutas para microserviços

| Arquivo                          | Antes                       | Depois                                                 |
| -------------------------------- | --------------------------- | ------------------------------------------------------ |
| `device.service.ts`              | `/api/v1/devices`           | `${DEVICE_SERVICE_URL}/api/v1/devices`                 |
| `notification.service.ts`        | `/api/v1/notifications`     | `${NOTIFICATION_SERVICE_URL}/api/v1/notifications`     |
| `notificationHistory.service.ts` | `/api/v1/notifications`     | `${NOTIFICATION_SERVICE_URL}/api/v1/notifications`     |
| `firebaseClient.ts`              | `/api/v1/notifications/ack` | `${NOTIFICATION_SERVICE_URL}/api/v1/notifications/ack` |

O JWT já é adicionado automaticamente pelo `axios-interceptor.ts` para todas as requisições (interceptor de request, independente de URL).

---

## 5. Outros Ajustes

### SecurityConfiguration (monolito)

- Removido: `requestMatchers("/api/internal/firebase/health").permitAll()`
- Removido: `requestMatchers(POST, "/api/v1/notifications/internal/fcm/ack").permitAll()` (legacy)
- Removido: `requestMatchers(POST, "/api/v1/notifications/ack").permitAll()` (proxy removido)

### WebConfigurer

- Removido: bean `RestTemplate` (era usado exclusivamente pelo NotificationProxyController)
- Removido: import `org.springframework.web.client.RestTemplate`

### application.yml

- Removido: bloco `firebase.tracing.enabled: false`
- Removido: bloco `notification-service.base-url`

### pom.xml

- Removido: dependência `com.google.firebase:firebase-admin:9.4.3` com exclusões de opentelemetry e grpc

---

## 6. Build e Testes

| Verificação                                            | Resultado                                 |
| ------------------------------------------------------ | ----------------------------------------- |
| `./mvnw compile -DskipTests`                           | ✅ BUILD SUCCESS                          |
| `./mvnw test-compile`                                  | ✅ BUILD SUCCESS                          |
| `webpack --config webpack/webpack.dev.js`              | ✅ compiled (0 errors, 3 warnings ESLint) |
| `GET /management/health` (monolito :8080)              | ✅ 200                                    |
| `GET /actuator/health` (device-service :8081)          | ✅ 200                                    |
| `GET /actuator/health` (notification-service :8082)    | ✅ 200                                    |
| `POST /api/authenticate`                               | ✅ JWT obtido                             |
| `GET http://localhost:8081/api/v1/devices` (JWT)       | ✅ 200                                    |
| `GET http://localhost:8082/api/v1/notifications` (JWT) | ✅ 200                                    |

---

## 7. Estrutura Final

```
push-notification-manager/              ← Monolito (auth gateway + frontend)
├── src/main/java/.../adapters/auth/    ← JHipster auth intocável
│   ├── web/rest/ (Account, Authenticate, User, Authority)
│   ├── service/ (UserService, MailService)
│   ├── security/ (JWT, DomainUserDetails)
│   ├── model/ (User, Authority, NotificationChannel)
│   └── repository/ (UserRepository, AuthorityRepository)
├── src/main/java/.../config/           ← Infra Spring Boot
├── src/main/webapp/                    ← Frontend React (JHipster)
│   └── app/
│       ├── entities/device/            → chama http://localhost:8081
│       └── entities/notification/     → chama http://localhost:8082

services/device-service/               ← Microserviço de Dispositivos (:8081)
│   Domínio: Device, FcmToken, DeviceStatus/Type
│   Use cases: Register, List, GetByToken
│   Adapters: REST, JPA, RabbitMQ

services/notification-service/         ← Microserviço de Notificações (:8082)
    Domínio: PushNotification, NotificationStatus, FcmToken
    Use cases: Send, GetById, GetHistory, MarkDelivered
    Adapters: REST, JPA, FCM, RabbitMQ (retry + DLQ)
```

---

## 8. Recomendações

1. **Remover tabelas Liquibase do monolito** — Os changesets `device` e `notification` no `src/main/resources/db/changelog/` pertencem aos microserviços e não são mais usados pelo monolito. Avaliar deprecação.

2. **CORS em produção** — `DEVICE_SERVICE_URL` e `NOTIFICATION_SERVICE_URL` são `localhost` por default. Para produção, injetar via variáveis de ambiente no build: `DEVICE_SERVICE_URL=https://device.prod.example.com npm run build`.

3. **`NotificationChannel` enum no monolito** — Presente em `adapters/auth/model/NotificationChannel.java`. Usado apenas pelo `NotificationFactory` (removido). Pode ser deletado na próxima limpeza.

4. **Tabelas de auth no DB** — O monolito ainda tem sua própria DB PostgreSQL com tabelas `jhi_user`, `jhi_authority`. Se no futuro o auth for extraído para um Identity Service, essas tabelas ficam lá.

5. **TechnicalStructureTest** — As regras ArchUnit `domainMustNotDependOnAdapters` e `portsMustNotDependOnAdapters` passam vacuamente (sem classes nos pacotes). Podem ser removidas para clareza.
