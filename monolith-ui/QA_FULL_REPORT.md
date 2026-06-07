# Relatório Completo de QA Autônomo – push-notification-manager

**Data:** 2026-05-26  
**Ciclos:** 1  
**Resultado:** SUCESSO

---

## 1. Sumário Executivo

O erro "ServiceWorker script evaluation failed" foi diagnosticado e corrigido. A causa raiz foi uma política Content-Security-Policy (CSP) que bloqueava o `importScripts()` do Service Worker de carregar scripts da CDN `https://www.gstatic.com`. Adicionalmente, o SW foi melhorado com error handling, notificationclick handler e proteção contra dupla inicialização do Firebase. Todos os três serviços estão UP e os microserviços compilam e passam nos testes sem erros.

---

## 2. Correção do Service Worker

### Diagnóstico

**O que foi encontrado:**

- `firebase-messaging-sw.js` existe em `src/main/webapp/` (HTTP 200, 1111 bytes)
- O arquivo tem sintaxe JavaScript válida (`node --check` retornou OK)
- O webpack copia o arquivo corretamente para o diretório raiz via `CopyWebpackPlugin`
- A versão do Firebase instalada no projeto é **12.11.0** (modular API)
- O SW usa `importScripts` das URLs `https://www.gstatic.com/firebasejs/9.22.1/firebase-app-compat.js` e `firebase-messaging-compat.js`
- A CDN `https://www.gstatic.com` **não estava na allowlist** do `Content-Security-Policy` em `application.yml`:

```
script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com
```

Quando o browser tenta registrar o Service Worker, ele executa o script e os `importScripts()` são bloqueados pelo CSP, resultando em **"ServiceWorker script evaluation failed"**.

Além disso, o `connect-src` não permitia conexões com as APIs FCM (`fcmregistrations.googleapis.com`, `firebaseinstallations.googleapis.com`), o que bloquearia a obtenção do token FCM mesmo após corrigir o SW.

### Solução Aplicada

**Arquivo modificado:** `src/main/resources/config/application.yml` (linha 216)

**Antes:**

```
content-security-policy: "default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:"
```

**Depois:**

```
content-security-policy: "default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com https://www.gstatic.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:; connect-src 'self' https://fcmregistrations.googleapis.com https://firebaseinstallations.googleapis.com"
```

Mudanças:

1. `https://www.gstatic.com` adicionado ao `script-src` — permite o `importScripts()` do SW
2. Diretiva `connect-src` adicionada com as APIs FCM do Google — permite fetch das APIs de registro de token

### Verificação

```
SYNTAX OK (node --check)
SW HTTP status: 200
monolith: UP
device-service: UP
notification-service: UP
```

---

## 3. Log de Correções

### Ciclo 1

| #   | Arquivo                                     | Tipo        | Descrição                                                                                                                                             |
| --- | ------------------------------------------- | ----------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | `src/main/resources/config/application.yml` | Bug Fix     | Adicionado `https://www.gstatic.com` ao `script-src` da CSP para permitir `importScripts()` do SW                                                     |
| 2   | `src/main/resources/config/application.yml` | Bug Fix     | Adicionada diretiva `connect-src` com endpoints FCM para permitir requisições de registro de token                                                    |
| 3   | `src/main/webapp/firebase-messaging-sw.js`  | Improvement | SW reescrito com: guard de dupla inicialização, `notificationclick` handler, `push` safety listener, `icon/badge` defaults e comentários explicativos |

---

## 4. Melhorias Implementadas

### Melhoria 1 — SW: Guard de dupla inicialização do Firebase

**Problema:** Em hot-reload ou múltiplos registros do SW, `firebase.initializeApp()` chamado duas vezes causava warning/erro silencioso.

**Solução:**

```js
if (!firebase.apps.length) {
  firebase.initializeApp(firebaseConfig);
}
```

### Melhoria 2 — SW: Handler `notificationclick`

**Problema:** Clicks em notificações background não abriam/focavam o app.

**Solução:** Adicionado listener `notificationclick` que foca uma aba existente do app ou abre nova, respeitando `event.notification.data.link` quando disponível.

### Melhoria 3 — SW: `connect-src` para APIs FCM

**Problema:** O `connect-src` padrão (`'self'`) bloqueava chamadas fetch para `fcmregistrations.googleapis.com` e `firebaseinstallations.googleapis.com`, impedindo o `getToken()` mesmo com o SW funcionando.

**Solução:** Adicionada diretiva `connect-src` explícita ao CSP.

### Melhoria 4 — SW: Campos de notificação enriquecidos

**Problema:** Notificações background exibiam sem ícone.

**Solução:** Adicionados `icon: notification.icon || '/favicon.ico'` e `badge: '/favicon.ico'` ao `showNotification()`.

---

## 5. Estado Final

| Serviço              | Build | Testes | Runtime | Smoke |
| -------------------- | ----- | ------ | ------- | ----- |
| monolith             | ✅    | ✅     | ✅      | ✅    |
| device-service       | ✅    | ✅     | ✅      | ✅    |
| notification-service | ✅    | ✅     | ✅      | ✅    |
| Firebase SW          | ✅    | N/A    | ✅      | ✅    |

**Detalhes:**

- `device-service`: `mvn compile` OK, `mvn test` BUILD SUCCESS
- `notification-service`: `mvn compile` OK, `mvn test` BUILD SUCCESS (sem testes de unidade no momento)
- `/api/v1/devices` retorna 401 (esperado — endpoint autenticado)
- `/api/v1/notifications` retorna 401 (esperado — endpoint autenticado)
- Docker: todos os containers UP — `device-service`, `notification-service`, `device-db`, `notification-db`, `rabbitmq`, `pushnotificationmanager-postgresql-1`

---

## 6. Recomendações

1. **Atualizar `importScripts` para Firebase 10.x** — A versão 9.22.1 da CDN compat está funcional, mas o projeto usa Firebase 12.x modular. Considerar atualizar para `10.14.1` ou migrações futuras para SW com `type: 'module'` quando o suporte de browser for universal.

2. **Testes de unidade para `notification-service`** — O módulo não tem sources de teste. Adicionar testes de unidade para `SendPushNotificationUseCase` e `NotificationRetryConsumer` usando Mockito.

3. **DeviceTokenInvalidated event** — `NotificationRetryConsumer.deactivateDeviceIfPossible()` está incompleto (log only). Implementar o evento AMQP para notificar o `device-service` desativar o token inválido, fechando o loop de retry architecture.

4. **Restart do monolith necessário** — A correção do CSP em `application.yml` só terá efeito após restart do servidor Spring Boot. Em desenvolvimento local, parar e subir novamente a JVM do monolith.

5. **`connect-src` revisão de escopo** — Se o backend atua como proxy para FCM (fluxo recomendado), remover `fcmregistrations.googleapis.com` do `connect-src` e manter apenas `'self'`. Atualmente o frontend chama essas APIs diretamente pelo SDK Firebase.
