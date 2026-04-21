package br.edu.acad.ifma.adapters.fcm;

import br.edu.acad.ifma.app.domain.shared.FcmToken;
import br.edu.acad.ifma.app.domain.shared.NotificationBody;
import br.edu.acad.ifma.app.domain.shared.NotificationTitle;
import br.edu.acad.ifma.app.domain.shared.exception.PushSendingException;
import br.edu.acad.ifma.app.port.PushSenderPort;
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
public class FcmService implements PushSenderPort {

    private final Logger log = LoggerFactory.getLogger(FcmService.class);
    private final FirebaseMessaging firebaseMessaging;

    // Make FirebaseMessaging optional so the application can start even when credentials are not provided
    public FcmService(@Autowired(required = false) @Nullable FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    /**
     * Sends an FCM notification to a registration token (notification + data). Synchronous.
     */
    public String sendToToken(String token, NotificationMessageTO message) {
        if (this.firebaseMessaging == null) {
            String msg = "FirebaseMessaging not configured; cannot send FCM message";
            log.warn("{} token={}", msg, token);
            throw new FcmClientException(msg);
        }

        try {
            Message.Builder builder = Message.builder().setToken(token);

            if (message.getTitle() != null || message.getBody() != null) {
                Notification.Builder nb = Notification.builder();
                if (message.getTitle() != null) nb.setTitle(message.getTitle());
                if (message.getBody() != null) nb.setBody(message.getBody());
                if (message.getImageUrl() != null) nb.setImage(message.getImageUrl());
                builder.setNotification(nb.build());
            }

            if (!message.getData().isEmpty()) {
                builder.putAllData(message.getData());
            }

            Message msg = builder.build();

            String response = firebaseMessaging.send(msg);
            log.debug("FCM sent message id={} token={}", response, token);
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message to token {}: {}", token, e.getMessage());
            throw new FcmClientException("FCM send failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending FCM message to token {}: {}", token, e.getMessage());
            throw new FcmClientException("FCM send failed: " + e.getMessage(), e);
        }
    }

    /**
     * Send only custom data to token (synchronous)
     */
    public String sendDataToToken(String token, Map<String, String> data) {
        NotificationMessageTO m = NotificationMessageTO.builder().dataMap(data).build();
        return sendToToken(token, m);
    }

    // --- PushSenderPort implementation ---
    @Override
    public String sendPushNotification(FcmToken token, NotificationTitle title, NotificationBody body) {
        try {
            NotificationMessageTO msg = NotificationMessageTO.builder()
                .title(title == null ? null : title.value())
                .body(body == null ? null : body.value())
                .build();
            return sendToToken(token.value(), msg);
        } catch (FcmClientException e) {
            // convert adapter-specific exception to domain-level PushSendingException
            throw new PushSendingException("FCM send failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new PushSendingException("FCM send failed: " + e.getMessage(), e);
        }
    }
}
