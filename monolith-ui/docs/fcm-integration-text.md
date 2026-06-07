# Integração técnica com Firebase Cloud Messaging (FCM)

## Resumo acadêmico (tom formal)

A integração entre o sistema desenvolvido e o Firebase Cloud Messaging (FCM) foi concebida para assegurar a entrega confiável e auditável de notificações push a dispositivos web e móveis. O FCM, oferecido pelo Google, fornece uma interface de transporte escalável e compatível com diferentes plataformas, permitindo o envio de mensagens do tipo "notification" e "data" com suporte a personalização, segmentação e monitoramento. No sistema implementado, adotou-se uma arquitetura cliente-servidor em que o backend (microserviço Spring Boot) opera como provedor de mensagens e o frontend (aplicação React) atua como origem dos tokens de inscrição e receptor final das mensagens.

### Arquitetura e componentes

- Backend (Spring Boot): o componente `FirebaseConfig` cria condicionalmente um bean `FirebaseMessaging` quando a propriedade `firebase.service-account-file` aponta para um JSON de service account válido; o envio é efetuado pelo serviço `FcmService` que constrói o payload e invoca o Admin SDK/HTTP v1 para transmitir a mensagem.
- Frontend (React + Firebase JS SDK): a aplicação registra um Service Worker (`firebase-messaging-sw.js`) e solicita ao navegador um token de inscrição (`messaging.getToken`) usando a chave pública VAPID. Esse token é usado para endereçar notificações a uma instância específica do navegador.
- Fluxo de persistência e confirmação: antes do envio, a notificação é persistida na base de dados (entidade `Notification` / `NotificationMessage`); quando o cliente recebe a mensagem (evento `onMessage` no frontend ou o Service Worker no background), é enviado um ACK ao endpoint interno (`/api/internal/firebase/ack`) que grava o recebimento (`receivedAt` / `messageId`) para fins de auditoria.

### Fluxo de sequência (resumo)

O fluxo completo pode ser descrito em etapas numeradas:
1. O navegador registra o Service Worker e obtém um token de inscrição com `messaging.getToken({ vapidKey })`.
2. O frontend envia ao backend (endpoint de envio) o token e o payload desejado (título, corpo, dados personalizados).
3. O backend persiste a notificação e chama o `FirebaseMessaging` para enviar a mensagem ao token especificado.
4. O FCM aceita a requisição e retorna um `messageId` indicando que a mensagem foi enfileirada/entregue ao serviço de transporte.
5. O FCM entrega a mensagem ao dispositivo; o Service Worker (ou `onMessage` em foreground) é acionado no cliente.
6. O cliente envia um ACK ao backend confirmando o recebimento; o backend atualiza o registro de notificação com `receivedAt` e/ou `messageId`.

A Figura 1 (arquivo `docs/fcm-integration-sequence.svg`) ilustra esta sequência.

### Observabilidade e garantias de entrega

O retorno do FCM com um `messageId` representa confirmação de aceite pelo serviço de transporte do Google, mas não implica que o dispositivo necessariamente exibiu a notificação ao usuário. Para obter confirmação de entrega efetiva, o sistema depende do ACK originado no cliente ao processar a mensagem (`onMessage` ou Service Worker), o qual é persistido no servidor. Adicionalmente, logs detalhados das requisições HTTP para os endpoints de autenticação (`oauth2.googleapis.com/token`) e de envio (`fcm.googleapis.com/v1/...:send`) são registrados para auditoria e diagnostico.

### Configuração e segurança

- Credenciais: o backend utiliza um arquivo JSON de service account do Google para autenticar as chamadas do Admin SDK; este arquivo é referenciado pela propriedade `firebase.service-account-file` e deve ser mantido fora do repositório (Secret Manager ou armazenamento seguro).
- VAPID: para a funcionalidade Web Push, a chave pública VAPID é utilizada no frontend ao solicitar `getToken`; a chave privada deve permanecer protegida.
- Recomendações: em ambientes de produção, recomenda-se armazenar credenciais em um cofre de segredos (por exemplo, Google Secret Manager) e utilizar mecanismos de rotação e auditoria de chaves.

### Limitações e trabalho futuro

- Confirmação final de visualização: a ACK do cliente confirma recepção técnica, mas não garante que o usuário visualizou ou interagiu com a notificação; métricas adicionais (interações, cliques) podem ser capturadas para este fim.
- Resiliência e escala: para altos volumes, recomenda-se externalizar o envio para processos assíncronos em filas (RabbitMQ, Cloud Tasks) com retry/backoff.

---

## Inserção da figura e trecho pronto para o documento

Cole o SVG em sua pasta de imagens do documento (por exemplo `figures/fcm-integration-sequence.svg`) e inclua o seguinte no seu Markdown/LaTeX:
<?xml version="1.0" encoding="UTF-8"?>
Markdown:

```markdown
![Figura 1 — Diagrama de sequência do fluxo FCM](docs/fcm-integration-sequence.svg)
*Figura 1 — Fluxo de envio e confirmação de notificações via Firebase Cloud Messaging.*
```

LaTeX (com figura em `figures/`):

```latex
\begin{figure}[ht]
  \centering
  \includegraphics[width=0.9\textwidth]{figures/fcm-integration-sequence.svg}
  \caption{Diagrama de sequência: fluxo client → backend → FCM → client}
  \label{fig:fcm-sequence}
\end{figure}
```

### Comando opcional para gerar PNG localmente (caso precise):

```bash
# usando inkscape
inkscape docs/fcm-integration-sequence.svg --export-type=png --export-filename=docs/fcm-integration-sequence.png

# ou usando rsvg-convert
rsvg-convert -w 1200 -o docs/fcm-integration-sequence.png docs/fcm-integration-sequence.svg
```

## Referências (links oficiais)

1. Firebase Cloud Messaging — Overview: https://firebase.google.com/docs/cloud-messaging
2. Firebase Web: Using FCM in web apps (Service Worker / getToken / onMessage): https://firebase.google.com/docs/cloud-messaging/js/client
3. Firebase Admin SDK (Java) — Sending messages: https://firebase.google.com/docs/admin/setup
4. FCM HTTP v1 API: https://firebase.google.com/docs/reference/fcm/rest

---

Se quiser, eu também adapto o texto para inglês acadêmico, gero uma versão em LaTeX com figure environment já com legenda, ou atualizo o README do repositório referenciando a figura. O próximo passo que você prefere?
<svg xmlns="http://www.w3.org/2000/svg" width="900" height="380" viewBox="0 0 900 380">
  <style>
    .actor { fill:#f4f6f8; stroke:#2b73b9; stroke-width:2; rx:6; }
    .actorText { font-family: Arial, Helvetica, sans-serif; font-size:14px; fill:#0b2f5a; }
    .note { font-family: Arial, Helvetica, sans-serif; font-size:12px; fill:#073044; }
    .arrow { stroke:#333; stroke-width:2; fill:none; marker-end:url(#arrowhead);} 
    .num { font-family: Arial, Helvetica, sans-serif; font-size:12px; fill:#ffffff; }
    .badge { fill:#2b73b9; rx:8; }
  </style>

  <defs>
    <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
      <polygon points="0 0, 10 3.5, 0 7" fill="#333" />
    </marker>
  </defs>

  <!-- Actors -->
  <rect x="40" y="30" width="160" height="48" class="actor" />
  <text x="120" y="60" text-anchor="middle" class="actorText">Browser (Client)</text>

  <rect x="260" y="30" width="200" height="48" class="actor" />
  <text x="360" y="60" text-anchor="middle" class="actorText">Frontend / Service Worker</text>

  <rect x="500" y="30" width="220" height="48" class="actor" />
  <text x="610" y="60" text-anchor="middle" class="actorText">Backend (Spring Boot)</text>

  <rect x="740" y="30" width="120" height="48" class="actor" />
  <text x="800" y="60" text-anchor="middle" class="actorText">Firebase (FCM)</text>

  <!-- Lifelines -->
  <line x1="120" y1="80" x2="120" y2="320" stroke="#cfdde8" stroke-dasharray="4 4" />
  <line x1="360" y1="80" x2="360" y2="320" stroke="#cfdde8" stroke-dasharray="4 4" />
  <line x1="610" y1="80" x2="610" y2="320" stroke="#cfdde8" stroke-dasharray="4 4" />
  <line x1="800" y1="80" x2="800" y2="320" stroke="#cfdde8" stroke-dasharray="4 4" />

  <!-- Arrows and steps -->
  <!-- 1: register SW & get token -->
  <path d="M120 100 L360 100" class="arrow" />
  <rect x="240" y="88" width="24" height="24" class="badge" />
  <text x="252" y="105" text-anchor="middle" class="num">1</text>
  <text x="240" y="78" class="note">Register SW & get token (messaging.getToken)</text>

  <!-- 2: client -> backend send token+payload -->
  <path d="M360 140 L610 140" class="arrow" />
  <rect x="480" y="128" width="24" height="24" class="badge" />
  <text x="492" y="145" text-anchor="middle" class="num">2</text>
  <text x="470" y="118" class="note">POST /api/notification/send (token + payload)</text>

  <!-- 3: backend persists & calls FCM -->
  <path d="M610 180 L800 180" class="arrow" />
  <rect x="700" y="168" width="24" height="24" class="badge" />
  <text x="712" y="185" text-anchor="middle" class="num">3</text>
  <text x="640" y="160" class="note">Backend: persist Notification → FirebaseMessaging.send()</text>

  <!-- 4: FCM returns messageId -->
  <path d="M800 220 L610 220" class="arrow" />
  <rect x="700" y="208" width="24" height="24" class="badge" />
  <text x="712" y="225" text-anchor="middle" class="num">4</text>
  <text x="640" y="200" class="note">FCM responds (messageId)</text>

  <!-- 5: FCM delivers to device -->
  <path d="M800 260 L360 260" class="arrow" />
  <rect x="580" y="248" width="24" height="24" class="badge" />
  <text x="592" y="265" text-anchor="middle" class="num">5</text>
  <text x="400" y="240" class="note">FCM → Device (Service Worker receives)</text>

  <!-- 6: onMessage ACK to backend -->
  <path d="M360 300 L610 300" class="arrow" />
  <rect x="480" y="288" width="24" height="24" class="badge" />
  <text x="492" y="305" text-anchor="middle" class="num">6</text>
  <text x="470" y="280" class="note">Service Worker / onMessage → POST /api/internal/firebase/ack</text>

  <!-- Footer caption -->
  <text x="20" y="360" class="note">Figura 1 — Diagrama de sequência: fluxo de envio e confirmação de notificações via Firebase Cloud Messaging.</text>
</svg>

