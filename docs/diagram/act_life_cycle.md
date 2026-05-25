actor Usuário
participant Browser
participant ServiceWorker
participant FCM
participant Backend
note over Browser,ServiceWorker: Registro e obtenção do token
Usuário->>Browser: Acessa a aplicação
Browser->>ServiceWorker: Registra service-worker.js
ServiceWorker->>FCM: requestPermission() + getToken(vapidKey)
FCM-->>ServiceWorker: FCM Token (ex: eXfG3k...)
ServiceWorker->>Backend: POST /api/devices { fcmToken, deviceType }
Backend-->>ServiceWorker: 201 Created
note over Backend,FCM: Envio de notificação
Backend->>FCM: POST /v1/messages:send { token, title, body }
FCM-->>Backend: { messageId: "..." }
note over FCM,ServiceWorker: Entrega da notificação
FCM->>ServiceWorker: Push Event
ServiceWorker->>Usuário: showNotification(title, body)
note over ServiceWorker,Backend: Confirmação de entrega
ServiceWorker->>Backend: POST /api/notifications/{id}/delivered
Backend-->>ServiceWorker: 200 OK
Note over Backend: status = DELIVERED
