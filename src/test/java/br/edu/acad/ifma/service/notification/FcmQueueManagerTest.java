package br.edu.acad.ifma.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FcmQueueManagerTest {

    private FcmService fcmService;
    private FcmQueueManager queueManager;

    @BeforeEach
    void setUp() {
        fcmService = Mockito.mock(FcmService.class);
        queueManager = new FcmQueueManager(fcmService);
    }

    @Test
    void testEnqueueAndProcess() throws Exception {
        when(fcmService.sendToToken(any(), any())).thenReturn("ok");

        queueManager.enqueue("t1", NotificationMessageTO.builder().titulo("T").corpo("C").build());
        queueManager.enqueue("t2", NotificationMessageTO.builder().titulo("T2").corpo("C2").build());

        int processed = queueManager.processQueue();
        assertThat(processed).isEqualTo(2);
        verify(fcmService, times(2)).sendToToken(any(), any());
    }
}
