package br.edu.acad.ifma.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.edu.acad.ifma.domain.Notification;
import br.edu.acad.ifma.domain.NotificationChannel;
import br.edu.acad.ifma.repository.NotificationMessageRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class NotificationServiceTest {

    private NotificationMessageRepository repository;
    private NotificationService service;

    private NotificationObserver emailObserver;
    private NotificationObserver smsObserver;
    private NotificationObserver fcmObserver;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(NotificationMessageRepository.class);

        emailObserver = Mockito.mock(NotificationObserver.class);
        smsObserver = Mockito.mock(NotificationObserver.class);
        fcmObserver = Mockito.mock(NotificationObserver.class);

        when(repository.save(any())).thenAnswer(i -> {
            Notification m = i.getArgument(0);
            m.setId(123L);
            return m;
        });

        // pass mocked observers directly (new preferred behavior)
        service = new NotificationService(repository, List.of(emailObserver, smsObserver, fcmObserver));
    }

    @Test
    void testCreateAndSend_email() {
        Notification msg = new Notification("Hi", "Body", NotificationChannel.EMAIL);

        Notification saved = service.createAndSend(msg);

        assertThat(saved.getId()).isEqualTo(123L);
        // verify all observers are notified for the message (now observers handle channel logic)
        verify(emailObserver, times(1)).onNotify(saved);
        verify(smsObserver, times(1)).onNotify(saved);
        verify(fcmObserver, times(1)).onNotify(saved);
    }

    @Test
    void testObserversNotified() {
        NotificationObserver observer = Mockito.mock(NotificationObserver.class);
        service.registerObserver(observer);

        Notification msg = new Notification("Hi", "Body", NotificationChannel.SMS);
        Notification saved = service.createAndSend(msg);

        verify(observer, times(1)).onNotify(saved);
    }

    @Test
    void testNoStrategyForChannel() {
        Notification msg = new Notification("Hi", "Body", null);
        Notification saved = service.createAndSend(msg);
        // observers should still be notified (observers decide how to handle channel == null)
        verify(emailObserver, times(1)).onNotify(saved);
        verify(smsObserver, times(1)).onNotify(saved);
        verify(fcmObserver, times(1)).onNotify(saved);
    }
}
