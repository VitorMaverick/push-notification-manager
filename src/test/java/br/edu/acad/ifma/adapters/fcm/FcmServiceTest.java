package br.edu.acad.ifma.adapters.fcm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

        String res = fcmService.sendToToken("token-1", NotificationMessageTO.builder().title("T").body("C").data("k", "v").build());
        assertThat(res).isEqualTo("msg-id-1");

        verify(firebaseMessaging, times(1)).send(any(Message.class));
    }

    @Test
    void testSendToToken_failure() throws Exception {
        when(firebaseMessaging.send(any(Message.class))).thenThrow(new RuntimeException("fail"));

        Assertions.assertThrows(FcmClientException.class, () -> {
            fcmService.sendToToken("token-1", NotificationMessageTO.builder().title("T").body("C").build());
        });
    }
}
