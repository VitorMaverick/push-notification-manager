package br.edu.acad.ifma.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class FcmServiceTest {

    private FirebaseMessaging firebaseMessaging;
    private FcmService fcmService;

    @BeforeEach
    void setUp() {
        firebaseMessaging = Mockito.mock(FirebaseMessaging.class);
        fcmService = new FcmService(firebaseMessaging);
    }

    @Test
    void testSendToToken_success() throws Exception {
        when(firebaseMessaging.send(any(Message.class))).thenReturn("msg-id-1");

        String res = fcmService.sendToToken("token-1", MensagemEnviada.builder().titulo("T").corpo("C").dado("k", "v").build());
        assertThat(res).isEqualTo("msg-id-1");

        // verify that firebaseMessaging.send was called with a Message (don't access internals)
        verify(firebaseMessaging, times(1)).send(any(Message.class));
    }

    @Test
    void testSendToToken_failure() throws Exception {
        when(firebaseMessaging.send(any(Message.class))).thenThrow(new RuntimeException("fail"));

        try {
            fcmService.sendToToken("token-1", MensagemEnviada.builder().titulo("T").corpo("C").build());
            // should not reach
            assertThat(false).isTrue();
        } catch (Exception e) {
            // expected: RuntimeException
            assertThat(e).isInstanceOf(RuntimeException.class);
        }
    }
}
