package br.edu.acad.ifma.app.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.acad.ifma.app.domain.shared.FcmToken;
import br.edu.acad.ifma.app.domain.shared.NotificationBody;
import br.edu.acad.ifma.app.domain.shared.NotificationTitle;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PushNotificationTest {

    private static final String VALID_TOKEN = "abcdefghijklmnopqrstuvwxyz";

    @Test
    void builder_creates_entity() {
        Instant now = Instant.now();
        PushNotification n = PushNotification.builder()
            .withId(1L)
            .withTitle(new NotificationTitle("Title"))
            .withBody(new NotificationBody("Body"))
            .withRecipientToken(new FcmToken(VALID_TOKEN))
            .withStatus(NotificationStatus.PENDING)
            .withCreatedAt(now)
            .build();

        assertThat(n.getId()).isEqualTo(1L);
        assertThat(n.getTitle().value()).isEqualTo("Title");
        assertThat(n.getBody().value()).isEqualTo("Body");
        assertThat(n.getRecipientToken().value()).isEqualTo(VALID_TOKEN);
        assertThat(n.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(n.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void markSent_updates_status_and_fcmId() {
        PushNotification n = buildPending();
        n.markSent("fcm-msg-001");

        assertThat(n.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(n.getFcmMessageId()).isEqualTo("fcm-msg-001");
        assertThat(n.getSentAt()).isNotNull();
    }

    @Test
    void markDelivered_updates_status() {
        PushNotification n = buildPending();
        n.markDelivered();

        assertThat(n.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
        assertThat(n.getDeliveredAt()).isNotNull();
    }

    @Test
    void markFailed_updates_status() {
        PushNotification n = buildPending();
        n.markFailed("FCM send failed");

        assertThat(n.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(n.getFcmMessageId()).isEqualTo("FCM send failed");
    }

    private PushNotification buildPending() {
        return PushNotification.builder()
            .withTitle(new NotificationTitle("Title"))
            .withBody(new NotificationBody("Body"))
            .withRecipientToken(new FcmToken(VALID_TOKEN))
            .withStatus(NotificationStatus.PENDING)
            .withCreatedAt(Instant.now())
            .build();
    }
}
