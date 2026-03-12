package br.edu.acad.ifma.service.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class FcmService {

    private final Logger log = LoggerFactory.getLogger(FcmService.class);
    private final FirebaseMessaging firebaseMessaging;

    // Make FirebaseMessaging optional so the application can start even when credentials are not provided
    public FcmService(@Autowired(required = false) @Nullable FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    /**
     * Envia uma notificação FCM para um token de registro (push notification + dados).
     * Agora executado de forma síncrona (bloqueante).
     */
    public String sendToToken(String token, NotificationMessageTO mensagem) throws Exception {
        if (this.firebaseMessaging == null) {
            String msg = "FirebaseMessaging not configured; cannot send FCM message";
            log.warn("{} token={}", msg, token);
            throw new IllegalStateException(msg);
        }

        try {
            Message.Builder builder = Message.builder().setToken(token);

            if (mensagem.getTitulo() != null || mensagem.getCorpo() != null) {
                Notification.Builder nb = Notification.builder();
                if (mensagem.getTitulo() != null) nb.setTitle(mensagem.getTitulo());
                if (mensagem.getCorpo() != null) nb.setBody(mensagem.getCorpo());
                if (mensagem.getImagemUrl() != null) nb.setImage(mensagem.getImagemUrl());
                builder.setNotification(nb.build());
            }

            if (mensagem.getDados() != null && !mensagem.getDados().isEmpty()) {
                builder.putAllData(mensagem.getDados());
            }

            Message message = builder.build();

            String response = firebaseMessaging.send(message);
            log.debug("FCM sent message id={} token={}", response, token);
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message to token {}: {}", token, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error sending FCM message to token {}: {}", token, e.getMessage());
            throw e;
        }
    }

    /**
     * Envia apenas dados customizados para token (síncrono)
     */
    public String sendDataToToken(String token, Map<String, String> data) throws Exception {
        NotificationMessageTO m = NotificationMessageTO.builder().dados(data).build();
        return sendToToken(token, m);
    }
}
