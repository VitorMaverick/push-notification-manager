package br.edu.acad.ifma.app.usecase.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.acad.ifma.app.domain.notification.NotificationStatus;
import br.edu.acad.ifma.app.domain.notification.PushNotification;
import br.edu.acad.ifma.app.domain.shared.exception.PushSendingException;
import br.edu.acad.ifma.app.port.NotificationRepositoryPort;
import br.edu.acad.ifma.app.port.PushSenderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SendPushNotificationUseCaseTest {

    private static final String VALID_TOKEN = "abcdefghijklmnopqrstuvwxyz";

    private NotificationRepositoryPort notificationRepository;
    private PushSenderPort pushSender;
    private SendPushNotificationUseCase useCase;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepositoryPort.class);
        pushSender = mock(PushSenderPort.class);
        useCase = new SendPushNotificationUseCase(notificationRepository, pushSender);

        // save returns the same notification passed in (simulating a saved version)
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void happy_path_pending_to_sent() {
        when(pushSender.sendPushNotification(any(), any(), any())).thenReturn("fcm-id-001");

        SendPushNotificationCommand cmd = new SendPushNotificationCommand(VALID_TOKEN, "Hello", "World");
        PushNotification result = useCase.execute(cmd);

        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(result.getFcmMessageId()).isEqualTo("fcm-id-001");
    }

    @Test
    void fcm_exception_marks_failed_and_rethrows() {
        PushSendingException ex = new PushSendingException("FCM send failed: timeout", new RuntimeException("timeout"));
        when(pushSender.sendPushNotification(any(), any(), any())).thenThrow(ex);

        SendPushNotificationCommand cmd = new SendPushNotificationCommand(VALID_TOKEN, "Hello", "World");

        assertThatThrownBy(() -> useCase.execute(cmd)).isInstanceOf(PushSendingException.class);

        ArgumentCaptor<PushNotification> captor = ArgumentCaptor.forClass(PushNotification.class);
        verify(notificationRepository, org.mockito.Mockito.atLeast(2)).save(captor.capture());

        PushNotification savedFailed = captor
            .getAllValues()
            .stream()
            .filter(n -> n.getStatus() == NotificationStatus.FAILED)
            .findFirst()
            .orElseThrow();
        assertThat(savedFailed.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }
}
